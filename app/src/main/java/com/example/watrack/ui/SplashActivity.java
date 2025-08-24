package com.example.watrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.watrack.R;
import com.example.watrack.model.SessionResponse;
import com.example.watrack.util.SessionPrefs;
import com.example.watrack.viewmodel.AuthViewModel;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final long DEFAULT_SESSION_TTL_SECONDS = 300; // 5 minutes
    private static final int SPLASH_DURATION = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
        );

        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);

        logo.setAlpha(0f);

        logo.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(800)
                .withEndAction(() -> logo.animate().scaleX(1f).scaleY(1f).setDuration(300).start())
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(
                () -> appName.animate().alpha(1f).setDuration(600).start(),
                600
        );

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthAndSession, SPLASH_DURATION);
    }

    private void checkAuthAndSession() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            SessionPrefs.clearSessionId(this);
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Step 1: read cache
        SessionPrefs.SessionCache cached = SessionPrefs.getCachedSession(this);
        boolean cacheValid = cached != null && cached.status != null && cached.isFresh(DEFAULT_SESSION_TTL_SECONDS);

        if (cacheValid) {
            Log.d("SplashActivity", "Using cached session: " + cached.status);
            routeFromStatus(cached.status, cached.sessionId);
            return;
        }

        // Step 2: fetch from backend
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getUserSession(currentUser.getUid())
                .observe(this, session -> {
                    if (session == null) {
                        if (cached != null) {
                            Log.w("SplashActivity", "Backend offline, falling back to cached session");
                            routeFromStatus(cached.status, cached.sessionId);
                        } else {
                            SessionPrefs.clearSessionId(this);
                            startActivity(new Intent(this, QrLinkActivity.class));
                            finish();
                        }
                        return;
                    }

                    Log.d("SplashActivity", "Backend session object: " + session.toString());
                    Log.d("SplashActivity", "Session status: " + session.getStatus());
                    Log.d("SplashActivity", "Session ID: " + session.getSessionId());

                    String status = session.getStatus();
                    String sessionId = session.getSessionId();

                    if (status == null || sessionId == null) {
                        SessionPrefs.clearSessionId(this);
                        startActivity(new Intent(this, QrLinkActivity.class));
                        finish();
                        return;
                    }

                    long ttlSeconds = session.getTtlSeconds() > 0
                            ? session.getTtlSeconds()
                            : DEFAULT_SESSION_TTL_SECONDS;

                    SessionPrefs.saveSession(this, sessionId, status, ttlSeconds);

                    routeFromStatus(status, sessionId);
                });
    }

        private void routeFromStatus(String status, String sessionId) {
        if (SessionResponse.STATUS_CONNECTED.equalsIgnoreCase(status)) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (SessionResponse.STATUS_PENDING.equalsIgnoreCase(status)) {
            startActivity(new Intent(this, QrLinkActivity.class));
        } else {
            SessionPrefs.clearSessionId(this);
            startActivity(new Intent(this, QrLinkActivity.class));
        }
        finish();
    }
}
