// java
// File: src/main/java/ru/itmo/controller/ImportController.java
package ru.itmo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.model.ImportOperation;
import ru.itmo.service.ImportService;

import java.util.Map;

@RestController
@RequestMapping("/api/cities")
public class ImportController {
    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCities(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is required"));
        }
        ImportOperation op = importService.importCities(file);
        return ResponseEntity.ok(op);
    }

    @GetMapping("/imports")
    public ResponseEntity<?> getImportHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> resp = importService.getImportHistory(page, size);
        return ResponseEntity.ok(resp);
    }
}
