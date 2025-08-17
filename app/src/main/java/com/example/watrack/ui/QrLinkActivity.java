package com.example.watrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

// Removed unused Glide import as we are no longer loading a dynamic image.
// import com.bumptech.glide.Glide;
import com.example.watrack.R;
import com.example.watrack.databinding.ActivityQrLinkBinding;
import com.example.watrack.viewmodel.QrLinkViewModel;

import java.util.concurrent.TimeUnit;

public class QrLinkActivity extends AppCompatActivity {

    private ActivityQrLinkBinding binding;
    private QrLinkViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrLinkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(QrLinkViewModel.class);

        setupUi();
        observeVm();

        // ✅ FIX: Use the correct method name from the ViewModel
        viewModel.fetchQr();

        // Optional: disable back while linking (UX choice)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                Toast.makeText(QrLinkActivity.this, "Please finish linking or tap Cancel.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUi() {
        // Buttons
        // ✅ FIX: Use the correct method name from the ViewModel
        binding.btnRefresh.setOnClickListener(v -> viewModel.fetchQr());
        binding.btnCancel.setOnClickListener(v -> finish());

        // Terms & Privacy
        binding.tvTerms.setOnClickListener(v -> showInfoDialog(
                "Terms & Conditions",
                "By using WaTrack, you agree to:\n\n" +
                        "• Use the app ethically and legally.\n" +
                        "• Not use it for harassment or surveillance.\n" +
                        "• You are responsible for compliance with local laws.\n\n" +
                        "WaTrack provides presence analytics only; it does not access messages or media."
        ));

        binding.tvPrivacy.setOnClickListener(v -> showInfoDialog(
                "Privacy Policy",
                "• We do not store your messages, contacts, or media.\n" +
                        "• We store only session identifiers and presence logs required to provide the service.\n" +
                        "• Device tokens are used solely for delivering notifications.\n" +
                        "• Data is never sold to third parties."
        ));
    }

    private void observeVm() {
        viewModel.getLoading().observe(this, loading -> {
            binding.loader.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.qrCard.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        });

        viewModel.getQrData().observe(this, qrResponse -> {
            if (qrResponse == null || qrResponse.getQr() == null) {
                // Handle the case where QR data or the QR field is null
                Log.d("QrLinkActivity", "QR data or QR link is null.");
                return;
            }

            String qrUrl = qrResponse.getQr();
            Log.d("QrLinkActivity", "Received QR URL: " + qrUrl);

            // ✅ FIX: Removed Glide and instead set the URL to the TextView
            binding.tvQrLink.setText(qrUrl);
            binding.tvQrLink.setVisibility(View.VISIBLE); // Ensure the TextView is visible
        });

        viewModel.getUiStatus().observe(this, binding.statusText::setText);

        viewModel.getCountdown().observe(this, msLeft -> {
            long sec = TimeUnit.MILLISECONDS.toSeconds(msLeft);
            long mm = sec / 60;
            long ss = sec % 60;
            binding.expireText.setText(msLeft > 0
                    ? String.format("Expires in %02d:%02d", mm, ss)
                    : "Expired");
        });

        viewModel.getLinked().observe(this, ok -> {
            if (ok != null && ok) {
                // 🎉 Navigate to Dashboard
                Toast.makeText(this, "Linked successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void showInfoDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Okay", (d, w) -> d.dismiss())
                .show();
    }
}