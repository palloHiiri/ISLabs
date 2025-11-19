package ru.itmo.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@NotNull
public enum GovernmentResponseDto {
    ARISTOCRACY,
    MATRIARCHY,
    NOOCRACY,
    PATRIARCHY;


    @Override
    public String toString() {
        return name();
    }

    public static GovernmentResponseDto fromString(String value) {
        return valueOf(value);
    }

}
