package com.example.ciphersafe;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.widget.Toast;
import androidx.biometric.BiometricManager;
import androidx.navigation.NavDestination;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.ciphersafe.databinding.ActivityMainBinding;
import com.example.ciphersafe.SecurityManager;
import com.example.ciphersafe.FirebaseAuthManager;
import com.example.ciphersafe.DatabaseManager;
import com.example.ciphersafe.HackerModeManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CipherSafe";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    private SecurityManager securityManager;
    private DatabaseManager databaseManager;
    private HackerModeManager hackerModeManager;
    private FirebaseAuthManager firebaseAuthManager;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        Log.d("FirebaseAuth", "Firebase initialized");
        // Initialize binding and set content view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        FirebaseAuth.getInstance().signOut();
        initializeManagers();

        setupNavigation();

        setupBiometricAuth();

//        setupFab();
    }

    /**
     * Initialize all module managers for the app
     */
    private void initializeManagers() {
        // Initialize Security Manager for encryption, hashing, and key management
        securityManager = new SecurityManager(this);

        // Initialize Firebase Auth Manager
        firebaseAuthManager = new FirebaseAuthManager(securityManager);

        // Initialize Database Manager for Firebase/cloud operations
        databaseManager = new DatabaseManager(this);


        Log.d(TAG, "All managers initialized successfully");
    }

    /**
     * Set up the navigation components
     */
    private void setupNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    /**
     * Set up biometric authentication using BiometricPrompt
     */
    private void setupBiometricAuth() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        runOnUiThread(() -> {
                            Snackbar.make(binding.getRoot(), "Authentication error: " + errString,
                                    Snackbar.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Authentication successful!",
                                    Toast.LENGTH_SHORT).show();

                            // Get current fragment id to determine where to navigate
                            NavDestination currentDest = navController.getCurrentDestination();
                            if (currentDest != null) {
                                int currentId = currentDest.getId();

                                if (currentId == R.id.FirstFragment) {
                                    // Coming from login screen
                                    navController.navigate(R.id.action_to_passwordListFragment);
                                } else if (currentId == R.id.authFragment) {
                                    // Coming from dedicated auth screen
                                    navController.navigate(R.id.action_authFragment_to_passwordListFragment);
                                }
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        runOnUiThread(() -> {
                            Snackbar.make(binding.getRoot(), "Authentication failed",
                                    Snackbar.LENGTH_SHORT).show();
                        });
                    }
                });

        // Set up prompt info for biometric dialog
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Log in using your biometric credential")
                .setDescription("Use your fingerprint or face to access your secure passwords")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Cancel")
                .build();
    }

    /**
     * Authenticate user with biometrics
     */
    public void authenticateUser() {
        BiometricManager biometricManager = BiometricManager.from(this);

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, "Biometric authentication not available",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set up FAB to add new password entries
     */
//    private void setupFab() {
//        binding.fab.setImageResource(android.R.drawable.ic_input_add);
//        binding.fab.setOnClickListener(view -> {
//            // Navigate to add password screen
//            // This will be replaced with actual navigation when we update the nav graph
//            Snackbar.make(view, "Add new password", Snackbar.LENGTH_LONG)
//                    .setAnchorView(R.id.fab)
//                    .setAction("Add", v -> {
//                        navController.navigate(R.id.action_to_addPasswordFragment);
//                    }).show();
//        });
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            navController.navigate(R.id.action_to_settingsFragment);
//            return true;
//        } else if (id == R.id.action_hacker_mode) {
//            // Launch hacker mode simulation
//            hackerModeManager.startSimulation();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public FirebaseAuthManager getFirebaseAuthManager() {
        return firebaseAuthManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public HackerModeManager getHackerModeManager() {
        return hackerModeManager;
    }
}