package com.logo.application.create;

import com.logo.domain.model.Logo;
import com.logo.domain.port.incoming.CreateLogoCommand;
import com.logo.domain.port.incoming.CreateLogoUseCase;
import com.logo.domain.port.outgoing.LogoApiPort;
import com.logo.domain.port.outgoing.LogoPersistencePort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateLogoServiceTest {

    private final LogoPersistencePort logoPersistencePort = mock(LogoPersistencePort.class);

    private final LogoApiPort logoApiPort = mock(LogoApiPort.class);

    private final CreateLogoService createLogoService = new CreateLogoService(logoPersistencePort, logoApiPort);

    private CreateLogoCommand createLogoCommand;
    private Logo existingLogo;
    private Logo newLogo;
    private byte[] logoData;

    @BeforeEach
    void setUp() {
        createLogoCommand = new CreateLogoCommand("AMZN");

        OffsetDateTime now = OffsetDateTime.now();
        existingLogo = new Logo("AMZN", "https://img.logo.dev/ticker/AMZN",
                "existing".getBytes(), "AMZN.jpeg", "image/jpeg", now, now);

        logoData = "logo-data".getBytes();
        newLogo = new Logo("AMZN", "https://img.logo.dev/ticker/AMZN",
                logoData, "AMZN.jpeg", "image/jpeg", now, now);
    }

    @Test
    void shouldReturnExistingLogoWhenFound() {
        // Given
        when(logoPersistencePort.get("AMZN")).thenReturn(Uni.createFrom().item(existingLogo));

        // When
        CreateLogoUseCase.Result result = createLogoService.execute(createLogoCommand)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(CreateLogoUseCase.Result.Success.class);
        CreateLogoUseCase.Result.Success success = (CreateLogoUseCase.Result.Success) result;
        assertThat(success.logo().externalIdentifier()).isEqualTo("AMZN");

        verify(logoApiPort, never()).fetchLogo(any());
        verify(logoPersistencePort, never()).save(any());
    }

    @Test
    void shouldFetchAndCreateLogoWhenNotFound() {
        // Given
        when(logoPersistencePort.get("AMZN")).thenReturn(Uni.createFrom().nullItem());
        when(logoApiPort.fetchLogo("AMZN")).thenReturn(Uni.createFrom().item(logoData));
        when(logoPersistencePort.save(any(Logo.class))).thenReturn(Uni.createFrom().item(newLogo));

        // When
        CreateLogoUseCase.Result result = createLogoService.execute(createLogoCommand)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(CreateLogoUseCase.Result.Success.class);
        CreateLogoUseCase.Result.Success success = (CreateLogoUseCase.Result.Success) result;
        assertThat(success.logo().externalIdentifier()).isEqualTo("AMZN");

        verify(logoApiPort).fetchLogo("AMZN");
        verify(logoPersistencePort).save(any(Logo.class));
    }

    @Test
    void shouldReturnErrorWhenApiCallFails() {
        // Given
        when(logoPersistencePort.get("AMZN")).thenReturn(Uni.createFrom().nullItem());
        when(logoApiPort.fetchLogo("AMZN")).thenReturn(Uni.createFrom().failure(new RuntimeException("API Error")));

        // When
        CreateLogoUseCase.Result result = createLogoService.execute(createLogoCommand)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(CreateLogoUseCase.Result.Error.class);
        CreateLogoUseCase.Result.Error error = (CreateLogoUseCase.Result.Error) result;
        assertThat(error.code()).isEqualTo(500);
    }

    @Test
    void shouldReturnErrorWhenPersistenceFails() {
        // Given
        when(logoPersistencePort.get("AMZN")).thenReturn(Uni.createFrom().failure(new RuntimeException("DB Error")));

        // When
        CreateLogoUseCase.Result result = createLogoService.execute(createLogoCommand)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(CreateLogoUseCase.Result.Error.class);
        CreateLogoUseCase.Result.Error error = (CreateLogoUseCase.Result.Error) result;
        assertThat(error.code()).isEqualTo(500);
    }
}
