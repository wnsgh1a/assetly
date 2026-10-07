package com.assetly.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductionEnvironmentValidatorTests {

    @Test
    void rejectsExampleJwtSecret() {
        assertThrows(IllegalStateException.class, () ->
                new ProductionEnvironmentValidator("change-this-secret-before-production", "secure-db-password"));
    }

    @Test
    void rejectsDefaultDatabasePassword() {
        assertThrows(IllegalStateException.class, () ->
                new ProductionEnvironmentValidator("a-secure-production-secret-that-is-longer-than-forty-eight-bytes", "assetly"));
    }

    @Test
    void rejectsDocumentedExampleValues() {
        assertThrows(IllegalStateException.class, () ->
                new ProductionEnvironmentValidator(
                        "replace-with-at-least-48-bytes-of-random-secret-material",
                        "replace-with-a-strong-database-password"));
    }

    @Test
    void acceptsSecureProductionSecrets() {
        assertDoesNotThrow(() ->
                new ProductionEnvironmentValidator("a-secure-production-secret-that-is-longer-than-forty-eight-bytes", "secure-db-password"));
    }
}
