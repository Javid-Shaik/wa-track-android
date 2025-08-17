package com.example.watrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watrack.R;
import com.example.watrack.TrackedUsersActivity;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity {
    private EditText phoneNumberInput;
    private Button trackButton, viewTrackedButton;
    private static final String BASE_URL = "http://localhost:3000/";
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        trackButton = findViewById(R.id.trackButton);
        viewTrackedButton = findViewById(R.id.viewTrackedButton);



        // Initialize Retrofit client
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();
        apiService = retrofit.create(ApiService.class);

        trackButton.setOnClickListener(v -> addPhoneNumberToTrack());
        viewTrackedButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrackedUsersActivity.class)));
    }

    private void addPhoneNumberToTrack() {
        String phoneNumber = phoneNumberInput.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10) {
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request body that your Node.js API expects
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("phoneNumber", phoneNumber);

        // Make the network call to the Node.js backend
        apiService.subscribeToTracking(requestBody).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    phoneNumberInput.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MainActivity", "Network Failure", t);
            }
        });
    }

    // POJO for API response, assuming your Node.js server returns a message on success
    public static class ApiResponse {
        private String message;

        public String getMessage() {
            return message;
        }
    }

    // Retrofit Interface to define the API endpoint
    // Assumes a POST endpoint at /api/tracked/subscribe
    public interface ApiService {
        @POST("/api/tracker/subscribe")
        Call<ApiResponse> subscribeToTracking(@Body Map<String, Object> requestBody);
    }
}