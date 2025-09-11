package com.logo.infrastructure.incoming.rest;

import com.logo.infrastructure.incoming.rest.request.CreateLogoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateLogoRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidRequest() {
        // Given
        CreateLogoRequest request = new CreateLogoRequest(
                "AMZN",
                "https://img.logo.dev/ticker/AMZN",
                "valid-file-content".getBytes(),
                "logo.png",
                "image/png"
        );

        // When
        Set<ConstraintViolation<CreateLogoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenExternalIdentifierIsBlank() {
        // Given
        CreateLogoRequest request = new CreateLogoRequest(
                "",
                "https://img.logo.dev/ticker/AMZN",
                "valid-file-content".getBytes(),
                "logo.png",
                "image/png"
        );

        // When
        Set<ConstraintViolation<CreateLogoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // Both @NotBlank and @Size trigger
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "External identifier is required",
                        "External identifier must be between 1 and 50 characters"
                );
    }

    @Test
    void shouldFailValidationWhenResourceUrlIsInvalid() {
        // Given
        CreateLogoRequest request = new CreateLogoRequest(
                "AMZN",
                "invalid-url",
                "valid-file-content".getBytes(),
                "logo.png",
                "image/png"
        );

        // When
        Set<ConstraintViolation<CreateLogoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Resource URL must be a valid HTTP or HTTPS URL");
    }

    @Test
    void shouldFailValidationWhenFileContentIsNull() {
        // Given
        CreateLogoRequest request = new CreateLogoRequest(
                "AMZN",
                "https://img.logo.dev/ticker/AMZN",
                null,
                "logo.png",
                "image/png"
        );

        // When
        Set<ConstraintViolation<CreateLogoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("File content is required");
    }

    @Test
    void shouldFailValidationWhenFileNameIsInvalid() {
        // Given
        CreateLogoRequest request = new CreateLogoRequest(
                "AMZN",
                "https://img.logo.dev/ticker/AMZN",
                "valid-file-content".getBytes(),
                "invalid-filename-no-extension",
                "image/png"
        );

        // When
        Set<ConstraintViolation<CreateLogoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("File name must have a valid extension");
    }

    @Test
    void shouldFailValidationWhenContentTypeIsInvalid() {
        // Given
        CreateLogoRequest request = new CreateLogoRequest(
                "AMZN",
                "https://img.logo.dev/ticker/AMZN",
                "valid-file-content".getBytes(),
                "logo.png",
                "invalid/content-type"
        );

        // When
        Set<ConstraintViolation<CreateLogoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Content type must be a valid image or application type");
    }

    @Test
    void shouldFailValidationWithMultipleErrors() {
        // Given
        CreateLogoRequest request = new CreateLogoRequest(
                "", // blank external identifier (triggers 2 violations: @NotBlank + @Size)
                "invalid-url", // invalid URL
                null, // null file content
                "no-extension", // invalid file name
                "invalid/type" // invalid content type
        );

        // When
        Set<ConstraintViolation<CreateLogoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(6); // 6 violations total (external identifier triggers 2)
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "External identifier is required",
                        "External identifier must be between 1 and 50 characters",
                        "Resource URL must be a valid HTTP or HTTPS URL",
                        "File content is required",
                        "File name must have a valid extension",
                        "Content type must be a valid image or application type"
                );
    }
}
