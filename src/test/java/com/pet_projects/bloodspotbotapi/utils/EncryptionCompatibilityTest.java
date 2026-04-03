package com.pet_projects.bloodspotbotapi.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptionCompatibilityTest {

    private static final String SECRET_KEY = System.getenv("ENCRYPTION_SECRET_KEY");

    @Test
    @EnabledIfEnvironmentVariable(named = "ENCRYPTION_SECRET_KEY", matches = ".{32,}")
    void testDecryptPythonEncryptedPassword() {
        String encrypted = "REPLACE_WITH_PYTHON_TEST_OUTPUT";
        String decrypted = EncryptionUtils.decrypt(encrypted, SECRET_KEY);
        assertEquals("TEST_PASSWORD_123", decrypted);
    }

    @Test
    void testDecryptPythonEncryptedPasswordWithFixedKey() {
        String secret = "this_is_a_32_character_secret__!";
        String encrypted = "22bqKFuruRsS6j8l1adRc/lhyIA7GsaXDu709DTSAHoYbwZd+tNz5ztwFFS+";
        String decrypted = EncryptionUtils.decrypt(encrypted, secret);
        assertEquals("TEST_PASSWORD_123", decrypted);
    }
}
