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
    public ImportOperation createOperation() {
        ImportOperation op = new ImportOperation();
        op.setStatus("RUNNING");
        op.setAddedCount(0);
        op.setMessage(null);
        Long id = importRepo.save(op);
        op.setId(id);
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
}

