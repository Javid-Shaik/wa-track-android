package com.example.watrack;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDetailsActivity extends AppCompatActivity {
    private TextView phoneNumberTextView, totalOnlineTimeTextView;
    private WebView chartWebView;
    private FirebaseFirestore db;
    private String phoneNumber;
    private List<String> timestamps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        totalOnlineTimeTextView = findViewById(R.id.totalOnlineTimeTextView);
        chartWebView = findViewById(R.id.chartWebView);
        db = FirebaseFirestore.getInstance();

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber == null) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        phoneNumberTextView.setText("Tracking: " + phoneNumber);
        fetchOnlineStatus();
    }

    private void fetchOnlineStatus() {
        db.collection("user_status_logs")
                .whereEqualTo("phoneNumber", phoneNumber)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            processStatusData(result.getDocuments());
                        } else {
                            Toast.makeText(this, "No logs found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Error fetching logs", task.getException());
                    }
                });
    }

    private void processStatusData(List<DocumentSnapshot> logs) {
        long totalOnlineTime = 0;
        StringBuilder timeData = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        for (DocumentSnapshot doc : logs) {
            long timestamp = doc.getLong("timestamp");
            boolean isOnline = doc.getBoolean("isOnline");

            timestamps.add(dateFormat.format(timestamp));
            timeData.append("['").append(dateFormat.format(timestamp)).append("', ").append(isOnline ? "1" : "0").append("],");

            if (isOnline) {
                totalOnlineTime += 60; // Assume each log entry represents 1-minute interval
            }
        }

        totalOnlineTimeTextView.setText("Total Online Time: " + (totalOnlineTime / 60) + " mins");
        loadChart(timeData.toString());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadChart(String timeData) {
        String chartHtml = "<html><head>"
                + "<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>"
                + "</head><body>"
                + "<canvas id='statusChart'></canvas>"
                + "<script>"
                + "var ctx = document.getElementById('statusChart').getContext('2d');"
                + "var chart = new Chart(ctx, {"
                + "    type: 'line',"
                + "    data: {"
                + "        labels: [" + timestamps.toString() + "],"
                + "        datasets: [{"
                + "            label: 'Online Status',"
                + "            data: [" + timeData + "],"
                + "            borderColor: 'blue',"
                + "            fill: false"
                + "        }]"
                + "    }"
                + "});"
                + "</script></body></html>";

        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        chartWebView.loadData(chartHtml, "text/html", "UTF-8");
    }
}
