package com.logo.application.create;

import com.logo.domain.model.Errors;
import com.logo.domain.model.Logo;
import com.logo.domain.port.incoming.CreateLogoCommand;
import com.logo.domain.port.incoming.CreateLogoUseCase;
import com.logo.domain.port.outgoing.LogoPersistencePort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateLogoServiceTest {

    private final LogoPersistencePort logoPersistencePort = mock(LogoPersistencePort.class);
    
    private final CreateLogoService createLogoService = new CreateLogoService(logoPersistencePort);
    
    private CreateLogoCommand createLogoCommand;
    private Logo savedLogo;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();
        
        createLogoCommand = new CreateLogoCommand(
                "AMZN",
                "https://img.logo.dev/ticker/AMZN",
                "logo-data".getBytes(),
                "AMZN.jpeg",
                "image/jpeg"
        );
        
        savedLogo = new Logo(
                "AMZN",
                "https://img.logo.dev/ticker/AMZN",
                "logo-data".getBytes(),
                "AMZN.jpeg",
                "image/jpeg",
                now,
                now
        );
    }

    @Test
    void shouldReturnSuccessWhenLogoIsCreatedSuccessfully() {
        // Given
        when(logoPersistencePort.save(any(Logo.class)))
                .thenReturn(Uni.createFrom().item(savedLogo));

        // When
        CreateLogoUseCase.Result result = createLogoService.execute(createLogoCommand)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(CreateLogoUseCase.Result.Success.class);
        CreateLogoUseCase.Result.Success success = (CreateLogoUseCase.Result.Success) result;
        assertThat(success.logo().externalIdentifier()).isEqualTo("AMZN");
        assertThat(success.logo().resourceUrl()).isEqualTo("https://img.logo.dev/ticker/AMZN");
        assertThat(success.logo().fileName()).isEqualTo("AMZN.jpeg");
        assertThat(success.logo().contentType()).isEqualTo("image/jpeg");
        assertThat(success.logo().fileContent()).isEqualTo("logo-data".getBytes());
    }

    @Test
    void shouldReturnErrorWhenPersistenceFails() {
        // Given
        when(logoPersistencePort.save(any(Logo.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When
        CreateLogoUseCase.Result result = createLogoService.execute(createLogoCommand)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isInstanceOf(CreateLogoUseCase.Result.Error.class);
        CreateLogoUseCase.Result.Error error = (CreateLogoUseCase.Result.Error) result;
        assertThat(error.error()).isEqualTo(Errors.EXTERNAL_SERVICE_ERROR);
    }
}