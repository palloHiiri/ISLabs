package ru.itmo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@NotNull
public enum GovernmentRequestDto {
    ARISTOCRACY,
    MATRIARCHY,
    NOOCRACY,
    PATRIARCHY;


    @Override
    public String toString() {
        return name();
    }

    public static GovernmentRequestDto fromString(String value) {
        return valueOf(value);
    }

}
