package com.logo.infrastructure.incoming.rest;

import java.time.OffsetDateTime;

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
