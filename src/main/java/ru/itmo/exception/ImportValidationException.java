package ru.itmo.exception;

public class ImportValidationException extends RuntimeException {
    public ImportValidationException() {
        super();
    }

    public ImportValidationException(String message) {
        super(message);
    }

    public ImportValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportValidationException(Throwable cause) {
        super(cause);
    }
}
