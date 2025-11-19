package ru.itmo.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.dto.request.HumanRequestDto;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtil {

    private static final Validator validator;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    public static void validateCity(CityRequestDto city) {
        Set<ConstraintViolation<CityRequestDto>> violations = validator.validate(city);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(violation ->
                            violation.getPropertyPath() + ": " + violation.getMessage()
                    )
                    .collect(Collectors.joining("; "));

            throw new RuntimeException("Validation failed: " + errorMessage);
        }

        // Дополнительная кастомная валидация
        validateCityCustom(city);
    }

    public static void validateHuman(HumanRequestDto human) {
        Set<ConstraintViolation<HumanRequestDto>> violations = validator.validate(human);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(violation ->
                            violation.getPropertyPath() + ": " + violation.getMessage()
                    )
                    .collect(Collectors.joining("; "));

            throw new RuntimeException("Human validation failed: " + errorMessage);
        }
    }

    private static void validateCityCustom(CityRequestDto city) {
        // Дополнительные проверки, которых нет в аннотациях
        if (city.getCoordinates() != null) {
            if (city.getCoordinates().getX() > 913) {
                throw new RuntimeException("X coordinate must be ≤ 913");
            }
            if (city.getCoordinates().getY() <= -243) {
                throw new RuntimeException("Y coordinate must be > -243");
            }
        }

        if (city.getEstablishmentDate() != null) {
            if (city.getEstablishmentDate().isAfter(java.time.LocalDate.now())) {
                throw new RuntimeException("Establishment date must be before current date");
            }
        }
    }
}