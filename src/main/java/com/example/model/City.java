package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

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

    public City() {}


}
