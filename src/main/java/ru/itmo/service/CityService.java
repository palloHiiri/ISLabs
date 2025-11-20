package ru.itmo.service;

import lombok.AllArgsConstructor;
import ru.itmo.mapper.CityMapper;
import ru.itmo.model.City;
import ru.itmo.model.Coordinates;
import ru.itmo.model.Human;
import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.dto.response.CityResponseDto;
import ru.itmo.repository.CityRepository;
import ru.itmo.repository.HumanRepository;
import ru.itmo.validator.CityValidator;
import ru.itmo.websocket.CityWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;


import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class CityService {
    private final CityRepository cityRepository;
    private final CityWebSocketHandler webSocketHandler;
    private final CityMapper cityMapper;
    private final CityValidator cityValidator;
    private final HumanRepository humanRepository;
    private final HumanService humanService;



    @Transactional
    public Long addCity(City city) {
        cityValidator.validateUniqueness(city, null);

        if (city.getCreationDate() == null) {
            city.setCreationDate(java.time.LocalDate.now());
        }

        if (city.getGovernor() != null) {
            Human governor = humanService.processGovernor(city.getGovernor());
            city.setGovernor(governor);
        }

        Long id = cityRepository.save(city);
        webSocketHandler.broadcastUpdate("CITY_ADDED", city);
        return id;
    }

    @Transactional(readOnly = true)
    public City getCity(Long id) {
        return cityRepository.findById(id);
    }

    @Transactional
    public void updateCity(City city) {
        cityValidator.validateUniqueness(city, city.getId());

        if (city.getGovernor() != null) {
            Human governor = humanService.processGovernor(city.getGovernor());
            city.setGovernor(governor);
        }

        cityRepository.update(city);
        webSocketHandler.broadcastUpdate("CITY_UPDATED", city);
    }

    @Transactional(readOnly = true)
    public Double getAverageCarCode(){
        return cityRepository.getAverageCarCode();
    }

    @Transactional(readOnly = true)
    public Double getSumOfTimezones(){
        return cityRepository.getSumOfTimezones();
    }

    @Transactional(readOnly = true)
    public List<City> getCitiesWithTimezoneLessThan(int timezone){
        return cityRepository.getCitiesWithTimezoneLessThan(timezone);
    }

    @Transactional(readOnly = true)
    public Double calculateDistanceToTheMostPopulatedCity(){
        return cityRepository.calculateDistanceToTheMostPopulatedCity();
    }

    @Transactional(readOnly = true)
    public List<Human> getAllGovernors() {
        return cityRepository.findAllGovernors();
    }

    @Transactional(readOnly = true)
    public List<Coordinates> getAllCoordinates() {
        return cityRepository.findAllCoordinates();
    }

    @Transactional
    public void deleteCityCascade(City city) {
        cityRepository.delete(city);
        webSocketHandler.broadcastUpdate("CITY_DELETED", city);
    }

    @Transactional(readOnly = true)
    public Double calculateDistanceToNewestCity(){
        return cityRepository.calculateDistanceToNewestCity();
    }

    @Transactional(readOnly = true)
    public List<City> getCitiesWithFiltersAndSort(Map<String, String> filters, String sortBy, String sortDirection) {
        return cityRepository.findWithFiltersAndSort(filters, sortBy, sortDirection);
    }

    public City mapRequestToEntity(CityRequestDto dto) {
        return cityMapper.toEntity(dto);
    }

    public CityResponseDto mapEntityToResponse(City city) {
        return cityMapper.toResponseDto(city);
    }


}