package ru.itmo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.exception.DatabaseException;
import ru.itmo.exception.FileStorageException;
import ru.itmo.model.ImportOperation;
import ru.itmo.repository.ImportRepository;
import org.springframework.transaction.annotation.Isolation;

import java.util.*;

@Service
public class ImportService {
    private final ImportRepository importRepo;
    private final ImportOperationService importOperationService;
    private final FileStorageService fileStorageService;
    private final ImportProcessingService importProcessingService;

    public ImportService(ImportRepository importRepo,
                         ImportOperationService importOperationService,
                         FileStorageService fileStorageService,
                         ImportProcessingService importProcessingService) {
        this.importRepo = importRepo;
        this.importOperationService = importOperationService;
        this.fileStorageService = fileStorageService;
        this.importProcessingService = importProcessingService;
    }

    public void setSimulateBusinessError(boolean simulate) {
        importProcessingService.setSimulateBusinessError(simulate);
    }

    public ImportOperation importCities(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String s3key = null;
        ImportOperation op = null;

        try {
            op = importOperationService.createOperation(filename, null);
        } catch (Exception e) {
            throw new DatabaseException("Failed to create import operation", e);
        }

        try {
            s3key = fileStorageService.uploadFile(filename, file.getInputStream(), file.getSize());
            op.setS3key(s3key);
            importOperationService.updateS3Key(op, s3key);
        } catch (FileStorageException e) {
            importOperationService.updateStatus(op, "FAILED", 0, "File storage error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            if (s3key != null) {
                fileStorageService.deleteFile(s3key);
            }
            if (op != null) {
                importOperationService.updateStatus(op, "FAILED", 0, "File upload error: " + e.getMessage());
            }
            throw new FileStorageException("Failed to upload file", e);
        }

        return importProcessingService.processImportFile(file, s3key, op);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Map<String, Object> getImportHistory(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        long total = importRepo.countAll();
        int totalPages = (int) Math.max(1, Math.ceil((double) total / size));

        if (page >= totalPages) page = Math.max(0, totalPages - 1);

        List<ImportOperation> items = importRepo.findPaged(page, size);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("currentPage", page);
        resp.put("totalItems", total);
        resp.put("totalPages", totalPages);
        resp.put("pageSize", size);
        return resp;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ImportOperation getImportOperationById(Long id) {
        return importRepo.findById(id);
    }
}
