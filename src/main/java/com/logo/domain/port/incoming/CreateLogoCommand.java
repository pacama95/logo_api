package com.logo.domain.port.incoming;

public record CreateLogoCommand(
        String externalIdentifier,
        String resourceUrl,
        byte[] fileContent,
        String fileName,
        String contentType
) {
}
