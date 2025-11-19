package ru.itmo.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Coordinates {

    private Long x;

    private Long y;

    public Coordinates() {

    }
}
