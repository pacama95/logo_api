package com.logo.application.get;

import com.logo.domain.model.Errors;
import com.logo.domain.model.Logo;
import com.logo.domain.port.incoming.GetLogoUseCase;
import com.logo.domain.port.outgoing.LogoApiPort;
import com.logo.domain.port.outgoing.LogoPersistencePort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetLogoServiceTest {

    private final LogoPersistencePort logoPersistencePort = mock(LogoPersistencePort.class);
    private final LogoApiPort logoApiPort = mock(LogoApiPort.class);

    private final GetLogoService getLogoService = new GetLogoService(logoPersistencePort, logoApiPort);

    private Logo existingLogo;
    private byte[] logoData;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();
        existingLogo = new Logo("AMZN", "https://img.logo.dev/ticker/AMZN",
                "logo-data".getBytes(), "AMZN.jpeg", "image/jpeg", now, now);
        logoData = "new-logo-data".getBytes();
    }

    @Test
    void shouldReturnSuccessWhenLogoExists() {
        // Given
        when(logoPersistencePort.get("AMZN")).thenReturn(Uni.createFrom().item(existingLogo));

        // When
        GetLogoUseCase.Result result = getLogoService.execute("AMZN")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetLogoUseCase.Result.Success.class);
        GetLogoUseCase.Result.Success success = (GetLogoUseCase.Result.Success) result;
        assertThat(success.logo().externalIdentifier()).isEqualTo("AMZN");
    }

    @Test
    void shouldFetchAndSaveLogoWhenNotFoundInDatabase() {
        // Given
        Logo newLogo = new Logo("TSLA", "https://img.logo.dev/ticker/TSLA",
                logoData, "TSLA.jpeg", "image/jpeg", OffsetDateTime.now(), OffsetDateTime.now());
        
        when(logoPersistencePort.get("TSLA")).thenReturn(Uni.createFrom().nullItem());
        when(logoApiPort.fetchLogo("TSLA")).thenReturn(Uni.createFrom().item(logoData));
        when(logoPersistencePort.save(any(Logo.class))).thenReturn(Uni.createFrom().item(newLogo));

        // When
        GetLogoUseCase.Result result = getLogoService.execute("TSLA")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetLogoUseCase.Result.Success.class);
        GetLogoUseCase.Result.Success success = (GetLogoUseCase.Result.Success) result;
        assertThat(success.logo().externalIdentifier()).isEqualTo("TSLA");
        
        verify(logoPersistencePort).get("TSLA");
        verify(logoApiPort).fetchLogo("TSLA");
        verify(logoPersistencePort).save(any(Logo.class));
    }

    @Test
    void shouldReturnErrorWhenApiFails() {
        // Given
        when(logoPersistencePort.get("APIERROR")).thenReturn(Uni.createFrom().nullItem());
        when(logoApiPort.fetchLogo("APIERROR")).thenReturn(Uni.createFrom().failure(new RuntimeException("API Error")));

        // When
        GetLogoUseCase.Result result = getLogoService.execute("APIERROR")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetLogoUseCase.Result.Error.class);
        GetLogoUseCase.Result.Error error = (GetLogoUseCase.Result.Error) result;
        assertThat(error.errors()).isEqualTo(Errors.EXTERNAL_SERVICE_ERROR);
    }

    @Test
    void shouldReturnErrorWhenPersistenceFails() {
        // Given
        when(logoPersistencePort.get("ERROR")).thenReturn(Uni.createFrom().failure(new RuntimeException("DB Error")));

        // When
        GetLogoUseCase.Result result = getLogoService.execute("ERROR")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetLogoUseCase.Result.Error.class);
        GetLogoUseCase.Result.Error error = (GetLogoUseCase.Result.Error) result;
        assertThat(error.errors()).isEqualTo(Errors.EXTERNAL_SERVICE_ERROR);
    }
}
