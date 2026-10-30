package com.learningplatform.backend;

import com.learningplatform.backend.validation.StrongPasswordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Himalaya@2026", "Abcdef1!", "K2-is-8611m"})
    void acceptsStrongPasswords(String password) {
        assertTrue(validator.isValid(password, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Ab1!",             // too short
            "himalaya@2026",    // no uppercase
            "HIMALAYA@2026",    // no lowercase
            "Himalaya@climb",   // no number
            "Himalaya2026",     // no special character
            "Hima laya@2026",   // contains a space
            "secret123"         // the old 6+ rule would have allowed this
    })
    void rejectsWeakPasswords(String password) {
        assertFalse(validator.isValid(password, null));
    }

    @Test
    void rejectsMissingAndTooLongPasswords() {
        assertFalse(validator.isValid(null, null));
        assertFalse(validator.isValid("Aa1!" + "x".repeat(61), null)); // 65 characters
    }
}
