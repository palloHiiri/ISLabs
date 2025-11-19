// java
package ru.itmo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.dto.request.CityRequestDto;
import ru.itmo.model.ImportOperation;
import ru.itmo.repository.ImportOperationRepository;

import java.io.InputStream;
import java.util.List;

@Service
public class ImportService {
    private final ObjectMapper objectMapper;
    private final ImportOperationRepository importRepo;
    private final CityService cityService;
    private final TransactionTemplate requiresNewTx;

    public ImportService(ObjectMapper objectMapper,
                         ImportOperationRepository importRepo,
                         CityService cityService,
                         PlatformTransactionManager transactionManager) {
        this.objectMapper = objectMapper;
        this.importRepo = importRepo;
        this.cityService = cityService;
        this.requiresNewTx = new TransactionTemplate(transactionManager);
        this.requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional
    public ImportOperation importCities(MultipartFile file) {
        ImportOperation op = new ImportOperation();
        op.setStatus("RUNNING");
        op.setAddedCount(0);
        op.setMessage(null);
        Long id = importRepo.save(op);
        op.setId(id);

        int successCount = 0;
        StringBuilder msg = new StringBuilder();

        try (InputStream is = file.getInputStream()) {
            List<CityRequestDto> list = objectMapper.readValue(is, new TypeReference<>() {});
            if (list == null || list.isEmpty()) {
                op.setStatus("FAILED");
                op.setMessage("Empty or invalid JSON array");
                op.setAddedCount(0);
                importRepo.save(op);
                return op;
            }

            for (int i = 0; i < list.size(); i++) {
                CityRequestDto dto = list.get(i);
                try {
                    var entity = cityService.mapRequestToEntity(dto);
                    requiresNewTx.execute(status -> {
                        cityService.addCity(entity);
                        return null;
                    });
                    successCount++;
                } catch (Exception ex) {
                    msg.append("item ").append(i).append(": ").append(ex.getMessage()).append("; ");
                }
            }

            if (successCount == list.size()) op.setStatus("SUCCESS");
            else if (successCount == 0) op.setStatus("FAILED");
            else op.setStatus("PARTIAL");

            op.setAddedCount(successCount);
            if (msg.length() > 0) op.setMessage(msg.toString());
            importRepo.save(op);
            return op;
        } catch (Exception e) {
            op.setStatus("FAILED");
            op.setAddedCount(0);
            op.setMessage("Import error: " + e.getMessage());
            importRepo.save(op);
            return op;
        }
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getImportHistory(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10; // приводим к default, согласованному с контроллером / фронтом

        long total = importRepo.countAll();
        int totalPages = (int) Math.max(1, Math.ceil((double) total / size));

        // если запрошена страница вне диапазона — вернуть последнюю существующую
        if (page >= totalPages) page = Math.max(0, totalPages - 1);

        List<ImportOperation> items = importRepo.findPaged(page, size);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("items", items);
        resp.put("currentPage", page);
        resp.put("totalItems", total);
        resp.put("totalPages", totalPages);
        resp.put("pageSize", size);
        return resp;
    }
}
