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
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityManager {
    private static final String TAG = "SecurityManager";
    private static final String PREFS_NAME = "CryptoPrefs";
    private static final String PREF_KEY = "AESKey";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_SIZE = 12;
    private Context context;

    public SecurityManager(Context context) {
        this.context = context;
    }

    public String encryptData(String plaintext, String email) {
        try {
            byte[] iv = generateDeterministicIV(email);

            SecretKey key = getOrGenerateKey();

            int salt = email.replaceAll("\\D", "").length();
            byte[] saltBytes = String.valueOf(salt).getBytes(StandardCharsets.UTF_8);

            byte[] combined = new byte[saltBytes.length + plaintext.getBytes().length];
            System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
            System.arraycopy(plaintext.getBytes(), 0, combined, saltBytes.length, plaintext.getBytes().length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encryptedData = cipher.doFinal(combined);

            byte[] finalCombined = new byte[IV_SIZE + encryptedData.length];
            System.arraycopy(iv, 0, finalCombined, 0, IV_SIZE);
            System.arraycopy(encryptedData, 0, finalCombined, IV_SIZE, encryptedData.length);

            return Base64.encodeToString(finalCombined, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data", e);
            return null;
        }
    }

    public String decryptData(String encryptedText, String email) {
        try {
            byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);

            byte[] iv = generateDeterministicIV(email);
            byte[] encryptedData = new byte[combined.length - IV_SIZE];
            System.arraycopy(combined, IV_SIZE, encryptedData, 0, encryptedData.length);

            SecretKey key = getOrGenerateKey();


            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decryptedData = cipher.doFinal(encryptedData);

            int salt = email.replaceAll("\\D", "").length();
            byte[] saltBytes = String.valueOf(salt).getBytes(StandardCharsets.UTF_8);
            byte[] plaintextBytes = new byte[decryptedData.length - saltBytes.length];
            System.arraycopy(decryptedData, saltBytes.length, plaintextBytes, 0, plaintextBytes.length);

            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data", e);
            return null;
        }
    }

    private byte[] generateDeterministicIV(String email) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(email.getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(hash, 0, iv, 0, IV_SIZE);
        return iv;
    }

    private SecretKey getOrGenerateKey() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encodedKey = prefs.getString(PREF_KEY, null);

        if (encodedKey != null) {
            byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
            return new SecretKeySpec(decodedKey, "AES");
        } else {
            byte[] keyBytes = new byte[32];
            new SecureRandom().nextBytes(keyBytes);
            String newEncodedKey = Base64.encodeToString(keyBytes, Base64.DEFAULT);

            prefs.edit().putString(PREF_KEY, newEncodedKey).apply();

            return new SecretKeySpec(keyBytes, "AES");
        }
    }
}
