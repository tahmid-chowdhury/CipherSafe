package com.example.ciphersafe;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private DatabaseReference database;

    public DatabaseManager(MainActivity mainActivity) {
        database = FirebaseDatabase.getInstance().getReference();
    }


    public void saveUser(String userId, String email, String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("username", username);

        database.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User saved in Realtime Database"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving user", e));
    }


    public void getAllUsers(FirebaseUserDataListener listener) {
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> userList = new ArrayList<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Map<String, Object> userData = (Map<String, Object>) userSnapshot.getValue();
                    if (userData != null) {
                        userData.put("userId", userSnapshot.getKey()); // Add user ID
                        userList.add(userData);
                    }
                }

                listener.onSuccess(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching users", error.toException());
                listener.onError(error.getMessage());
            }
        });
    }


    public interface FirebaseUserDataListener {
        void onSuccess(List<Map<String, Object>> userList);
        void onError(String errorMessage);
    }
}
