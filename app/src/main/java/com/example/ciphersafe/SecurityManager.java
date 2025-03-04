package com.example.ciphersafe.security;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class SecurityManager {
    private static final String TAG = "SecurityManager";
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String AES_KEY_ALIAS = "ciphersafe_encryption_key";
    private static final int GCM_TAG_LENGTH = 128;

    private Context context;
    private KeyStore keyStore;

    public SecurityManager(Context context) {
        this.context = context;
        try {
            initializeKeyStore();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing security module", e);
        }
    }

    /**
     * Initialize Android KeyStore for secure key storage
     */
    private void initializeKeyStore() throws Exception {
        keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);

        if (!keyStore.containsAlias(AES_KEY_ALIAS)) {
            generateEncryptionKey();
        }
    }

    /**
     * Generate AES encryption key for password encryption
     */
    private void generateEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);

        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                AES_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build();

        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    /**
     * Generate a random salt for password hashing
     */
    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hash password with SHA-256 and provided salt
     */
    public String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error hashing password", e);
            return null;
        }
    }

    /**
     * Encrypt data using AES encryption
     */
    public String encryptData(String plaintext) {
        try {
            final SecretKey key = (SecretKey) keyStore.getKey(AES_KEY_ALIAS, null);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] iv = cipher.getIV();
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes());

            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data", e);
            return null;
        }
    }

    /**
     * Decrypt data using AES encryption
     */
    public String decryptData(String encryptedText) {
        try {
            byte[] combined = null;

            // Extract IV and encrypted data
            byte[] iv = new byte[12]; // GCM IV size
            byte[] encryptedData = new byte[combined.length - 12];
            System.arraycopy(combined, 0, iv, 0, 12);
            System.arraycopy(combined, 12, encryptedData, 0, encryptedData.length);

            final SecretKey key = (SecretKey) keyStore.getKey(AES_KEY_ALIAS, null);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data", e);
            return null;
        }
    }

    /**
     * Generate a strong random password
     */
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }
}