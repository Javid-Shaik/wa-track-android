package com.example.watrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.watrack.R;
import com.example.watrack.util.SessionPrefs;
import com.example.watrack.viewmodel.AuthViewModel;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());

        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);

        logo.setAlpha(0f);

        // Logo animation (fade + scale)
        logo.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(800)
                .withEndAction(() -> {
                    // Restore to normal scale after zoom effect
                    logo.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
                })
                .start();

        // App name fades in after logo
        new Handler().postDelayed(() -> appName.animate().alpha(1f).setDuration(600).start(), 600);

        // Navigate after delay
        new Handler().postDelayed(() -> {
            checkAuthAndSession();
        }, SPLASH_DURATION);
    }

    private void checkAuthAndSession() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Not logged in → go to login
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Ask backend for user’s session
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getUserSession(currentUser.getUid())
                .observe(this, session -> {
                    if (session != null && "LINKED".equals(session.getStatus())) {
                        SessionPrefs.saveSessionId(this, session.getSessionId());
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } else {
                        SessionPrefs.clearSessionId(this);
                        startActivity(new Intent(SplashActivity.this, QrLinkActivity.class));
                    }
                    finish();
                });
    }
}