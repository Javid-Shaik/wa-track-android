package com.example.watrack.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.watrack.R;
import com.example.watrack.databinding.ActivitySignupBinding;
import com.example.watrack.util.LoaderDialog;
import com.example.watrack.viewmodel.AuthViewModel;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
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

import org.json.JSONException;

import java.util.concurrent.Executor;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private AuthViewModel authViewModel;
    private CredentialManager credentialManager;
    private boolean isPasswordVisible = false;

    private LoaderDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        credentialManager = CredentialManager.create(this);

        loader = new LoaderDialog();

        animateIntro();
        setupEmailSignUp();
        setupGoogleSignUp();
        setupLoginPrompt();
        setupPasswordToggle();
    }

    private void animateIntro() {
        binding.logo.animate().alpha(1f).setDuration(500).setStartDelay(100);
        binding.title.animate().alpha(1f).setDuration(500).setStartDelay(300);
        binding.subtitle.animate().alpha(1f).setDuration(500).setStartDelay(500);
    }

    private void setupEmailSignUp() {
        binding.btnSignUp.setOnClickListener(v -> {
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

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        safeDismissLoader();

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.getIdToken(true).addOnSuccessListener(result -> {
                                    String idToken = result.getToken();
                                    registerWithBackend(idToken);
                                });
                            }
                        } else {
                            handleSignUpError(task.getException());
                        }
                    });
        });
    }

    private void setupGoogleSignUp() {
        binding.btnGoogleSignUp.setOnClickListener(v -> {
            initiateGoogleSignUp();
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork);
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void initiateGoogleSignUp() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Correct GoogleIdOption setup
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        Executor executor = ContextCompat.getMainExecutor(this);

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignUpResult(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.w("GoogleSignUp", "Google sign up failed", e);
                        Toast.makeText(SignupActivity.this,
                                "Google sign-up failed. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void handleGoogleSignUpResult(GetCredentialResponse result) {
        try {
            Credential credential = result.getCredential();

            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.getData());

            String idToken = googleIdTokenCredential.getIdToken();
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
            loader.show(getSupportFragmentManager(), "loader");

            mAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this, task -> {
                        safeDismissLoader();

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.getIdToken(true).addOnSuccessListener(res -> {
                                    String token = res.getToken();
                                    registerWithBackend(token);
                                });
                            }
                        } else {
                            handleSignUpError(task.getException());
                        }
                    });

        } catch (Exception e) {
            // Needed for createFrom()
            Log.e("GoogleSignUp", "Failed to parse Google credential", e);
            Toast.makeText(this, "Invalid Google credential", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignUpError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "Account already exists. Please log in.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            Toast.makeText(this, "Sign-up failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("SignupActivity", "Sign-up error", exception);
        }
    }

    private void setupLoginPrompt() {
        binding.tvLoginPrompt.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void registerWithBackend(String firebaseIdToken) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(deviceToken -> {
                    authViewModel.registerUser(firebaseIdToken, deviceToken)
                            .observe(this, response -> {
                                Log.d("Response from backend", firebaseIdToken);
                                if (response != null) {
                                    Toast.makeText(this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Backend registration failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get FCM token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SignupActivity", "FCM token error", e);
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
        binding.ivToggleConfirmPassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                binding.etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = false;
            } else {
                binding.etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_on);
                isPasswordVisible = true;
            }
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.getText().length());
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
