package ru.itmo.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.dto.response.HumanResponseDto;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class CityRequestDto {

    @NotNull(message = "Name is required")
    @Size(min = 1, message = "Name cannot be empty")
    private String name;

    @NotNull(message = "Coordinates are required")
    private CoordinatesRequestDto coordinates;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "0.01", inclusive = false, message = "Area must be greater than 0")
    private Double area;

    @Min(value = 1, message = "Population must be greater than 0")
    private Long population;

    private LocalDate creationDate;

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

    private GovernmentRequestDto government;

    @NotNull(message = "Governor is required")
    private HumanRequestDto governor;

    @Min(value = 10000000, message = "OKTMO must be at least 8 digits")
    @Max(value = 99999999, message = "OKTMO must be at most 8 digits")
    private Long oktmo;

    @Min(value = 100000, message = "Postal code must be at least 6 digits")
    @Max(value = 999999, message = "Postal code must be at most 6 digits")
    private Long postalCode;


    private HumanRequestDto passport;

    public CityRequestDto() {}


}
