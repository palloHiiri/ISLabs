package ru.itmo.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HumanResponseDto {
    private String name;

    @NotNull
    @Size(min = 1, message = "Name cannot be empty")
    public HumanResponseDto() {

    }
}
