package ru.itmo.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ImportOperation {
    private Long id;
    private String status;
    private Integer addedCount;
    private String message;
    private LocalDateTime timestamp;
    private String filename;
    private String s3key;

    public ImportOperation() {
        this.timestamp = LocalDateTime.now();
    }
}
