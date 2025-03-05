package com.example.ciphersafe;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.security.SecureRandom;
import java.util.Random;

public class AddEditCredentialActivity extends AppCompatActivity {

    private TextInputEditText serviceNameEditText;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextView titleTextView;
    private Button generatePasswordButton;
    private Button saveButton;
    private Button cancelButton;

    private Credential credential;
    private int position = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        // Initialize views
        titleTextView = findViewById(R.id.addEditTitleTextView);
        serviceNameEditText = findViewById(R.id.serviceNameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        generatePasswordButton = findViewById(R.id.generatePasswordButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Check if we're in edit mode
        if (getIntent().hasExtra("credential")) {
            credential = (Credential) getIntent().getSerializableExtra("credential");
            position = getIntent().getIntExtra("position", -1);
            isEditMode = true;
            titleTextView.setText("Edit Credential");
            fillFields();
        } else {
            credential = new Credential();
            titleTextView.setText("Add New Credential");
        }

        // Setup click listeners
        generatePasswordButton.setOnClickListener(v -> {
            String generatedPassword = generateSecurePassword();
            passwordEditText.setText(generatedPassword);
        });

        saveButton.setOnClickListener(v -> {
            if (validateFields()) {
                saveCredential();
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void fillFields() {
        serviceNameEditText.setText(credential.getServiceName());
        usernameEditText.setText(credential.getUsername());
        passwordEditText.setText(credential.getPassword());
    }

    private boolean validateFields() {
        boolean isValid = true;

        String serviceName = serviceNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(serviceName)) {
            serviceNameEditText.setError("Service name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    private void saveCredential() {
        String serviceName = serviceNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        credential.setServiceName(serviceName);
        credential.setUsername(username);
        credential.setPassword(password);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("credential", credential);
        if (isEditMode && position != -1) {
            resultIntent.putExtra("position", position);
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String generateSecurePassword() {
        // Password generation parameters
        final int length = 16;
        final String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        final String numbers = "0123456789";
        final String specialChars = "!@#$%^&*()_-+=<>?";
        final String allChars = upperChars + lowerChars + numbers + specialChars;

        // Use SecureRandom for better randomness
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        // Ensure at least one character from each category
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Fill the rest of the password with random characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password characters to avoid predictable pattern
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int randomIndex = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[randomIndex];
            passwordArray[randomIndex] = temp;
        }

        return new String(passwordArray);
    }
}