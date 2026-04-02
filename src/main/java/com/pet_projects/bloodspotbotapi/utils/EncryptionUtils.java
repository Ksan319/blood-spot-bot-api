package com.pet_projects.bloodspotbotapi.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class EncryptionUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int AES_KEY_BIT = 256;

    private EncryptionUtils() {
    }

    public static String encrypt(String plaintext, String secret) {
        if (plaintext == null) {
            return null;
        }
        try {
            SecretKey key = getAESKey(secret);
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    public static String decrypt(String ciphertext, String secret) {
        if (ciphertext == null) {
            return null;
        }
        try {
            SecretKey key = getAESKey(secret);
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] encrypted = new byte[combined.length - IV_LENGTH_BYTE];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTE);
            System.arraycopy(combined, IV_LENGTH_BYTE, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    private static SecretKey getAESKey(String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("Secret key must be at least 32 characters");
        }
        byte[] keyBytes = secret.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
