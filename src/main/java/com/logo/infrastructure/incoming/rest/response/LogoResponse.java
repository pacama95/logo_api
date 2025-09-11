package com.logo.infrastructure.incoming.rest.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.OffsetDateTime;

@RegisterForReflection
public record LogoResponse(
        String externalIdentifier,
        String resourceUrl,
        byte[] fileContent,
        String fileName,
        String contentType,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
