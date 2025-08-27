package com.example.batch.exception;

public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final boolean retryable;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUS001";
        this.retryable = false;
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = false;
    }
    
    public BusinessException(String errorCode, String message, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUS001";
        this.retryable = false;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
}