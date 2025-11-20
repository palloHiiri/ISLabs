package ru.itmo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.model.ImportOperation;
import ru.itmo.repository.ImportRepository;
import ru.itmo.repository.CityRepository;
import ru.itmo.model.City;
import ru.itmo.validator.CityValidator;

import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import java.io.InputStream;
import java.util.*;

@Service
public class ImportService {
    private final ObjectMapper objectMapper;
    private final ImportRepository importRepo;
    private final CityService cityService;
    private final TransactionTemplate requiresNewTx;
    private final Validator validator;
    private final CityRepository cityRepository;
    private final CityValidator cityValidator;

    public ImportService(ObjectMapper objectMapper,
                         ImportRepository importRepo,
                         CityService cityService,
                         PlatformTransactionManager transactionManager,
                         CityRepository cityRepository,
                         CityValidator cityValidator) {
        this.objectMapper = objectMapper;
        this.importRepo = importRepo;
        this.cityService = cityService;
        this.requiresNewTx = new TransactionTemplate(transactionManager);
        this.requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        this.cityRepository = cityRepository;
        this.cityValidator = cityValidator;
    }

    @Transactional
    public ImportOperation importCities(MultipartFile file) {
        ImportOperation op = new ImportOperation();
        op.setStatus("RUNNING");
        op.setAddedCount(0);
        op.setMessage(null);

        requiresNewTx.execute(status -> {
            Long id = importRepo.save(op);
            op.setId(id);
            return null;
        });

        try (InputStream is = file.getInputStream()) {
            List<CityRequestDto> list = objectMapper.readValue(is, new TypeReference<>() {});
            if (list == null || list.isEmpty()) {
                op.setStatus("FAILED");
                op.setMessage("Empty or invalid JSON array");
                op.setAddedCount(0);
                requiresNewTx.execute(status -> {
                    importRepo.update(op);
                    return null;
                });
                return op;
            }

            StringBuilder msg = new StringBuilder();
            List<City> entities = new ArrayList<>(list.size());

            Set<String> seenPostal = new HashSet<>();
            Set<String> seenOktmo = new HashSet<>();

            for (int i = 0; i < list.size(); i++) {
                CityRequestDto dto = list.get(i);

                Set<ConstraintViolation<CityRequestDto>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    for (ConstraintViolation<CityRequestDto> v : violations) {
                        msg.append("item ").append(i).append(": ").append(v.getPropertyPath()).append(" ").append(v.getMessage()).append("; ");
                    }
                    continue;
                }

                City entity = cityService.mapRequestToEntity(dto);

                String postalStr = null;
                if (dto != null) {
                    try {
                        Object p = dto.getClass().getMethod("getPostalCode").invoke(dto);
                        postalStr = p != null ? p.toString() : null;
                    } catch (Exception ignore) {
                        postalStr = entity.getPostalCode() != null ? entity.getPostalCode().toString() : null;
                    }
                }

                if (postalStr != null && !postalStr.isBlank()) {
                    try {
                        Long postalLong = Long.parseLong(postalStr);
                        if (cityRepository.existsByPostalCode(postalLong)) {
                            msg.append("item ").append(i).append(": postalCode already exists in DB ").append(postalStr).append("; ");
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    if (!seenPostal.add(postalStr)) {
                        msg.append("item ").append(i).append(": duplicate postalCode in import ").append(postalStr).append("; ");
                    }
                }

                String oktmoStr = null;
                if (dto != null) {
                    try {
                        Object o = dto.getClass().getMethod("getOktmo").invoke(dto);
                        oktmoStr = o != null ? o.toString() : null;
                    } catch (Exception ignore) {
                        oktmoStr = entity.getOktmo() != null ? entity.getOktmo().toString() : null;
                    }
                }

                if (oktmoStr != null && !oktmoStr.isBlank()) {
                    try {
                        Long oktmoLong = Long.parseLong(oktmoStr);
                        if (cityRepository.existsByOktmo(oktmoLong)) {
                            msg.append("item ").append(i).append(": oktmo already exists in DB ").append(oktmoStr).append("; ");
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    if (!seenOktmo.add(oktmoStr)) {
                        msg.append("item ").append(i).append(": duplicate oktmo in import ").append(oktmoStr).append("; ");
                    }
                }

                try {
                    cityValidator.validateUniqueness(entity, null);
                } catch (Exception ex) {
                    msg.append("item ").append(i).append(": ").append(ex.getMessage()).append("; ");
                }

                entities.add(entity);
            }

            if (msg.length() > 0) {
                op.setStatus("FAILED");
                op.setAddedCount(0);
                op.setMessage(msg.toString());
                requiresNewTx.execute(status -> {
                    importRepo.update(op);
                    return null;
                });
                return op;
            }

            int successCount = 0;
            for (City entity : entities) {
                cityService.addCityInCurrentTransaction(entity);
                successCount++;
            }

            op.setAddedCount(successCount);
            op.setMessage(null);
            op.setStatus(successCount == entities.size() ? "SUCCESS" : (successCount == 0 ? "FAILED" : "PARTIAL"));

            requiresNewTx.execute(status -> {
                importRepo.update(op);
                return null;
            });

            return op;
        } catch (Exception e) {
            op.setStatus("FAILED");
            op.setAddedCount(0);
            op.setMessage("Import error: " + e.getMessage());
            requiresNewTx.execute(status -> {
                importRepo.update(op);
                return null;
            });
            return op;
        }
    }


    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getImportHistory(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        long total = importRepo.countAll();
        int totalPages = (int) Math.max(1, Math.ceil((double) total / size));

        if (page >= totalPages) page = Math.max(0, totalPages - 1);

        List<ImportOperation> items = importRepo.findPaged(page, size);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("items", items);
        resp.put("currentPage", page);
        resp.put("totalItems", total);
        resp.put("totalPages", totalPages);
        resp.put("pageSize", size);
        return resp;
    }
}
