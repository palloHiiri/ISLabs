package ru.itmo.controller;

import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.dto.response.CityResponseDto;
import jakarta.validation.Valid;
import ru.itmo.dto.response.CityResponseDto;
import ru.itmo.model.City;
import ru.itmo.model.Coordinates;
import ru.itmo.model.Human;
import ru.itmo.service.CityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cities")
public class CityController {
    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping("/")
    public ResponseEntity<?> addCity(@Valid @RequestBody CityRequestDto cityRequest) {
        try {
            City city = cityService.mapRequestToEntity(cityRequest);
            cityService.addCity(city);
            CityResponseDto response = cityService.mapEntityToResponse(city);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error adding city: " + e.getMessage());
            return createErrorResponse("Failed to add city: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCity(@PathVariable("id") Long id) {
        try {
            if (id == null || id <= 0) {
                return createErrorResponse("Invalid city ID", HttpStatus.BAD_REQUEST);
            }

            City city = cityService.getCity(id);
            if (city != null) {
                CityResponseDto response = cityService.mapEntityToResponse(city);
                return ResponseEntity.ok(response);
            } else {
                return createErrorResponse("City with ID " + id + " not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Error getting city: " + e.getMessage());
            return createErrorResponse("Failed to retrieve city: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllCities(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "idFilter", defaultValue = "") String idFilter,
            @RequestParam(value = "nameFilter", defaultValue = "") String nameFilter,
            @RequestParam(value = "coordinatesXFilter", defaultValue = "") String coordinatesXFilter,
            @RequestParam(value = "coordinatesYFilter", defaultValue = "") String coordinatesYFilter,
            @RequestParam(value = "creationDateFilter", defaultValue = "") String creationDateFilter,
            @RequestParam(value = "areaFilter", defaultValue = "") String areaFilter,
            @RequestParam(value = "populationFilter", defaultValue = "") String populationFilter,
            @RequestParam(value = "establishmentDateFilter", defaultValue = "") String establishmentDateFilter,
            @RequestParam(value = "capitalFilter", defaultValue = "") String capitalFilter,
            @RequestParam(value = "metersAboveSeaLevelFilter", defaultValue = "") String metersAboveSeaLevelFilter,
            @RequestParam(value = "timezoneFilter", defaultValue = "") String timezoneFilter,
            @RequestParam(value = "carCodeFilter", defaultValue = "") String carCodeFilter,
            @RequestParam(value = "governmentFilter", defaultValue = "") String governmentFilter,
            @RequestParam(value = "governorFilter", defaultValue = "") String governorFilter,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection) {
        try {
            if (page < 0) {
                return createErrorResponse("Page number cannot be negative", HttpStatus.BAD_REQUEST);
            }
            if (size <= 0 || size > 100) {
                return createErrorResponse("Page size must be between 1 and 100", HttpStatus.BAD_REQUEST);
            }
            Map<String, String> filters = new HashMap<>();
            filters.put("id", idFilter);
            filters.put("name", nameFilter);
            filters.put("coordinatesX", coordinatesXFilter);
            filters.put("coordinatesY", coordinatesYFilter);
            filters.put("creationDate", creationDateFilter);
            filters.put("area", areaFilter);
            filters.put("population", populationFilter);
            filters.put("establishmentDate", establishmentDateFilter);
            filters.put("capital", capitalFilter);
            filters.put("metersAboveSeaLevel", metersAboveSeaLevelFilter);
            filters.put("timezone", timezoneFilter);
            filters.put("carCode", carCodeFilter);
            filters.put("government", governmentFilter);
            filters.put("governor", governorFilter);
            List<City> filteredCities = cityService.getCitiesWithFiltersAndSort(filters, sortBy, sortDirection);
            int totalCities = filteredCities.size();
            int totalPages = (int) Math.ceil((double) totalCities / size);
            if (page < 0) page = 0;
            if (size <= 0) size = 5;
            int fromIndex = page * size;
            if (fromIndex >= totalCities) {
                fromIndex = Math.max(0, (totalPages - 1) * size);
                page = Math.max(0, totalPages - 1);
            }
            int toIndex = Math.min(fromIndex + size, totalCities);
            List<City> pageContent = fromIndex < totalCities ? filteredCities.subList(fromIndex, toIndex) : List.of();
            List<CityResponseDto> pageContentDto = pageContent.stream()
                    .map(cityService::mapEntityToResponse)
                    .collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("cities", pageContentDto);
            response.put("currentPage", page);
            response.put("totalItems", totalCities);
            response.put("totalPages", totalPages);
            response.put("pageSize", size);
            response.put("filters", filters);
            response.put("sortBy", sortBy);
            response.put("sortDirection", sortDirection);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getAllCities: " + e.getMessage());
            return createErrorResponse("Failed to retrieve cities: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCity(@PathVariable("id") Long id, @Valid @RequestBody CityRequestDto cityRequest) {
        try {
            City city = cityService.mapRequestToEntity(cityRequest);
            city.setId(id);
            cityService.updateCity(city);
            CityResponseDto response = cityService.mapEntityToResponse(city);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error updating city: " + e.getMessage());
            return createErrorResponse("Failed to update city: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCity(@PathVariable("id") Long id) {
        try {
            if (id == null || id <= 0) {
                return createErrorResponse("Invalid city ID", HttpStatus.BAD_REQUEST);
            }

            City city = cityService.getCity(id);
            if (city != null) {
                cityService.deleteCityCascade(city);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "City and related cities deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return createErrorResponse("City with ID " + id + " not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Error deleting city: " + e.getMessage());
            return createErrorResponse("Failed to delete city: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/sum-of-timezones")
    public ResponseEntity<?> getSumOfTimezones() {
        try {
            Double sum = cityService.getSumOfTimezones();
            return ResponseEntity.ok(sum);
        } catch (Exception e) {
            System.err.println("Error calculating sum of timezones: " + e.getMessage());
            return createErrorResponse("Failed to calculate sum of timezones: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/governors")
    public ResponseEntity<List<Human>> getAllGovernors() {
        List<Human> governors = cityService.getAllGovernors();
        return ResponseEntity.ok(governors);
    }

    @GetMapping("/coordinates")
    public ResponseEntity<List<Coordinates>> getAllCoordinates() {
        List<Coordinates> coordinates = cityService.getAllCoordinates();
        return ResponseEntity.ok(coordinates);
    }

    @GetMapping("/average-car-code")
    public ResponseEntity<?> getAverageCarCode() {
        try {
            Double averageCarCode = cityService.getAverageCarCode();
            return ResponseEntity.ok(averageCarCode);
        } catch (Exception e) {
            System.err.println("Error calculating average car code: " + e.getMessage());
            return createErrorResponse("Failed to calculate average car code: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/timezone-less-than/{timezone}")
    public ResponseEntity<?> getCitiesWithTimezoneLessThan(@PathVariable("timezone") int timezone) {
        try {
            if (timezone < -13 || timezone > 15) {
                return createErrorResponse("Timezone must be between -13 and 15", HttpStatus.BAD_REQUEST);
            }

            List<City> cities = cityService.getCitiesWithTimezoneLessThan(timezone);
            List<CityResponseDto> citiesDto = cities.stream()
                    .map(cityService::mapEntityToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(citiesDto);
        } catch (Exception e) {
            System.err.println("Error getting cities with timezone less than " + timezone + ": " + e.getMessage());
            return createErrorResponse("Failed to get cities with timezone greater than " + timezone + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/distance-to-most-populated")
    public ResponseEntity<?> getDistanceToMostPopulatedCity() {
        try {
            Double dist = cityService.calculateDistanceToTheMostPopulatedCity();
            return ResponseEntity.ok(dist);
        } catch (Exception e) {
            System.err.println("Error calculating distance to most populated city: " + e.getMessage());
            return createErrorResponse("Failed to calculate distance to most populated city: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/distance-to-newest")
    public ResponseEntity<?> getDistanceToNewestCity() {
        try {
            Double dist = cityService.calculateDistanceToNewestCity();
            return ResponseEntity.ok(dist);
        } catch (Exception e) {
            System.err.println("Error calculating distance to newest city: " + e.getMessage());
            return createErrorResponse("Failed to calculate distance to newest city: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(errorResponse);
    }
}