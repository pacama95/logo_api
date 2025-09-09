package com.logo.infrastructure.incoming.rest.mapper;

import com.logo.domain.model.Logo;
import com.logo.domain.port.incoming.CreateLogoCommand;
import com.logo.infrastructure.incoming.rest.CreateLogoRequest;
import com.logo.infrastructure.incoming.rest.LogoResponse;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LogoDtoMapperTest {

    LogoDtoMapper logoDtoMapper = Mappers.getMapper(LogoDtoMapper.class);

    private CreateLogoRequest createLogoRequest;
    private Logo domainLogo;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = OffsetDateTime.now();
        
        createLogoRequest = new CreateLogoRequest("AMZN");

        domainLogo = new Logo(
                "AMZN",
                "https://img.logo.dev/ticker/AMZN",
                "logo-data".getBytes(),
                "AMZN.jpeg",
                "image/jpeg",
                testTime,
                testTime
        );
    }

    @Test
    void shouldMapCreateRequestToCommand() {
        // When
        CreateLogoCommand result = logoDtoMapper.toCommand(createLogoRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.ticker()).isEqualTo(createLogoRequest.ticker());
    }

    @Test
    void shouldMapDomainLogoToResponse() {
        // When
        LogoResponse result = logoDtoMapper.toResponse(domainLogo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.externalIdentifier()).isEqualTo(domainLogo.externalIdentifier());
        assertThat(result.resourceUrl()).isEqualTo(domainLogo.resourceUrl());
        assertThat(result.fileContent()).isEqualTo(domainLogo.fileContent());
        assertThat(result.fileName()).isEqualTo(domainLogo.fileName());
        assertThat(result.contentType()).isEqualTo(domainLogo.contentType());
        assertThat(result.createdAt()).isEqualTo(domainLogo.createdAt());
        assertThat(result.updatedAt()).isEqualTo(domainLogo.updatedAt());
    }
}
