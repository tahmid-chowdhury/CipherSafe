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
import com.example.ciphersafe.security.SecurityManager;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private SecurityManager securityManager;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            securityManager = ((MainActivity) getActivity()).getSecurityManager();
        }

        binding.registerSubmitButton.setOnClickListener(v -> attemptRegistration());

        binding.backToLoginButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );
    }

    private void attemptRegistration() {
        String username = binding.registerUsernameInput.getText().toString();
        String email = binding.registerEmailInput.getText().toString();
        String password = binding.registerPasswordInput.getText().toString();
        String confirmPassword = binding.confirmPasswordInput.getText().toString();

        // Validate inputs
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords don't match");
            return;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters");
            return;
        }

        // Create account using SecurityManager
        try {
            byte[] salt = securityManager.generateSalt();
            String hashedPassword = securityManager.hashPassword(password, salt);

            // Here you would normally save the user to a database
            // For now, just show success and return to login
            Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();

            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_FirstFragment);
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}