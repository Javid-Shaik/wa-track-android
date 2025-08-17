package com.example.watrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.watrack.databinding.ActivityOtpBinding;
import com.example.watrack.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

public class OtpActivity extends AppCompatActivity {

    private ActivityOtpBinding binding;
    private String verificationId;
    private FirebaseAuth mAuth;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        verificationId = getIntent().getStringExtra("verificationId");

        animateIntro();

        binding.btnVerifyOtp.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString().trim();
            if (TextUtils.isEmpty(otp) || otp.length() < 6) {
                binding.etOtp.setError("Enter valid OTP");
                return;
            }

            binding.btnVerifyOtp.setEnabled(false);

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            mAuth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> {
                        mAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(result -> {
                            String idToken = result.getToken();
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnSuccessListener(deviceToken -> {
                                        authViewModel.registerUser(idToken, deviceToken)
                                                .observe(this, response -> {
                                                    binding.btnVerifyOtp.setEnabled(true);
                                                    if (response != null && response.isSuccessful()) {
                                                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(this, MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(this, "Backend registration failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        binding.btnVerifyOtp.setEnabled(true);
                        Toast.makeText(this, "OTP verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void animateIntro() {
        binding.subtitle.animate().alpha(1f).setDuration(500).setStartDelay(500);
    }
}