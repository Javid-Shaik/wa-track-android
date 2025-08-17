package com.example.watrack.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.watrack.R;
import com.example.watrack.databinding.ActivityLoginBinding;
import com.example.watrack.util.LoaderDialog;
import com.example.watrack.viewmodel.AuthViewModel;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

// Credential Manager imports
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

/*
 * --- Old Google Sign-In imports (deprecated, kept for reference) ---
 * import com.google.android.gms.auth.api.signin.GoogleSignIn;
 * import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
 * import com.google.android.gms.auth.api.signin.GoogleSignInClient;
 * import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
 * import com.google.android.gms.common.api.ApiException;
 * import com.google.android.gms.tasks.Task;
 */

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private AuthViewModel authViewModel;
    private boolean isPasswordVisible = false;
    private LoaderDialog loader;

    // Credential Manager
    private CredentialManager credentialManager;

    /*
     * --- Old Google Sign-In constants (deprecated, kept for reference) ---
     * private GoogleSignInClient googleSignInClient;
     * private static final int RC_GOOGLE_SIGN_IN = 9001;
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize Credential Manager
        credentialManager = CredentialManager.create(this);

        /*
         * --- Old Google Sign-In initialization (deprecated, kept for reference) ---
         * GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
         *         .requestIdToken(getString(R.string.default_web_client_id))
         *         .requestEmail()
         *         .build();
         * googleSignInClient = GoogleSignIn.getClient(this, gso);
         */

        loader = new LoaderDialog();

        animateIntro();
        setupPasswordToggle();
        setupEmailAuth();
        setupGoogleAuth(); // ✅ New flow using Credential Manager
        setupForgotPassword();
        setupSignUpPrompt();
    }

    private void animateIntro() {
        binding.logo.animate().alpha(1f).setDuration(500).setStartDelay(100);
        binding.title.animate().alpha(1f).setDuration(500).setStartDelay(300);
        binding.subtitle.animate().alpha(1f).setDuration(500).setStartDelay(500);
    }

    private void setupEmailAuth() {
        binding.btnEmailLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                binding.etEmail.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                binding.etPassword.setError("Password is required");
                return;
            }

            loader.show(getSupportFragmentManager(), "loader");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        safeDismissLoader();

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.e("Firebase user --> {}", user.getUid());
                                user.getIdToken(true).addOnSuccessListener(result -> {
                                    String idToken = result.getToken();
                                    registerWithBackend(idToken);
                                });
                            }
                        } else {
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Unknown error";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Invalid email or password";
                            }
                            Toast.makeText(this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.e("LoginActivity", "Email login error", task.getException());
                        }
                    });
        });
    }

    private void setupGoogleAuth() {
        binding.btnGoogleSignIn.setOnClickListener(v -> {
            beginGoogleSignIn();
        });
    }

    private void beginGoogleSignIn() {
        // 1. Build GetGoogleIdOption with server client ID
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id)) // ✅ must be Web client ID
                .setAutoSelectEnabled(true)
                .build();

        // 2. Build GetCredentialRequest
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // 3. Call CredentialManager
        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignInResult(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.w("GoogleSignIn", "Google sign in failed", e);
                        if (e instanceof androidx.credentials.exceptions.NoCredentialException) {
                            Toast.makeText(LoginActivity.this,
                                    "No Google accounts found or sign-in cancelled.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Google sign-in failed. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void handleGoogleSignInResult(GetCredentialResponse result) {
        Credential credential = result.getCredential();
        try {
            // ✅ Correct way: parse credential data into GoogleIdTokenCredential
            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.getData());
            String idToken = googleIdTokenCredential.getIdToken();
            firebaseAuthWithGoogle(idToken);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse Google credentials", Toast.LENGTH_SHORT).show();
            Log.e("GoogleSignIn", "Credential parsing failed", e);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        loader.show(getSupportFragmentManager(), "loader");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    safeDismissLoader();

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.getIdToken(true).addOnSuccessListener(result -> {
                                String token = result.getToken();
                                registerWithBackend(token);
                            });
                        }
                    } else {
                        Toast.makeText(this, "Google authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*
     * --- Old Google Sign-In activity result handler (deprecated, kept for reference) ---
     * @Override
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *
     *     if (requestCode == RC_GOOGLE_SIGN_IN) {
     *         Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
     *         try {
     *             GoogleSignInAccount account = task.getResult(ApiException.class);
     *             firebaseAuthWithGoogle(account.getIdToken());
     *         } catch (ApiException e) {
     *             Log.w("GoogleSignIn", "Google sign in failed", e);
     *             Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
     *         }
     *     }
     * }
     */

    private void setupForgotPassword() {
        binding.tvForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        Toast.makeText(this,
                                task.isSuccessful() ? "Reset email sent" : "Error: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void setupSignUpPrompt() {
        binding.tvSignUpPrompt.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    private void registerWithBackend(String firebaseIdToken) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(deviceToken -> {
                    authViewModel.registerUser(firebaseIdToken, deviceToken)
                            .observe(this, response -> {
                                Log.e("LoginActivity", "Response code: " + response.code());
                                if (response != null && response.code() != 404) {
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Backend registration failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get FCM token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("LoginActivity", "FCM token error", e);
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle() {
        binding.ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = false;
            } else {
                binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
                isPasswordVisible = true;
            }
            binding.etPassword.setSelection(binding.etPassword.getText().length());
        });
        binding.ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = false;
            } else {
                binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
                isPasswordVisible = true;
            }
            binding.etPassword.setSelection(binding.etPassword.getText().length());
        });
    }

    private void safeDismissLoader() {
        if (loader != null && loader.isAdded()) {
            if (!getSupportFragmentManager().isStateSaved()) {
                loader.dismiss();
            } else {
                Log.w("LoginActivity", "Attempted to dismiss loader after onSaveInstanceState.");
            }
        }
    }
}
