package ru.itmo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.dto.request.CoordinatesRequestDto;
import ru.itmo.dto.request.HumanRequestDto;
import ru.itmo.exception.DatabaseException;
import ru.itmo.exception.ImportValidationException;
import ru.itmo.model.City;
import ru.itmo.model.ImportOperation;
import ru.itmo.validator.CityValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ImportProcessingService {
    private final ObjectMapper objectMapper;
    private final CityService cityService;
    private final Validator validator;
    private final CityValidator cityValidator;
    private final ImportOperationService importOperationService;
    private final FileStorageService fileStorageService;

    private volatile boolean simulateBusinessError = false;

    public ImportProcessingService(ObjectMapper objectMapper,
                                   CityService cityService,
                                   CityValidator cityValidator,
                                   ImportOperationService importOperationService,
                                   FileStorageService fileStorageService) {
        this.objectMapper = objectMapper;
        this.cityService = cityService;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        this.cityValidator = cityValidator;
        this.importOperationService = importOperationService;
        this.fileStorageService = fileStorageService;
    }

    public void setSimulateBusinessError(boolean simulate) {
        this.simulateBusinessError = simulate;
        System.out.println("simulateBusinessError set to: " + simulate);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImportOperation processImportFile(MultipartFile file, String s3key, ImportOperation op) {
//        System.out.println("simulateBusinessError is: " + simulateBusinessError);
//        if (simulateBusinessError) {
//            System.out.println("Triggering NullPointerException...");
//        }

        List<CityRequestDto> list;
        try {
            list = objectMapper.readValue(file.getInputStream(), new TypeReference<>() {});
        } catch (Exception e) {
            cleanupOnError(s3key);
            importOperationService.updateStatus(op, "FAILED", 0, "Failed to parse JSON: " + e.getMessage());
            throw new ImportValidationException("Failed to parse JSON: " + e.getMessage());
        }

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
    }

    private void cleanupOnError(String s3key) {
        if (s3key != null) {
            try {
                fileStorageService.deleteFile(s3key);
            } catch (Exception ignored) {}
        }
    }
}

