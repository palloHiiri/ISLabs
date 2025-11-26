package ru.itmo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.dto.request.CoordinatesRequestDto;
import ru.itmo.dto.request.HumanRequestDto;
import ru.itmo.exception.ImportValidationException;
import ru.itmo.model.ImportOperation;
import ru.itmo.repository.ImportRepository;
import ru.itmo.model.City;
import ru.itmo.validator.CityValidator;

import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.springframework.transaction.annotation.Isolation;

import java.io.InputStream;
import java.util.*;
@Service
public class ImportService {
    private final ObjectMapper objectMapper;
    private final ImportRepository importRepo;
    private final CityService cityService;
    private final Validator validator;
    private final CityValidator cityValidator;
    private final ImportOperationService importOperationService;

    public ImportService(ObjectMapper objectMapper,
                         ImportRepository importRepo,
                         CityService cityService,
                         CityValidator cityValidator,
                         ImportOperationService importOperationService) {
        this.objectMapper = objectMapper;
        this.importRepo = importRepo;
        this.cityService = cityService;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        this.cityValidator = cityValidator;
        this.importOperationService = importOperationService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImportOperation importCities(MultipartFile file) {
        ImportOperation op = importOperationService.createOperation();

        try (InputStream is = file.getInputStream()) {
            List<CityRequestDto> list = objectMapper.readValue(is, new TypeReference<>() {});

            if (list == null || list.isEmpty()) {
                return importOperationService.updateStatus(op, "FAILED", 0, "Empty or invalid JSON array");
            }

            StringBuilder msg = new StringBuilder();
            List<City> entities = new ArrayList<>(list.size());

            for (int i = 0; i < list.size(); i++) {
                CityRequestDto dto = list.get(i);

                Set<ConstraintViolation<CityRequestDto>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    for (ConstraintViolation<CityRequestDto> v : violations) {
                        msg.append("item ").append(i).append(": ")
                                .append(v.getPropertyPath()).append(" ")
                                .append(v.getMessage()).append("; ");
                    }
                    continue;
                }

                if (dto.getCoordinates() != null) {
                    Set<ConstraintViolation<CoordinatesRequestDto>> coordViolations =
                            validator.validate(dto.getCoordinates());
                    if (!coordViolations.isEmpty()) {
                        for (ConstraintViolation<CoordinatesRequestDto> v : coordViolations) {
                            msg.append("item ").append(i).append(": coordinates.")
                                    .append(v.getPropertyPath()).append(" ")
                                    .append(v.getMessage()).append("; ");
                        }
                        continue;
                    }
                }

                if (dto.getGovernor() != null) {
                    Set<ConstraintViolation<HumanRequestDto>> humanViolations =
                            validator.validate(dto.getGovernor());
                    if (!humanViolations.isEmpty()) {
                        for (ConstraintViolation<HumanRequestDto> v : humanViolations) {
                            msg.append("item ").append(i).append(": governor.")
                                    .append(v.getPropertyPath()).append(" ")
                                    .append(v.getMessage()).append("; ");
                        }
                        continue;
                    }
                }


                City entity = cityService.mapRequestToEntity(dto);

                try {
                    cityValidator.validateUniqueness(entity, null);
                } catch (Exception ex) {
                    msg.append("item ").append(i).append(": ")
                            .append(ex.getMessage()).append("; ");
                }

                entities.add(entity);
            }

            if (!msg.isEmpty()) {
                importOperationService.updateStatus(op, "FAILED", 0, msg.toString());
                throw new IllegalArgumentException("Validation errors during import");
            }

            int successCount = 0;
            for (City entity : entities) {
                cityService.addCity(entity);
                successCount++;
            }

            String status = successCount == entities.size() ? "SUCCESS" : "FAILED";
            return importOperationService.updateStatus(op, status, successCount, null);

        } catch (Exception e) {
            importOperationService.updateStatus(op, "FAILED", 0, "Import error: " + e.getMessage());
            throw new ImportValidationException("Import failed. Incorrect data in file", e);
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Map<String, Object> getImportHistory(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        long total = importRepo.countAll();
        int totalPages = (int) Math.max(1, Math.ceil((double) total / size));

        if (page >= totalPages) page = Math.max(0, totalPages - 1);

        List<ImportOperation> items = importRepo.findPaged(page, size);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("currentPage", page);
        resp.put("totalItems", total);
        resp.put("totalPages", totalPages);
        resp.put("pageSize", size);
        return resp;
    }
}
