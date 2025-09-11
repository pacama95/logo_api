package com.logo.domain.model;

/**
 * Domain-specific errors that abstract away HTTP status codes.
 * These represent business errors that can occur in the domain.
 */
public enum Errors {
    
    /**
     * The requested resource was not found in the external service
     */
    LOGO_NOT_FOUND("The requested logo was not found"),
    
    /**
     * The external service is temporarily unavailable
     */
    SERVICE_UNAVAILABLE("The logo service is temporarily unavailable"),
    
    /**
     * The request was rejected due to invalid parameters
     */
    INVALID_REQUEST("The request parameters are invalid"),
    
    /**
     * Access to the resource was denied (authentication/authorization issues)
     */
    ACCESS_DENIED("Access to the logo service was denied"),
    
    /**
     * The external service returned an unexpected error
     */
    EXTERNAL_SERVICE_ERROR("An unexpected error occurred with the logo service"),
    
    /**
     * Request limit exceeded or rate limiting applied
     */
    RATE_LIMITED("Too many requests to the logo service"),
    
    /**
     * Network or communication error
     */
    COMMUNICATION_ERROR("Unable to communicate with the logo service");
    
    private final String message;
    
    Errors(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}
