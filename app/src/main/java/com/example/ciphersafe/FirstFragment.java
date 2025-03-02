package com.example.ciphersafe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ciphersafe.databinding.FragmentFirstBinding;
import com.example.ciphersafe.security.SecurityManager;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private SecurityManager securityManager;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            securityManager = ((MainActivity) getActivity()).getSecurityManager();
        }

        binding.loginButton.setOnClickListener(v -> attemptLogin());

        binding.registerButton.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        binding.biometricButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).authenticateUser();
            }
        });
    }

    private void attemptLogin() {
        String username = binding.usernameInput.getText().toString();
        String password = binding.passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Here you would normally check against stored credentials
        // For demonstration, use a simple validation
        if (isValidCredentials(username, password)) {
            // Navigate to password list after successful login
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_to_passwordListFragment);
        } else {
            Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidCredentials(String username, String password) {
        // Replace with actual authentication logic using DatabaseManager
        // This is a placeholder implementation
        return username.equals("admin") && password.equals("password");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}