package com.logo.infrastructure.incoming.rest.request;

public record CreateLogoRequest(
        String externalIdentifier,
        String resourceUrl,
        byte[] fileContent,
        String fileName,
        String contentType
) {
}
