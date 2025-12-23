package ru.itmo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.model.ImportOperation;
import ru.itmo.repository.ImportRepository;

@Service
public class ImportOperationService {
    private final ImportRepository importRepo;

    public ImportOperationService(ImportRepository importRepo) {
        this.importRepo = importRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportOperation createOperation(String fileName, String s3key) {
        ImportOperation op = new ImportOperation();
        op.setStatus("RUNNING");
        op.setAddedCount(0);
        op.setMessage(null);
        Long id = importRepo.save(op);
        op.setId(id);
        op.setS3key(s3key);
        op.setFilename(fileName);
        importRepo.update(op);
        return op;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportOperation updateStatus(ImportOperation op, String status,
                                        int addedCount, String message) {
        op.setStatus(status);
        op.setAddedCount(addedCount);
        op.setMessage(message);
        importRepo.update(op);
        return op;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportOperation updateS3Key(ImportOperation op, String s3key) {
        op.setS3key(s3key);
        importRepo.update(op);
        return op;
    }
}
