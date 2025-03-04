package com.example.ciphersafe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ciphersafe.databinding.FragmentSecondBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private FirebaseAuthManager authManager;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            authManager = ((MainActivity) getActivity()).getFirebaseAuthManager();
        }

        binding.registerSubmitButton.setOnClickListener(v -> attemptRegistration());

        binding.backToLoginButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );
    }

    private void attemptRegistration() {
        FirebaseAuth.getInstance().signOut();
        String username = binding.registerUsernameInput.getText().toString();
        String email = binding.registerEmailInput.getText().toString();
        String password = binding.registerPasswordInput.getText().toString();
        String confirmPassword = binding.confirmPasswordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("All fields are required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        if (password.length() < 8) {
            showToast("Password must be at least 8 characters long");
            return;
        }

        // ✅ Register user in Firebase
        authManager.registerUser(email, password, username, new FirebaseAuthManager.FirebaseAuthListener() {
            @Override
            public void onSuccess(String message) {
                showToast(message);
                // Navigate back to login screen after registration
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }

            @Override
            public void onError(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
