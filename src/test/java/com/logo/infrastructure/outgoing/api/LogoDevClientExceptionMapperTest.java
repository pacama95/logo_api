package com.logo.infrastructure.outgoing.api;

import com.logo.domain.exception.ServiceException;
import com.logo.domain.model.Errors;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogoDevClientExceptionMapperTest {

    private LogoDevClientExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LogoDevClientExceptionMapper();
    }

    @Test
    void shouldMapNotFoundError() {
        // Given
        Response response = mockResponse(404, "Not Found");

        // When
        ServiceException exception = mapper.toThrowable(response);

        // Then
        assertThat(exception.getError()).isEqualTo(Errors.LOGO_NOT_FOUND);
        assertThat(exception.getDetails()).contains("No logo found for the requested ticker symbol");
    }

    @Test
    void shouldMapUnauthorizedError() {
        // Given
        Response response = mockResponse(401, "Unauthorized");

        // When
        ServiceException exception = mapper.toThrowable(response);

        // Then
        assertThat(exception.getError()).isEqualTo(Errors.ACCESS_DENIED);
        assertThat(exception.getDetails()).contains("Invalid or missing authentication token");
    }

    @Test
    void shouldMapBadRequestError() {
        // Given
        Response response = mockResponse(400, "Bad Request");

        // When
        ServiceException exception = mapper.toThrowable(response);

        // Then
        assertThat(exception.getError()).isEqualTo(Errors.INVALID_REQUEST);
        assertThat(exception.getDetails()).contains("Invalid ticker symbol or request parameters");
    }

    @Test
    void shouldMapRateLimitError() {
        // Given
        Response response = mockResponse(429, "Too Many Requests");

        // When
        ServiceException exception = mapper.toThrowable(response);

        // Then
        assertThat(exception.getError()).isEqualTo(Errors.RATE_LIMITED);
        assertThat(exception.getDetails()).contains("API rate limit exceeded");
    }

    @Test
    void shouldMapInternalServerError() {
        // Given
        Response response = mockResponse(500, "Internal Server Error");

        // When
        ServiceException exception = mapper.toThrowable(response);

        // Then
        assertThat(exception.getError()).isEqualTo(Errors.EXTERNAL_SERVICE_ERROR);
        assertThat(exception.getDetails()).contains("Logo service internal server error");
    }

    @Test
    void shouldMapServiceUnavailable() {
        // Given
        Response response = mockResponse(503, "Service Unavailable");

        // When
        ServiceException exception = mapper.toThrowable(response);

        // Then
        assertThat(exception.getError()).isEqualTo(Errors.SERVICE_UNAVAILABLE);
        assertThat(exception.getDetails()).contains("Logo service is temporarily unavailable");
    }

    @Test
    void shouldHandleErrorStatusCodes() {
        // Then
        assertThat(mapper.handles(400, new MultivaluedHashMap<>())).isTrue();
        assertThat(mapper.handles(404, new MultivaluedHashMap<>())).isTrue();
        assertThat(mapper.handles(500, new MultivaluedHashMap<>())).isTrue();
        assertThat(mapper.handles(200, new MultivaluedHashMap<>())).isFalse();
        assertThat(mapper.handles(201, new MultivaluedHashMap<>())).isFalse();
        assertThat(mapper.handles(300, new MultivaluedHashMap<>())).isFalse();
    }

    @Test
    void shouldMapGeneric4xxError() {
        // Given
        Response response = mockResponse(418, "I'm a teapot"); // Unusual 4xx code

        // When
        ServiceException exception = mapper.toThrowable(response);

        // Then
        assertThat(exception.getError()).isEqualTo(Errors.INVALID_REQUEST);
        assertThat(exception.getDetails()).contains("Client error (HTTP 418)");
    }

    @Test
    void shouldMapGeneric5xxError() {
        // Given
        Response response = mockResponse(520, "Unknown Error"); // Unusual 5xx code

        // When
        ServiceException exception = mapper.toThrowable(response);

        // Then
        assertThat(exception.getError()).isEqualTo(Errors.EXTERNAL_SERVICE_ERROR);
        assertThat(exception.getDetails()).contains("Server error (HTTP 520)");
    }

    private Response mockResponse(int status, String reasonPhrase) {
        Response response = mock(Response.class);
        Response.StatusType statusType = mock(Response.StatusType.class);
        
        when(response.getStatus()).thenReturn(status);
        when(response.getStatusInfo()).thenReturn(statusType);
        when(statusType.getReasonPhrase()).thenReturn(reasonPhrase);
        
        return response;
    }
}
