package ru.itmo.controller;

import ru.itmo.model.Human;
import ru.itmo.service.HumanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/humans")
public class HumanController {
    private final HumanService humanService;

    public HumanController(HumanService humanService) {
        this.humanService = humanService;
    }

    @GetMapping("/")
    public ResponseEntity<List<Human>> getAllHumans() {
        List<Human> humans = humanService.getAllHumans();
        return ResponseEntity.ok(humans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHumanById(@PathVariable Long id) {
        try {
            Human human = humanService.getHumanById(id);
            if (human != null) {
                return ResponseEntity.ok(human);
            } else {
                return createErrorResponse("Human not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve human: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> addHuman(@RequestBody Human human) {
        try {
            Long id = humanService.addHuman(human);
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            return createErrorResponse("Failed to add human: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHuman(@PathVariable Long id, @RequestBody Human human) {
        try {
            human.setId(id);
            humanService.updateHuman(human);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return createErrorResponse("Failed to update human: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHuman(@PathVariable Long id) {
        try {
            Human human = humanService.getHumanById(id);
            if (human != null) {
                humanService.deleteHuman(human);
                return ResponseEntity.ok().build();
            } else {
                return createErrorResponse("Human not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return createErrorResponse("Failed to delete human: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", true,
                "message", message,
                "status", status.value(),
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}
