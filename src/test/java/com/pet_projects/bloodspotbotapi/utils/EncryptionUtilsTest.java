package com.pet_projects.bloodspotbotapi.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptionUtilsTest {

    private static final String SECRET_KEY = "this_is_a_32_character_secret__!";

    @Test
    public void testEncryptDecrypt_Success() {
        String plaintext = "myPassword123";
        String encrypted = EncryptionUtils.encrypt(plaintext, SECRET_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, SECRET_KEY);
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testEncryptDecrypt_EmptyString() {
        String plaintext = "";
        String encrypted = EncryptionUtils.encrypt(plaintext, SECRET_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, SECRET_KEY);
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testEncryptDecrypt_NullInput() {
        String encrypted = EncryptionUtils.encrypt(null, SECRET_KEY);
        assertNull(encrypted);
        String decrypted = EncryptionUtils.decrypt(null, SECRET_KEY);
        assertNull(decrypted);
    }

    @Test
    public void testEncrypt_WrongKey() {
        String plaintext = "myPassword123";
        String encrypted = EncryptionUtils.encrypt(plaintext, SECRET_KEY);
        String wrongKey = "different_32_character_secret!";
        assertThrows(EncryptionUtils.EncryptionException.class, () -> {
            EncryptionUtils.decrypt(encrypted, wrongKey);
        });
    }

    @Test
    public void testEncryptDecrypt_LongPassword() {
        String plaintext = "a".repeat(100);
        String encrypted = EncryptionUtils.encrypt(plaintext, SECRET_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, SECRET_KEY);
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testEncryptDecrypt_CyrillicPassword() {
        String plaintext = "пароль123";
        String encrypted = EncryptionUtils.encrypt(plaintext, SECRET_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, SECRET_KEY);
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testEncryptDecrypt_SpecialCharacters() {
        String plaintext = "p@$$w0rd!#$%^&*()";
        String encrypted = EncryptionUtils.encrypt(plaintext, SECRET_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, SECRET_KEY);
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testEncrypt_GeneratesDifferentCiphertext() {
        String plaintext = "myPassword123";
        String encrypted1 = EncryptionUtils.encrypt(plaintext, SECRET_KEY);
        String encrypted2 = EncryptionUtils.encrypt(plaintext, SECRET_KEY);
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    public void testDecrypt_InvalidBase64() {
        assertThrows(EncryptionUtils.EncryptionException.class, () -> {
            EncryptionUtils.decrypt("not_valid_base64!", SECRET_KEY);
        });
    }

    @Test
    public void testGetAESKey_TooShort() {
        assertThrows(EncryptionUtils.EncryptionException.class, () -> {
            EncryptionUtils.encrypt("test", "short");
        });
    }
}
