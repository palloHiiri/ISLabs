package ru.itmo.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.hibernate.exception.JDBCConnectionException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return createErrorResponse("Validation failed: " + errors.toString(), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImportValidationException.class)
    public ResponseEntity<Map<String, Object>> handleImportValidationException(ImportValidationException ex){
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorageException(FileStorageException ex){
        return createErrorResponse("File storage unavailable: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseException(DatabaseException ex){
        return createErrorResponse("Database error: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex){
        return createErrorResponse("Database unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(JDBCConnectionException.class)
    public ResponseEntity<Map<String, Object>> handleJDBCConnectionException(JDBCConnectionException ex){
        return createErrorResponse("Database connection failed", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<Map<String, Object>> handleCannotCreateTransactionException(CannotCreateTransactionException ex){
        return createErrorResponse("Database unavailable: cannot create transaction", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException(SQLException ex){
        return createErrorResponse("Database error", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex){
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof SQLException || cause instanceof JDBCConnectionException) {
                return createErrorResponse("Database unavailable", HttpStatus.SERVICE_UNAVAILABLE);
            }
            cause = cause.getCause();
        }
        return createErrorResponse("Internal server error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
