package com.logo.infrastructure.outgoing.persistence;

import com.logo.domain.model.Logo;
import com.logo.infrastructure.outgoing.persistence.mapper.LogoEntityMapper;
import com.logo.infrastructure.outgoing.persistence.repository.LogoRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LogoPersistenceAdapterTest {

    private final LogoRepository logoRepository = mock(LogoRepository.class);

    private final LogoEntityMapper logoEntityMapper = mock(LogoEntityMapper.class);

    private final LogoPersistenceAdapter logoPersistenceAdapter = new LogoPersistenceAdapter(logoRepository, logoEntityMapper);

    private Logo domainLogo;
    private LogoEntity logoEntity;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();
        domainLogo = new Logo("AMZN", "https://img.logo.dev/ticker/AMZN",
                "logo-data".getBytes(), "AMZN.jpeg", "image/jpeg", now, now);

        logoEntity = new LogoEntity();
        logoEntity.externalIdentifier = "AMZN";
        logoEntity.resourceUrl = "https://img.logo.dev/ticker/AMZN";
        logoEntity.fileContent = "logo-data".getBytes();
        logoEntity.fileName = "AMZN.jpeg";
        logoEntity.contentType = "image/jpeg";
        logoEntity.createdAt = now;
        logoEntity.updatedAt = now;
    }

    @Test
    void shouldSaveLogoSuccessfully() {
        // Given
        when(logoEntityMapper.toEntity(domainLogo)).thenReturn(logoEntity);
        when(logoRepository.persistAndFlush(logoEntity)).thenReturn(Uni.createFrom().item(logoEntity));
        when(logoEntityMapper.toDomain(logoEntity)).thenReturn(domainLogo);

        // When
        Logo result = logoPersistenceAdapter.save(domainLogo)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result.externalIdentifier()).isEqualTo("AMZN");

        verify(logoEntityMapper).toEntity(domainLogo);
        verify(logoRepository).persistAndFlush(logoEntity);
        verify(logoEntityMapper).toDomain(logoEntity);
    }

    @Test
    void shouldGetLogoByExternalIdentifierSuccessfully() {
        // Given
        when(logoRepository.findByExternalIdentifier("AMZN")).thenReturn(Uni.createFrom().item(logoEntity));
        when(logoEntityMapper.toDomain(logoEntity)).thenReturn(domainLogo);

        // When
        Logo result = logoPersistenceAdapter.get("AMZN")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result.externalIdentifier()).isEqualTo("AMZN");

        verify(logoRepository).findByExternalIdentifier("AMZN");
        verify(logoEntityMapper).toDomain(logoEntity);
    }

    @Test
    void shouldReturnNullWhenLogoNotFound() {
        // Given
        when(logoRepository.findByExternalIdentifier("NOTFOUND")).thenReturn(Uni.createFrom().nullItem());

        // When
        Logo result = logoPersistenceAdapter.get("NOTFOUND")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertThat(result).isNull();

        verify(logoRepository).findByExternalIdentifier("NOTFOUND");
    }

    @Test
    void shouldPropagateRepositoryFailure() {
        // Given
        RuntimeException dbException = new RuntimeException("Database error");
        when(logoRepository.findByExternalIdentifier("ERROR")).thenReturn(Uni.createFrom().failure(dbException));

        // When
        Uni<Logo> result = logoPersistenceAdapter.get("ERROR");

        // Then
        assertThatThrownBy(() -> result.await().indefinitely())
                .isInstanceOf(RuntimeException.class);
    }
}
