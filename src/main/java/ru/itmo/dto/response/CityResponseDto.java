package ru.itmo.dto.response;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class CityResponseDto {

    private Long id;

    @NotNull(message = "Name is required")
    @Size(min = 1, message = "Name cannot be empty")
    private String name;

    @NotNull(message = "Coordinates are required")
    private CoordinatesResponseDto coordinates;

    @NotNull(message = "Creation date is required")
    private LocalDate creationDate;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "0.01", inclusive = false, message = "Area must be greater than 0")
    private Double area;

    @Min(value = 1, message = "Population must be greater than 0")
    private Long population;

    @PastOrPresent(message = "Establishment date cannot be in the future")
    private LocalDate establishmentDate;

    private boolean capital;

    private Float metersAboveSeaLevel;

    @Min(value = -14, message = "Timezone must be at least -14")
    @Max(value = 15, message = "Timezone must be at most 15")
    private Integer timezone;

    @Min(value = 1, message = "Car code must be greater than 0")
    @Max(value = 1000, message = "Car code must be at most 1000")
    private Integer carCode;

    @NotNull(message = "Government type is required")
    private GovernmentResponseDto government;

    @NotNull(message = "Governor is required")
    private HumanResponseDto governor;

    @Min(value = 10000000, message = "OKTMO must be at least 8 digits")
    @Max(value = 99999999, message = "OKTMO must be at most 8 digits")
    private Long oktmo;

    @Min(value = 100000, message = "Postal code must be at least 6 digits")
    @Max(value = 999999, message = "Postal code must be at most 6 digits")
    private Long postalCode;

    private HumanResponseDto passport;

    public CityResponseDto() {}


}
