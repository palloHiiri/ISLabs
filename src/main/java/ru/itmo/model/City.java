package ru.itmo.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class City {

    private Long id;

    private String name;
    private Coordinates coordinates;

    private LocalDate creationDate;

    private Double area;

    private Long population;

    private LocalDate establishmentDate;

    private boolean capital;

    private Float metersAboveSeaLevel;

    private Integer timezone;

    private Integer carCode;

    private Government government;

    private Human governor;

    private Human passport;

    private Long oktmo;

    private Long postalCode;

    public City() {}


}
