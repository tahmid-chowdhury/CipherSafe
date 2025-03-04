package com.example.ciphersafe;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private DatabaseReference database;

    public DatabaseManager(MainActivity mainActivity) {
        database = FirebaseDatabase.getInstance().getReference();
    }

    public void saveUser(String userId, String email, String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("username", username);

        database.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseDB", "User saved in Realtime Database"))
                .addOnFailureListener(e -> Log.e("FirebaseDB", "Error saving user", e));
    }
}