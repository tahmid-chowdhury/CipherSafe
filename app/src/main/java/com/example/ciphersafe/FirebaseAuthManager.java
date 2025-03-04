package com.example.ciphersafe;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuthManager {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public interface FirebaseAuthListener {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public void registerUser(String email, String password, String username, FirebaseAuthListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d("FirebaseAuth", "User registered successfully: " + user.getEmail());

                            // Save user data to Realtime Database
                            saveUserToDatabase(user.getUid(), email, username, password, listener);
                        }
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e("FirebaseAuth", "Registration failed: " + e.getMessage(), e);
                            listener.onError(e.getMessage());
                        } else {
                            Log.e("FirebaseAuth", "Registration failed with unknown error.");
                            listener.onError("Unknown error.");
                        }
                    }
                });
    }

    private void saveUserToDatabase(String userId, String email, String username, String password, FirebaseAuthListener listener) {
        // Create a user object
        User user = new User(email, username, password);

        // Save to Realtime Database
        databaseReference.child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseAuth", "User data saved to Realtime Database");
                    listener.onSuccess("Registration successful!");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseAuth", "Failed to save user data to Realtime Database: " + e.getMessage());
                    listener.onError("Failed to save user data.");
                });
    }

    public void activateUser(String email, String password, FirebaseAuthListener listener) {
        Log.d("FirebaseAuth", "Attempting to log in with email: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d("FirebaseAuth", "User logged in successfully: " + user.getEmail());
                            listener.onSuccess("Login successful!");
                        } else {
                            Log.e("FirebaseAuth", "Login failed: Current user is null");
                            listener.onError("Login failed: User not found.");
                        }
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e("FirebaseAuth", "Login failed: " + e.getMessage(), e);
                            listener.onError(e.getMessage());
                        } else {
                            Log.e("FirebaseAuth", "Login failed with unknown error.");
                            listener.onError("Unknown error.");
                        }
                    }
                });
    }


    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}
