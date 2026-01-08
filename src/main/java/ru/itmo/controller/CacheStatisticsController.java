package ru.itmo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.dto.response.CacheStatisticsDto;
import ru.itmo.service.CacheStatisticsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheStatisticsController {

    private final CacheStatisticsService cacheStatisticsService;

    public CacheStatisticsController(CacheStatisticsService cacheStatisticsService) {
        this.cacheStatisticsService = cacheStatisticsService;
    }

    @PostMapping("/logging/enable")
    public ResponseEntity<Map<String, Object>> enableLogging() {
        cacheStatisticsService.enableLogging();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "L2 Cache statistics logging enabled");
        response.put("loggingEnabled", true);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logging/disable")
    public ResponseEntity<Map<String, Object>> disableLogging() {
        cacheStatisticsService.disableLogging();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "L2 Cache statistics logging disabled");
        response.put("loggingEnabled", false);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/logging/status")
    public ResponseEntity<Map<String, Object>> getLoggingStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("loggingEnabled", cacheStatisticsService.isLoggingEnabled());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        CacheStatisticsDto stats = cacheStatisticsService.getCurrentStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("secondLevelCache", Map.of(
                "hits", stats.getSecondLevelCacheHits(),
                "misses", stats.getSecondLevelCacheMisses(),
                "puts", stats.getSecondLevelCachePuts(),
                "hitRatio", String.format("%.2f%%", stats.getSecondLevelCacheHitRatio())
        ));
        response.put("queryCache", Map.of(
                "hits", stats.getQueryCacheHits(),
                "misses", stats.getQueryCacheMisses(),
                "puts", stats.getQueryCachePuts(),
                "hitRatio", String.format("%.2f%%", stats.getQueryCacheHitRatio())
        ));
        response.put("loggingEnabled", cacheStatisticsService.isLoggingEnabled());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/statistics/reset")
    public ResponseEntity<Map<String, Object>> resetStatistics() {
        cacheStatisticsService.resetStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "L2 Cache statistics have been reset");

        return ResponseEntity.ok(response);
    }
}

