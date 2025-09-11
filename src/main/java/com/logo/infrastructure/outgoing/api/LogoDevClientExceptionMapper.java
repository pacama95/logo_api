package com.logo.infrastructure.outgoing.api;

import com.logo.domain.exception.ServiceException;
import com.logo.domain.model.Errors;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.logging.Logger;

/**
 * Maps HTTP status codes from the external logo service to domain-specific exceptions.
 * This ensures that HTTP details never leak into the domain layer.
 */
public class LogoDevClientExceptionMapper implements ResponseExceptionMapper<ServiceException> {
    
    private static final Logger LOG = Logger.getLogger(LogoDevClientExceptionMapper.class);
    
    @Override
    public ServiceException toThrowable(Response response) {
        int status = response.getStatus();
        String reasonPhrase = response.getStatusInfo().getReasonPhrase();
        
        // Log the HTTP error for debugging purposes
        LOG.warn("Logo service returned HTTP status %d: %s".formatted(status, reasonPhrase));
        
        // Map HTTP status codes to domain errors
        return switch (status) {
            case 400 -> new ServiceException(
                    Errors.INVALID_REQUEST, 
                    "Invalid ticker symbol or request parameters"
            );
            
            case 401 -> new ServiceException(
                    Errors.ACCESS_DENIED, 
                    "Invalid or missing authentication token"
            );
            
            case 403 -> new ServiceException(
                    Errors.ACCESS_DENIED, 
                    "Access forbidden with current credentials"
            );
            
            case 404 -> new ServiceException(
                    Errors.LOGO_NOT_FOUND, 
                    "No logo found for the requested ticker symbol"
            );
            
            case 429 -> new ServiceException(
                    Errors.RATE_LIMITED, 
                    "API rate limit exceeded"
            );
            
            case 500 -> new ServiceException(
                    Errors.EXTERNAL_SERVICE_ERROR, 
                    "Logo service internal server error"
            );
            
            case 502, 503 -> new ServiceException(
                    Errors.SERVICE_UNAVAILABLE, 
                    "Logo service is temporarily unavailable"
            );
            
            case 504 -> new ServiceException(
                    Errors.COMMUNICATION_ERROR, 
                    "Logo service request timeout"
            );
            
            default -> {
                if (status >= 400 && status < 500) {
                    yield new ServiceException(
                            Errors.INVALID_REQUEST, 
                            "Client error (HTTP " + status + "): " + reasonPhrase
                    );
                } else if (status >= 500) {
                    yield new ServiceException(
                            Errors.EXTERNAL_SERVICE_ERROR, 
                            "Server error (HTTP " + status + "): " + reasonPhrase
                    );
                } else {
                    // For any other unexpected status codes
                    yield new ServiceException(
                            Errors.COMMUNICATION_ERROR, 
                            "Unexpected response (HTTP " + status + "): " + reasonPhrase
                    );
                }
            }
        };
    }
    
    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        // Handle all HTTP error status codes (4xx and 5xx)
        return status >= 400;
    }
}
