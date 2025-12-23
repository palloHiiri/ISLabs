package ru.itmo.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.model.ImportOperation;
import ru.itmo.service.FileStorageService;
import ru.itmo.service.ImportService;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/cities")
public class ImportController {
    private final ImportService importService;
    private final FileStorageService fileStorageService;

    public ImportController(ImportService importService, FileStorageService fileStorageService) {
        this.importService = importService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCities(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is required"));
        }
        ImportOperation op = importService.importCities(file);
        return ResponseEntity.ok(op);
    }

    @PostMapping("/simulate-error")
    public ResponseEntity<?> simulateError(@RequestParam("enable") boolean enable) {
        importService.setSimulateBusinessError(enable);
        return ResponseEntity.ok(Map.of("simulateBusinessError", enable));
    }

    @GetMapping("/imports")
    public ResponseEntity<?> getImportHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> resp = importService.getImportHistory(page, size);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/imports/{id}/download")
    public ResponseEntity<?> downloadFile(@PathVariable("id") Long id) {
        ImportOperation op = importService.getImportOperationById(id);
        if (op == null || op.getS3key() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            InputStream is = fileStorageService.downloadFile(op.getS3key());
            InputStreamResource resource = new InputStreamResource(is);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + op.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "File storage unavailable or file not found"));
        }
    }
}
