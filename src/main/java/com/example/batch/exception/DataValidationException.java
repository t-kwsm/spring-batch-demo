package com.example.batch.exception;

import java.util.List;

public class DataValidationException extends RuntimeException {
    
    private final List<String> validationErrors;
    
    public DataValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
    
    public DataValidationException(String message, List<String> validationErrors, Throwable cause) {
        super(message, cause);
        this.validationErrors = validationErrors;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
    
    @Override
    public String getMessage() {
        if (validationErrors != null && !validationErrors.isEmpty()) {
            return super.getMessage() + " - Errors: " + String.join(", ", validationErrors);
        }
        return super.getMessage();
    }
}