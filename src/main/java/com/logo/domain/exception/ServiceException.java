package com.logo.domain.exception;

import com.logo.domain.model.Errors;

/**
 * Domain exception for external service errors.
 * This exception abstracts HTTP status codes and provides domain-specific error information.
 */
public class ServiceException extends RuntimeException {
    
    private final Errors error;
    private final String details;
    
    public ServiceException(Errors error) {
        super(error.getMessage());
        this.error = error;
        this.details = null;
    }
    
    public ServiceException(Errors error, String details) {
        super(error.getMessage() + (details != null ? ": " + details : ""));
        this.error = error;
        this.details = details;
    }
    
    public ServiceException(Errors error, String details, Throwable cause) {
        super(error.getMessage() + (details != null ? ": " + details : ""), cause);
        this.error = error;
        this.details = details;
    }
    
    public Errors getError() {
        return error;
    }
    
    public String getDetails() {
        return details;
    }
    
    @Override
    public String toString() {
        return "ServiceException{" +
                "error=" + error +
                ", details='" + details + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
