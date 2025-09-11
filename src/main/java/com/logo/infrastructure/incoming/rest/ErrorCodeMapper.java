package com.logo.infrastructure.incoming.rest;

import com.logo.domain.model.Errors;

/**
 * Maps domain errors to HTTP status codes.
 * This ensures domain errors are properly translated to HTTP responses.
 */
public class ErrorCodeMapper {
    
    public static int toHttpStatus(Errors error) {
        return switch (error) {
            case LOGO_NOT_FOUND -> 404;
            case ACCESS_DENIED -> 403;
            case INVALID_REQUEST -> 400;
            case RATE_LIMITED -> 429;
            case SERVICE_UNAVAILABLE -> 503;
            case COMMUNICATION_ERROR -> 502;
            case EXTERNAL_SERVICE_ERROR -> 500;
        };
    }
}
