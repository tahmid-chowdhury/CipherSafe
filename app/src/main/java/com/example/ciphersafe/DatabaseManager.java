package com.example.ciphersafe;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_PASSWORDS = "passwords";

    private Context context;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    public DatabaseManager(Context context) {
        this.context = context;

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "Database manager initialized");
    }

    /**
     * Create a new user account
     */
    public void createUser(String email, String hashedPassword, byte[] salt, final DatabaseCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, hashedPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Store user details in Firestore
                            Map<String, Object> userDoc = new HashMap<>();
                            userDoc.put("email", email);
                            userDoc.put("salt", salt);

                            firestore.collection(COLLECTION_USERS)
                                    .document(user.getUid())
                                    .set(userDoc)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User document created");
                                        callback.onSuccess(null);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error creating user document", e);
                                        callback.onError(e.getMessage());
                                    });
                        }
                    } else {
                        Log.e(TAG, "Error creating account", task.getException());
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    /**
     * Sign in existing user
     */
    public void signIn(String email, String hashedPassword, final DatabaseCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, hashedPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign in successful");
                        callback.onSuccess(null);
                    } else {
                        Log.e(TAG, "Sign in failed", task.getException());
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    /**
     * Sign out current user
     */
    public void signOut() {
        firebaseAuth.signOut();
        Log.d(TAG, "User signed out");
    }

    /**
     * Save encrypted password to database
     */
    public void savePassword(String title, String username, String encryptedPassword,
                             String website, final DatabaseCallback callback) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No user logged in");
            return;
        }

        Map<String, Object> passwordEntry = new HashMap<>();
        passwordEntry.put("title", title);
        passwordEntry.put("username", username);
        passwordEntry.put("password", encryptedPassword);
        passwordEntry.put("website", website);
        passwordEntry.put("timestamp", System.currentTimeMillis());

        firestore.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .collection(COLLECTION_PASSWORDS)
                .add(passwordEntry)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Password saved with ID: " + documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving password", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Retrieve all encrypted passwords for current user
     */
    public void getAllPasswords(final PasswordsCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No user logged in");
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .collection(COLLECTION_PASSWORDS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            callback.onPasswordsLoaded(snapshot.getDocuments());
                        }
                    } else {
                        Log.e(TAG, "Error fetching passwords", task.getException());
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    /**
     * Update existing password entry
     */
    public void updatePassword(String passwordId, String title, String username,
                               String encryptedPassword, String website, final DatabaseCallback callback) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No user logged in");
            return;
        }

        DocumentReference passwordRef = firestore.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .collection(COLLECTION_PASSWORDS)
                .document(passwordId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("username", username);
        updates.put("password", encryptedPassword);
        updates.put("website", website);
        updates.put("updated_at", System.currentTimeMillis());

        passwordRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Password updated successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating password", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Delete password entry
     */
    public void deletePassword(String passwordId, final DatabaseCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No user logged in");
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .collection(COLLECTION_PASSWORDS)
                .document(passwordId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Password deleted successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting password", e);
                    callback.onError(e.getMessage());
                });
    }

    // Callback interfaces
    public interface DatabaseCallback {
        void onSuccess(String data);
        void onError(String errorMessage);
    }

    public interface PasswordsCallback {
        void onPasswordsLoaded(List<?> passwords);
        void onError(String errorMessage);
    }
}