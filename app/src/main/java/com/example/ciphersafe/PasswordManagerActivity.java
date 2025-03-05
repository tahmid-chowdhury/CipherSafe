package com.example.ciphersafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PasswordManagerActivity extends AppCompatActivity implements CredentialAdapter.OnCredentialClickListener {

    private static final int ADD_CREDENTIAL_REQUEST_CODE = 100;
    private static final int EDIT_CREDENTIAL_REQUEST_CODE = 101;

    private RecyclerView credentialsRecyclerView;
    private CredentialAdapter adapter;
    private List<Credential> credentialsList;
    private CredentialDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_profile);

        // Initialize database helper
        dbHelper = CredentialDatabaseHelper.getInstance(this);

        // Setup RecyclerView
        credentialsRecyclerView = findViewById(R.id.credentialsRecyclerView);
        credentialsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CredentialAdapter(this);
        credentialsRecyclerView.setAdapter(adapter);

        // Setup Add button
        FloatingActionButton addButton = findViewById(R.id.addCredentialButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(PasswordManagerActivity.this, AddEditCredentialActivity.class);
            startActivityForResult(intent, ADD_CREDENTIAL_REQUEST_CODE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load credentials from database each time activity is shown
        loadCredentials();
    }

    private void loadCredentials() {
        // Get credentials from SQLite database
        credentialsList = dbHelper.getAllCredentials();
        adapter.setCredentials(credentialsList);
    }

    @Override
    public void onEditClick(Credential credential, int position) {
        Intent intent = new Intent(PasswordManagerActivity.this, AddEditCredentialActivity.class);
        intent.putExtra("credential", credential);
        intent.putExtra("position", position);
        startActivityForResult(intent, EDIT_CREDENTIAL_REQUEST_CODE);
    }

    @Override
    public void onDeleteClick(Credential credential, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Credential")
                .setMessage("Are you sure you want to delete this credential?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete from database
                    dbHelper.deleteCredential(credential.getId());

                    // Update UI
                    adapter.removeCredential(position);
                    credentialsList.remove(position);

                    Toast.makeText(PasswordManagerActivity.this, "Credential deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onShowPasswordClick(Credential credential, int position) {
        // In a real app, you might want to add additional security here,
        // such as biometric authentication before showing the password
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("credential")) {
                Credential credential = (Credential) data.getSerializableExtra("credential");

                if (requestCode == ADD_CREDENTIAL_REQUEST_CODE) {
                    // Add to database
                    dbHelper.addCredential(credential);

                    // Update UI
                    credentialsList.add(credential);
                    adapter.addCredential(credential);

                    Toast.makeText(this, "Credential added", Toast.LENGTH_SHORT).show();
                } else if (requestCode == EDIT_CREDENTIAL_REQUEST_CODE) {
                    int position = data.getIntExtra("position", -1);
                    if (position != -1) {
                        // Update in database
                        dbHelper.updateCredential(credential);

                        // Update UI
                        credentialsList.set(position, credential);
                        adapter.updateCredential(credential, position);

                        Toast.makeText(this, "Credential updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}