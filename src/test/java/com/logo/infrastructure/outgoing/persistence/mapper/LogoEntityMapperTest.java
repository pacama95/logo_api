package com.logo.infrastructure.outgoing.persistence.mapper;

import com.logo.domain.model.Logo;
import com.logo.infrastructure.outgoing.persistence.LogoEntity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LogoEntityMapperTest {

    LogoEntityMapper logoEntityMapper = Mappers.getMapper(LogoEntityMapper.class);

    private Logo domainLogo;
    private LogoEntity logoEntity;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = OffsetDateTime.now();
        
        domainLogo = new Logo(
                "AMZN",
                "https://img.logo.dev/ticker/AMZN",
                "logo-data".getBytes(),
                "AMZN.jpeg",
                "image/jpeg",
                testTime,
                testTime
        );

        logoEntity = new LogoEntity();
        logoEntity.id = 1L;
        logoEntity.externalIdentifier = "AMZN";
        logoEntity.resourceUrl = "https://img.logo.dev/ticker/AMZN";
        logoEntity.fileContent = "logo-data".getBytes();
        logoEntity.fileName = "AMZN.jpeg";
        logoEntity.contentType = "image/jpeg";
        logoEntity.createdAt = testTime;
        logoEntity.updatedAt = testTime;
    }

    @Test
    void shouldMapDomainLogoToEntity() {
        // When
        LogoEntity result = logoEntityMapper.toEntity(domainLogo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.externalIdentifier).isEqualTo(domainLogo.externalIdentifier());
        assertThat(result.resourceUrl).isEqualTo(domainLogo.resourceUrl());
        assertThat(result.fileContent).isEqualTo(domainLogo.fileContent());
        assertThat(result.fileName).isEqualTo(domainLogo.fileName());
        assertThat(result.contentType).isEqualTo(domainLogo.contentType());
        assertThat(result.createdAt).isEqualTo(domainLogo.createdAt());
        assertThat(result.updatedAt).isEqualTo(domainLogo.updatedAt());
        
        // ID should not be mapped (ignored)
        assertThat(result.id).isNull();
    }

    @Test
    void shouldMapEntityToDomainLogo() {
        // When
        Logo result = logoEntityMapper.toDomain(logoEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.externalIdentifier()).isEqualTo(logoEntity.externalIdentifier);
        assertThat(result.resourceUrl()).isEqualTo(logoEntity.resourceUrl);
        assertThat(result.fileContent()).isEqualTo(logoEntity.fileContent);
        assertThat(result.fileName()).isEqualTo(logoEntity.fileName);
        assertThat(result.contentType()).isEqualTo(logoEntity.contentType);
        assertThat(result.createdAt()).isEqualTo(logoEntity.createdAt);
        assertThat(result.updatedAt()).isEqualTo(logoEntity.updatedAt);
    }

    @Test
    void shouldHandleNullInput() {
        // When mapping null domain logo
        LogoEntity entityResult = logoEntityMapper.toEntity(null);
        
        // When mapping null entity
        Logo domainResult = logoEntityMapper.toDomain(null);

        // Then
        assertThat(entityResult).isNull();
        assertThat(domainResult).isNull();
    }

    @Test
    void shouldHandleEmptyByteArray() {
        // Given
        Logo logoWithEmptyContent = new Logo(
                "TEST",
                "https://test.com",
                new byte[0],
                "test.jpeg",
                "image/jpeg",
                testTime,
                testTime
        );

        // When
        LogoEntity result = logoEntityMapper.toEntity(logoWithEmptyContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.fileContent).isNotNull();
        assertThat(result.fileContent).hasSize(0);
    }
}
