package com.logo.domain.model;

import java.time.OffsetDateTime;

public record Logo(
        String externalIdentifier,
        String resourceUrl,
        byte[] fileContent,
        String fileName,
        String contentType,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
