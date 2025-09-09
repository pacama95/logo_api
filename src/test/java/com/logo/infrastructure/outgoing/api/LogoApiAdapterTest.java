package com.logo.infrastructure.outgoing.api;

import io.quarkus.test.junit.QuarkusTestProfile;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogoApiAdapterTest {

    private final LogoDevClient logoDevClient = mock(LogoDevClient.class);

    private final String logoDevToken = "test-token";

    private final LogoApiAdapter logoApiAdapter = new LogoApiAdapter(logoDevClient, logoDevToken);

    private byte[] logoData;

    public static class TestConfig implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("logo.dev.token", "test-token");
        }
    }

    @BeforeEach
    void setUp() {
        logoData = "fake-logo-data".getBytes();
    }

    @Test
    void shouldFetchLogoSuccessfully() {
        // Given
        String ticker = "AMZN";
        when(logoDevClient.fetchLogo(eq(ticker), eq("test-token")))
                .thenReturn(Uni.createFrom().item(logoData));

        // When
        byte[] result = logoApiAdapter.fetchLogo(ticker)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).hasSize(logoData.length);
    }

    @Test
    void shouldPropagateApiFailure() {
        // Given
        String ticker = "FAIL";
        RuntimeException apiException = new RuntimeException("API unavailable");
        when(logoDevClient.fetchLogo(eq(ticker), eq("test-token")))
                .thenReturn(Uni.createFrom().failure(apiException));

        // When Then
        logoApiAdapter.fetchLogo(ticker)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure()
                .assertFailedWith(RuntimeException.class);
    }
}
