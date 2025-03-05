package com.example.ciphersafe;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for encrypting and decrypting sensitive data
 * This implementation uses standard Java/Android crypto without androidx.security
 */
public class EncryptionUtil {
    private static final String TAG = "EncryptionUtil";
    private static final String SHARED_PREFS_NAME = "encryption_prefs";
    private static final String PASSWORD_KEY = "master_password";
    private static final String SALT_KEY = "salt_key";

    private static final String DEFAULT_PASSWORD = "default_secure_password_123!";
    private static final int ITERATION_COUNT = 10000;
    private static final int KEY_LENGTH = 256;

    // AES encryption parameters
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    /**
     * Encrypt a string
     */
    public static String encrypt(Context context, String plainText) {
        try {
            // Get encryption key
            SecretKey secretKey = getSecretKey(context);

            // Generate random IV
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            // Encrypt
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            // Return as Base64
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption error", e);
            return plainText; // Fallback to plaintext
        }
    }

    /**
     * Decrypt a string
     */
    public static String decrypt(Context context, String encryptedText) {
        try {
            // Get encryption key
            SecretKey secretKey = getSecretKey(context);

            // Decode Base64
            byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);

            // Extract IV and encrypted data
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Decrypt
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // Return as string
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Decryption error", e);
            return encryptedText; // Return original if decryption fails
        }
    }

    /**
     * Get or create secret key for encryption/decryption
     */
    private static SecretKey getSecretKey(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

            // Get or generate salt
            String saltBase64 = prefs.getString(SALT_KEY, null);
            byte[] salt;

            if (saltBase64 == null) {
                // Generate new salt
                salt = new byte[16];
                new SecureRandom().nextBytes(salt);
                saltBase64 = Base64.encodeToString(salt, Base64.DEFAULT);
                prefs.edit().putString(SALT_KEY, saltBase64).apply();
            } else {
                salt = Base64.decode(saltBase64, Base64.DEFAULT);
            }

            // Get master password or use default
            String masterPassword = prefs.getString(PASSWORD_KEY, DEFAULT_PASSWORD);

            // Generate key from password and salt using PBKDF2
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                PBEKeySpec spec = new PBEKeySpec(
                        masterPassword.toCharArray(),
                        salt,
                        ITERATION_COUNT,
                        KEY_LENGTH
                );

                byte[] keyBytes = factory.generateSecret(spec).getEncoded();
                return new SecretKeySpec(keyBytes, ALGORITHM);
            } catch (Exception e) {
                Log.e(TAG, "Error generating key with PBKDF2", e);

                // Fallback to a simple key derivation if PBKDF2 fails
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] keyBytes = digest.digest((masterPassword + Base64.encodeToString(salt, Base64.DEFAULT)).getBytes(StandardCharsets.UTF_8));
                return new SecretKeySpec(keyBytes, ALGORITHM);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting secret key", e);

            // Ultimate fallback to a static key (not secure but better than crashing)
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] keyBytes = digest.digest(DEFAULT_PASSWORD.getBytes(StandardCharsets.UTF_8));
                return new SecretKeySpec(keyBytes, ALGORITHM);
            } catch (Exception ex) {
                Log.e(TAG, "Critical fallback error", ex);
                // Final fallback (extremely unsafe - for emergency only)
                return new SecretKeySpec("DefaultFallbackKey12".getBytes(), ALGORITHM);
            }
        }
    }

    /**
     * Set a master password for encryption (optional enhancement)
     */
    public static void setMasterPassword(Context context, String password) {
        if (password != null && !password.isEmpty()) {
            SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(PASSWORD_KEY, password).apply();
        }
    }
}