package com.example.watrack.repository;

import com.example.watrack.model.LinkStatusResponse;
import com.example.watrack.model.QrResponse;
import com.example.watrack.model.RegisterRequest;
import com.example.watrack.model.SessionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.Map;

public interface ApiService {
    @POST("/api/auth/register") Call<Void> registerUser(@Body RegisterRequest request);
    // Create a new sessionId (backend generates one)
    @POST("/api/wa/create-session")
    Call<SessionResponse> createSession(@Body Map<String, Object> body);

    // Tell backend to start/resume client
    @POST("/api/wa/start-client")
    Call<Void> startClient(@Body Map<String, Object> body);

    // Get QR data URL (base64 data:image/png... or object with data)
    @GET("/api/wa/qr-code/{sessionId}")
    Call<Map<String, Object>> getQr(@Path("sessionId") String sessionId);

    // Or, if backend returns a wrapper QrResponse:
    @GET("/api/wa/qr-code/{sessionId}")
    Call<QrResponse> getQr2(@Path("sessionId") String sessionId);

    // Poll status
    @GET("/api/wa/status/{sessionId}")
    Call<LinkStatusResponse> getStatus(@Path("sessionId") String sessionId);

    // Subscribe to a phoneNumber for presence (app-level)
    @POST("/api/wa/subscribe")
    Call<Map<String, Object>> subscribe(@Body Map<String, Object> body);

    @GET("api/session/status")
    Call<SessionResponse> getSessionStatus(@Query("sessionId") String sessionId);

    @POST("api/session/refresh")
    Call<SessionResponse> refreshQr(@Query("sessionId") String sessionId);

}
