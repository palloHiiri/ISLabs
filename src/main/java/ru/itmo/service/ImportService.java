package ru.itmo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.dto.request.CoordinatesRequestDto;
import ru.itmo.dto.request.HumanRequestDto;
import ru.itmo.exception.DatabaseException;
import ru.itmo.exception.FileStorageException;
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

import java.util.*;
@Service
public class ImportService {
    private final ObjectMapper objectMapper;
    private final ImportRepository importRepo;
    private final CityService cityService;
    private final Validator validator;
    private final CityValidator cityValidator;
    private final ImportOperationService importOperationService;
    private final FileStorageService fileStorageService;

    private volatile boolean simulateBusinessError = false;

    public void setSimulateBusinessError(boolean simulate) {
        this.simulateBusinessError = simulate;
        System.out.println("simulateBusinessError set to: " + simulate);
    }

    public ImportService(ObjectMapper objectMapper,
                         ImportRepository importRepo,
                         CityService cityService,
                         CityValidator cityValidator,
                         ImportOperationService importOperationService, FileStorageService fileStorageService) {
        this.objectMapper = objectMapper;
        this.importRepo = importRepo;
        this.cityService = cityService;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        this.cityValidator = cityValidator;
        this.importOperationService = importOperationService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImportOperation importCities(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String s3key = null;
        ImportOperation op = null;

        try {
            op = importOperationService.createOperation(filename, null);
        } catch (Exception e) {
            throw new DatabaseException("Failed to create import operation", e);
        }

        try {
            s3key = fileStorageService.uploadFile(filename, file.getInputStream(), file.getSize());
            op.setS3key(s3key);
            importOperationService.updateS3Key(op, s3key);
        } catch (FileStorageException e) {
            importOperationService.updateStatus(op, "FAILED", 0, "File storage error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            if (s3key != null) {
                try {
                    fileStorageService.deleteFile(s3key);
                } catch (Exception ignored) {}
            }
            if (op != null) {
                try {
                    importOperationService.updateStatus(op, "FAILED", 0, "File upload error: " + e.getMessage());
                } catch (Exception ignored) {}
            }
            throw new FileStorageException("Failed to upload file", e);
        }

        try {
            System.out.println("simulateBusinessError is: " + simulateBusinessError);
            if (simulateBusinessError) {
                System.out.println("Triggering NullPointerException...");
//                String nullString = null;
//                nullString.length();
            }

            List<CityRequestDto> list = objectMapper.readValue(file.getInputStream(), new TypeReference<>() {});

            if (list == null || list.isEmpty()) {
                cleanupOnError(s3key);
                importOperationService.updateStatus(op, "FAILED", 0, "Empty or invalid JSON array");
                throw new ImportValidationException("Empty or invalid JSON array");
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
                    entities.add(entity);
                } catch (Exception ex) {
                    msg.append("item ").append(i).append(": ")
                            .append(ex.getMessage()).append("; ");
                }
            }

            if (!msg.isEmpty()) {
                cleanupOnError(s3key);
                importOperationService.updateStatus(op, "FAILED", 0, "Validation errors: " + msg.toString());
                throw new ImportValidationException("Validation errors: " + msg.toString());
            }

            int successCount = 0;
            for (City entity : entities) {
                try {
                    cityService.addCity(entity);
                    successCount++;
                } catch (Exception ex) {
                    cleanupOnError(s3key);
                    importOperationService.updateStatus(op, "FAILED", 0, "Failed to add cities: " + ex.getMessage());
                    throw new DatabaseException("Failed to add cities to database", ex);
                }
            }

            String status = successCount == entities.size() ? "SUCCESS" : "FAILED";
            return importOperationService.updateStatus(op, status, successCount, null);

        } catch (FileStorageException | ImportValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            cleanupOnError(s3key);
            try {
                importOperationService.updateStatus(op, "FAILED", 0, "Business logic error: " + e.getMessage());
            } catch (Exception ignored) {}
            throw e;
        } catch (Exception e) {
            cleanupOnError(s3key);
            try {
                importOperationService.updateStatus(op, "FAILED", 0, "Database error: " + e.getMessage());
            } catch (Exception ignored) {}
            throw new DatabaseException("Database error during import", e);
        }
    }

    private void cleanupOnError(String s3key) {
        if (s3key != null) {
            try {
                fileStorageService.deleteFile(s3key);
            } catch (Exception ignored) {}
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

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ImportOperation getImportOperationById(Long id) {
        return importRepo.findById(id);
    }
}
