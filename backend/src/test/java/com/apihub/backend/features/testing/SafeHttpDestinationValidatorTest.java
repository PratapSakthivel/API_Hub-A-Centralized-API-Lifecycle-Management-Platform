package com.apihub.backend.features.testing;

import com.apihub.backend.features.testing.service.SafeHttpDestinationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SafeHttpDestinationValidatorTest {

    private SafeHttpDestinationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SafeHttpDestinationValidator();
    }

    @Test
    void validateUrl_ValidPublicHttp_Success() {
        assertDoesNotThrow(() -> validator.validateUrl("https://example.com/api/test"));
    }

    @Test
    void validateUrl_DisallowedScheme_ThrowsSecurityException() {
        assertThrows(SecurityException.class, () -> validator.validateUrl("ftp://example.com/resource"));
        assertThrows(SecurityException.class, () -> validator.validateUrl("file:///etc/passwd"));
    }

    @Test
    void validateUrl_EmptyOrMalformed_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateUrl(""));
        assertThrows(IllegalArgumentException.class, () -> validator.validateUrl("not_a_valid_url"));
    }
}
