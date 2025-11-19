package ru.itmo.dto.response;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HumanResponseDto {


    @NotNull
    @Size(min = 1, message = "Name cannot be empty")
    private String name;

    @Min(value = 1000000000L, message = "Passport number must be exactly 10 digits long")
    @Max(value = 9999999999L, message = "Passport number must be exactly 10 digits long")
    private Long passport;

    public HumanResponseDto() {

    }
}
