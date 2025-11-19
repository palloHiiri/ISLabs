package ru.itmo.mapper;

import org.mapstruct.Mapper;
import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.dto.response.CityResponseDto;
import ru.itmo.model.City;

@Mapper(componentModel = "spring")
public interface CityMapper {
    City toEntity(CityRequestDto dto);
    CityResponseDto toResponseDto(City city);
}
