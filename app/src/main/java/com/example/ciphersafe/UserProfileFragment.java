package com.example.ciphersafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment implements CredentialAdapter.OnCredentialClickListener {

    private static final int ADD_CREDENTIAL_REQUEST_CODE = 100;
    private static final int EDIT_CREDENTIAL_REQUEST_CODE = 101;

    private RecyclerView credentialsRecyclerView;
    private CredentialAdapter adapter;
    private List<Credential> credentialsList;
    private CredentialDatabaseHelper dbHelper;
    private FloatingActionButton addButton;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        // Initialize database helper
        dbHelper = CredentialDatabaseHelper.getInstance(requireContext());

        // Initialize credentials list
        credentialsList = new ArrayList<>();

        // Get current user ID
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Handle case where user is not logged in
            Toast.makeText(requireContext(), "You must be logged in to view credentials", Toast.LENGTH_LONG).show();
            // You might want to navigate back to login screen here
            return view;
        }

        // Setup RecyclerView
        credentialsRecyclerView = view.findViewById(R.id.credentialsRecyclerView);
        credentialsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CredentialAdapter(this);
        credentialsRecyclerView.setAdapter(adapter);

        // Setup Add button
        addButton = view.findViewById(R.id.addCredentialButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), Add_Edit_Activity.class);
            // Pass the current user ID to the add activity
            intent.putExtra("userId", currentUserId);
            startActivityForResult(intent, ADD_CREDENTIAL_REQUEST_CODE);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load credentials from database each time fragment is shown
        loadCredentials();
    }

    private void loadCredentials() {
        if (currentUserId != null) {
            // Load only credentials that belong to the current user
            credentialsList = dbHelper.getCredentialsForUser(currentUserId);
            adapter.setCredentials(credentialsList);

            if (credentialsList.isEmpty()) {
                // Show a message if no credentials exist
                Toast.makeText(requireContext(), "No saved credentials. Add your first one!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onEditClick(Credential credential, int position) {
        // Check if position is valid
        if (position < 0 || position >= credentialsList.size()) {
            Toast.makeText(requireContext(), "Error: Invalid credential position", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireActivity(), Add_Edit_Activity.class);
        intent.putExtra("credential", credential);
        intent.putExtra("position", position);
        // Also pass the user ID in case it's needed
        intent.putExtra("userId", currentUserId);
        startActivityForResult(intent, EDIT_CREDENTIAL_REQUEST_CODE);
    }

    @Override
    public void onDeleteClick(Credential credential, int position) {
        // Safety check - verify position is valid
        if (position < 0 || position >= credentialsList.size()) {
            Toast.makeText(requireContext(), "Error: Invalid position", Toast.LENGTH_SHORT).show();
            // Reload credentials to refresh the UI state
            loadCredentials();
            return;
        }

        // Check if this credential belongs to the current user
        if (credential.getUserId() == null || credential.getUserId().equals(currentUserId)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Credential")
                    .setMessage("Are you sure you want to delete this credential?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try {
                            // Delete from database first
                            dbHelper.deleteCredential(credential.getId());

                            // Then remove from the adapter
                            adapter.removeCredential(position);

                            // Finally remove from our list (safely)
                            if (position >= 0 && position < credentialsList.size()) {
                                credentialsList.remove(position);
                            } else {
                                // If the index is somehow out of bounds, reload everything
                                loadCredentials();
                            }

                            Toast.makeText(requireContext(), "Credential deleted", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            // Handle any exceptions that might occur
                            Toast.makeText(requireContext(), "Error deleting credential: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            // Reload to ensure UI is in sync with database
                            loadCredentials();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(requireContext(), "You can only delete your own credentials", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShowPasswordClick(Credential credential, int position) {
        // In a real app, you might want to add additional security here,
        // such as biometric authentication before showing the password
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == requireActivity().RESULT_OK) {
            if (data != null && data.hasExtra("credential")) {
                Credential credential = (Credential) data.getSerializableExtra("credential");

                // Ensure the credential has the current user's ID
                if (credential.getUserId() == null) {
                    credential.setUserId(currentUserId);
                }

                if (requestCode == ADD_CREDENTIAL_REQUEST_CODE) {
                    // Add to database
                    dbHelper.addCredential(credential);

                    // Reload all credentials to ensure consistency
                    loadCredentials();

                    Toast.makeText(requireContext(), "Credential added", Toast.LENGTH_SHORT).show();
                } else if (requestCode == EDIT_CREDENTIAL_REQUEST_CODE) {
                    int position = data.getIntExtra("position", -1);
                    if (position != -1 && position < credentialsList.size()) {
                        // Only allow editing if credential belongs to current user
                        if (credential.getUserId() == null || credential.getUserId().equals(currentUserId)) {
                            // Update in database
                            dbHelper.updateCredential(credential);

                            // Reload all credentials to ensure consistency
                            loadCredentials();

                            Toast.makeText(requireContext(), "Credential updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "You can only edit your own credentials", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Position is invalid, reload everything
                        loadCredentials();
                    }
                }
            }
        }
    }
}