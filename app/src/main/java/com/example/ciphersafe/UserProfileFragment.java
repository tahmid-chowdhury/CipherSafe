package com.example.ciphersafe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileFragment extends Fragment {

    private TextView emailTextView;
    private TextView usernameTextView;
    private TextView passwordTextView;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        emailTextView = view.findViewById(R.id.emailTextView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        passwordTextView = view.findViewById(R.id.passwordTextView);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        emailTextView.setText(user.getEmail());
                        usernameTextView.setText(user.getUsername());
                        passwordTextView.setText(user.getPassword());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle possible errors.
                }
            });
        }

        return view;
    }

}
