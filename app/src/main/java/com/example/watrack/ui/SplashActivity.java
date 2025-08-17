package com.example.watrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watrack.R;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;

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

        // Start invisible in code (not XML)
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
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DURATION);
    }
}
