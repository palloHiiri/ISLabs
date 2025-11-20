package ru.itmo.validator;

import ru.itmo.model.City;
import ru.itmo.repository.CityRepository;
import org.springframework.stereotype.Component;

@Component
public class CityValidator {
    private final CityRepository cityRepository;

    public CityValidator(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public void validateUniqueness(City city, Long excludeId) {
        if (city.getPostalCode() != null) {
            if (cityRepository.existsByPostalCode(city.getPostalCode())) {
                if (excludeId == null || !cityRepository.findById(excludeId).getPostalCode().equals(city.getPostalCode())) {
                    throw new IllegalArgumentException("Postal code must be unique");
                }
            }
        }
        if (city.getOktmo() != null) {
            if (cityRepository.existsByOktmo(city.getOktmo())) {
                if (excludeId == null || !cityRepository.findById(excludeId).getOktmo().equals(city.getOktmo())) {
                    throw new IllegalArgumentException("OKTMO must be unique");
                }
            }
        }
    }
}
