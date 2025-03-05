package com.example.ciphersafe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class AuthFragment extends Fragment {

    private Button authButton;
    private TextView statusText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authButton = view.findViewById(R.id.auth_button);
        statusText = view.findViewById(R.id.auth_status);

        // Check if biometric auth is available
        checkBiometricAvailability();

        authButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).authenticateUser();
            }
        });
    }

    private void checkBiometricAvailability() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                statusText.setText("Biometric authentication is available");
                authButton.setEnabled(true);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                statusText.setText("This device doesn't support biometric authentication");
                authButton.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                statusText.setText("Biometric features are currently unavailable");
                authButton.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                statusText.setText("No biometric credentials are enrolled");
                authButton.setEnabled(false);
                break;
            default:
                statusText.setText("Biometric status unknown");
                authButton.setEnabled(false);
                break;
        }
    }
}