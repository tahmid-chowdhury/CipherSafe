package com.example.ciphersafe;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {
    private FirebaseAuth mAuth;
    private FirebaseAuthListener authListener;

    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public interface FirebaseAuthListener {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public void setAuthListener(FirebaseAuthListener listener) {
        this.authListener = listener;
    }

    public void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        if (authListener != null) {
                            authListener.onSuccess("Registration successful");
                        }
                    } else {
                        // If registration fails, display a message to the user.
                        if (authListener != null) {
                            authListener.onError(task.getException() != null ?
                                    task.getException().getMessage() : "Registration failed");
                        }
                    }
                });
    }

    public void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        if (authListener != null) {
                            authListener.onSuccess("Login successful");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        if (authListener != null) {
                            authListener.onError(task.getException() != null ?
                                    task.getException().getMessage() : "Authentication failed");
                        }
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}