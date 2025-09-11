package com.logo.infrastructure.incoming.rest.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;

@RegisterForReflection
public record CreateLogoRequest(
        @NotBlank(message = "External identifier is required")
        @Size(min = 1, max = 50, message = "External identifier must be between 1 and 50 characters")
        String externalIdentifier,

        @NotBlank(message = "Resource URL is required")
        @Size(max = 2048, message = "Resource URL must not exceed 2048 characters")
        @Pattern(regexp = "^https?://.*", message = "Resource URL must be a valid HTTP or HTTPS URL")
        String resourceUrl,

        @NotNull(message = "File content is required")
        @Size(min = 1, max = 10485760, message = "File content must be between 1 byte and 10MB")
        byte[] fileContent,

        @NotBlank(message = "File name is required")
        @Size(min = 1, max = 255, message = "File name must be between 1 and 255 characters")
        @Pattern(regexp = "^[\\w\\-. ]+\\.[a-zA-Z]{2,4}$", message = "File name must have a valid extension")
        String fileName,

        @NotBlank(message = "Content type is required")
        @Pattern(regexp = "^(image|application)/(jpeg|jpg|png|gif|svg\\+xml|pdf|octet-stream)$", 
                 message = "Content type must be a valid image or application type")
        String contentType
) {
}
