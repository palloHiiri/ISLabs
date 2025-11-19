package ru.itmo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CoordinatesRequestDto {

    @NotNull
    @Max(value = 913, message = "X coordinate must be less than or equal to 913")
    private Long x;

    @NotNull
    @Min(value = -244, message = "Y coordinate must be greater than -244")
    private Long y;

    public CoordinatesRequestDto() {

    }
}
