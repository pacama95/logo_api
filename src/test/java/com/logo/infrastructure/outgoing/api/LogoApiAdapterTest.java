package com.logo.infrastructure.outgoing.api;

import com.logo.domain.exception.ServiceException;
import com.logo.domain.model.Errors;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoApiAdapterTest {

    @Mock
    private LogoDevClient logoDevClient;

    private LogoApiAdapter logoApiAdapter;
    private final String logoDevToken = "test-token-123";
    private byte[] mockLogoData;

    @BeforeEach
    void setUp() {
        logoApiAdapter = new LogoApiAdapter(logoDevClient, logoDevToken);
        mockLogoData = "fake-logo-data".getBytes();
    }

    @Test
    void shouldFetchLogoSuccessfully() {
        // Given
        String ticker = "AAPL";
        when(logoDevClient.fetchLogo(eq(ticker), eq(logoDevToken)))
                .thenReturn(Uni.createFrom().item(mockLogoData));

        // When
        byte[] result = logoApiAdapter.fetchLogo(ticker)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isEqualTo(mockLogoData);
        verify(logoDevClient).fetchLogo(ticker, logoDevToken);
    }

    @Test
    void shouldPropagateServiceExceptionWhenLogoNotFound() {
        // Given
        String ticker = "UNKNOWN";
        ServiceException originalException = new ServiceException(
                Errors.LOGO_NOT_FOUND,
                "No logo found for ticker UNKNOWN"
        );
        when(logoDevClient.fetchLogo(eq(ticker), eq(logoDevToken)))
                .thenReturn(Uni.createFrom().failure(originalException));

        // When
        Throwable failure = logoApiAdapter.fetchLogo(ticker)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(failure).isInstanceOf(ServiceException.class);
        ServiceException serviceException = (ServiceException) failure;
        assertThat(serviceException.getError()).isEqualTo(Errors.LOGO_NOT_FOUND);
        assertThat(serviceException.getDetails()).isEqualTo("No logo found for ticker UNKNOWN");
        assertThat(serviceException).isSameAs(originalException);
        verify(logoDevClient).fetchLogo(ticker, logoDevToken);
    }

    @Test
    void shouldPropagateServiceExceptionWhenAccessDenied() {
        // Given
        String ticker = "DENIED";
        ServiceException originalException = new ServiceException(
                Errors.ACCESS_DENIED,
                "Invalid authentication token"
        );
        when(logoDevClient.fetchLogo(eq(ticker), eq(logoDevToken)))
                .thenReturn(Uni.createFrom().failure(originalException));

        // When
        Throwable failure = logoApiAdapter.fetchLogo(ticker)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(failure).isInstanceOf(ServiceException.class);
        ServiceException serviceException = (ServiceException) failure;
        assertThat(serviceException.getError()).isEqualTo(Errors.ACCESS_DENIED);
        assertThat(serviceException.getDetails()).isEqualTo("Invalid authentication token");
        assertThat(serviceException).isSameAs(originalException);
        verify(logoDevClient).fetchLogo(ticker, logoDevToken);
    }

    @Test
    void shouldPropagateServiceExceptionWhenRateLimited() {
        // Given
        String ticker = "RATE_LIMITED";
        ServiceException originalException = new ServiceException(
                Errors.RATE_LIMITED,
                "API rate limit exceeded"
        );
        when(logoDevClient.fetchLogo(eq(ticker), eq(logoDevToken)))
                .thenReturn(Uni.createFrom().failure(originalException));

        // When
        Throwable failure = logoApiAdapter.fetchLogo(ticker)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(failure).isInstanceOf(ServiceException.class);
        ServiceException serviceException = (ServiceException) failure;
        assertThat(serviceException.getError()).isEqualTo(Errors.RATE_LIMITED);
        assertThat(serviceException.getDetails()).isEqualTo("API rate limit exceeded");
        assertThat(serviceException).isSameAs(originalException);
        verify(logoDevClient).fetchLogo(ticker, logoDevToken);
    }

    @Test
    void shouldWrapUnexpectedExceptionInServiceException() {
        // Given
        String ticker = "NETWORK_ERROR";
        ConnectException unexpectedException = new ConnectException("Connection refused");
        when(logoDevClient.fetchLogo(eq(ticker), eq(logoDevToken)))
                .thenReturn(Uni.createFrom().failure(unexpectedException));

        // When
        Throwable failure = logoApiAdapter.fetchLogo(ticker)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(failure).isInstanceOf(ServiceException.class);
        ServiceException serviceException = (ServiceException) failure;
        assertThat(serviceException.getError()).isEqualTo(Errors.COMMUNICATION_ERROR);
        assertThat(serviceException.getDetails()).isEqualTo("Unexpected error communicating with logo service: Connection refused");
        assertThat(serviceException.getMessage()).contains("Connection refused");
        assertThat(serviceException.getCause()).isEqualTo(unexpectedException);
        verify(logoDevClient).fetchLogo(ticker, logoDevToken);
    }

    @Test
    void shouldNotDoubleWrapServiceExceptionWhenAlreadyWrapped() {
        // Given
        String ticker = "ALREADY_WRAPPED";
        ServiceException alreadyWrappedException = new ServiceException(
                Errors.EXTERNAL_SERVICE_ERROR,
                "Server internal error"
        );
        when(logoDevClient.fetchLogo(eq(ticker), eq(logoDevToken)))
                .thenReturn(Uni.createFrom().failure(alreadyWrappedException));

        // When
        Throwable failure = logoApiAdapter.fetchLogo(ticker)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(failure).isInstanceOf(ServiceException.class);
        ServiceException serviceException = (ServiceException) failure;
        // Should be the original exception, not double-wrapped
        assertThat(serviceException).isSameAs(alreadyWrappedException);
        assertThat(serviceException.getError()).isEqualTo(Errors.EXTERNAL_SERVICE_ERROR);
        assertThat(serviceException.getDetails()).isEqualTo("Server internal error");
        verify(logoDevClient).fetchLogo(ticker, logoDevToken);
    }
}
