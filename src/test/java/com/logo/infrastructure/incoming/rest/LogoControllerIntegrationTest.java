package com.logo.infrastructure.incoming.rest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Base64;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestProfile(LogoControllerIntegrationTest.SimpleIntegrationTestProfile.class)
class LogoControllerIntegrationTest {

    private static WireMockServer wireMockServer;

    public static class SimpleIntegrationTestProfile implements io.quarkus.test.junit.QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.rest-client.logo-dev-api.url", "http://localhost:8090",
                    "logo.dev.token", "test-token",
                    // Use PostgreSQL with dev services (test containers)
                    "quarkus.datasource.devservices.enabled", "true",
                    "quarkus.hibernate-orm.database.generation", "drop-and-create",
                    "quarkus.liquibase.migrate-at-start", "false"
            );
        }
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8090));
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("Should successfully create a logo with valid request")
    void shouldCreateLogoSuccessfully() {
        // Given
        String logoData = Base64.getEncoder().encodeToString("test-logo-content".getBytes());
        String requestBody = """
                {
                    "externalIdentifier": "AAPL",
                    "resourceUrl": "https://img.logo.dev/ticker/AAPL",
                    "fileContent": "%s",
                    "fileName": "AAPL.png",
                    "contentType": "image/png"
                }
                """.formatted(logoData);

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/logos")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("externalIdentifier", equalTo("AAPL"))
                .body("resourceUrl", equalTo("https://img.logo.dev/ticker/AAPL"))
                .body("fileName", equalTo("AAPL.png"))
                .body("contentType", equalTo("image/png"))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test
    @DisplayName("Should return validation errors for invalid request")
    void shouldReturnValidationErrors() {
        // Given - Invalid request with empty fields
        String invalidRequest = """
                {
                    "externalIdentifier": "",
                    "resourceUrl": "not-a-url",
                    "fileContent": null,
                    "fileName": "invalid-name",
                    "contentType": "invalid/content-type"
                }
                """;

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/v1/logos")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should fetch logo from external API when not in database")
    void shouldFetchLogoFromExternalApi() {
        // Given
        String mockLogoData = "external-api-logo-content";
        wireMockServer.stubFor(get(urlEqualTo("/ticker/MSFT?token=test-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "image/jpeg")
                        .withBody(mockLogoData.getBytes())));

        // When & Then
        given()
                .when()
                .get("/api/v1/logos/external/MSFT")
                .then()
                .statusCode(200)
                .header("Content-Type", "image/jpeg")
                .header("Content-Disposition", containsString("MSFT.jpeg"))
                .body(equalTo(mockLogoData));

        // Verify external API was called
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/ticker/MSFT?token=test-token")));
    }

    @Test
    @DisplayName("Should return 500 when external API fails")
    void shouldReturn500WhenExternalApiFails() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/ticker/UNKNOWN?token=test-token"))
                .willReturn(aResponse().withStatus(500)));

        // When & Then - Currently returns 500 due to error handling
        given()
                .when()
                .get("/api/v1/logos/external/UNKNOWN")
                .then()
                .statusCode(500);

        // Verify external API was called
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/ticker/UNKNOWN?token=test-token")));
    }

    @Test
    @DisplayName("Should return 404 when logo not found in external API")
    void shouldReturn404WhenLogoNotFoundInExternalApi() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/ticker/UNKNOWN?token=test-token"))
                .willReturn(aResponse().withStatus(404)));

        // When & Then - Currently returns 500 due to error handling
        given()
                .when()
                .get("/api/v1/logos/external/UNKNOWN")
                .then()
                .statusCode(404);

        // Verify external API was called
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/ticker/UNKNOWN?token=test-token")));
    }

    @Test
    @DisplayName("Should create logo and then retrieve from database")
    void shouldCreateAndRetrieveLogo() {
        // Step 1: Create a logo
        String logoData = Base64.getEncoder().encodeToString("created-logo-data".getBytes());
        String requestBody = """
                {
                    "externalIdentifier": "NFLX",
                    "resourceUrl": "https://img.logo.dev/ticker/NFLX",
                    "fileContent": "%s",
                    "fileName": "NFLX.jpg",
                    "contentType": "image/jpeg"
                }
                """.formatted(logoData);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/logos")
                .then()
                .statusCode(201);

        // Step 2: Retrieve the logo (should come from database, not external API)
        given()
                .when()
                .get("/api/v1/logos/external/NFLX")
                .then()
                .statusCode(200)
                .header("Content-Type", "image/jpeg")
                .header("Content-Disposition", containsString("NFLX.jpg"))
                .body(equalTo("created-logo-data"));

        // Verify NO external API calls were made (since logo exists in DB)
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/ticker/NFLX?token=test-token")));
    }
}
