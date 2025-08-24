package com.example.watrack.repository;

import com.example.watrack.model.ActivityLogResponse;
import com.example.watrack.model.Contact;
import com.example.watrack.model.ContactResponse;
import com.example.watrack.model.QrResponse;
import com.example.watrack.model.RegisterRequest;
import com.example.watrack.model.SessionResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //  Auth
    @POST("/api/auth/register")
    Call<Void> registerUser(@Body RegisterRequest request);

    //  Session management
    @POST("/api/wa/create-session")
    Call<SessionResponse> createSession(@Body Map<String, Object> body);

    @POST("/api/wa/ensure-session")
    Call<SessionResponse> ensureSession(@Body Map<String, String> body);

    @GET("/api/wa/session-by-user/{firebaseUid}")
    Call<SessionResponse> getSessionByUser(@Path("firebaseUid") String firebaseUid);

    @POST("/api/wa/start-client")
    Call<Void> startClient(@Body Map<String, Object> body);

    @POST("/api/wa/end-session")
    Call<Void> endSession(@Body Map<String, Object> body);

    //  QR
    @GET("/api/wa/qr-code/{sessionId}")
    Call<QrResponse> getQr(@Path("sessionId") String sessionId);

    @GET("/api/wa/qr-code/{sessionId}")
    Call<QrResponse> getQr2(@Path("sessionId") String sessionId);

    //  Polling status
    @GET("/api/wa/status/{sessionId}")
    Call<SessionResponse> getUserSession(@Path("sessionId") String sessionId);

    //  Subscription
    @POST("/api/wa/subscribe")
    Call<Map<String, Object>> subscribe(@Body Map<String, Object> body);

    @POST("/api/wa/unsubscribe")
    Call<Map<String, Object>> unsubscribe(@Body Map<String, Object> body);

    //  Profile & contacts
    @GET("/api/wa/profile-pic/{sessionId}/{phoneNumber}")
    Call<Map<String, Object>> getProfilePic(
            @Path("sessionId") String sessionId,
            @Path("phoneNumber") String phoneNumber
    );

    @GET("/api/wa/in-contacts/{sessionId}/{phoneNumber}")
    Call<Map<String, Object>> checkInContacts(
            @Path("sessionId") String sessionId,
            @Path("phoneNumber") String phoneNumber
    );

    //  Tracking
    @GET("/api/wa/history/{trackingId}")
    Call<Map<String, Object>> getHistory(@Path("trackingId") String trackingId);

    @GET("/api/wa/last-seen/{trackingId}")
    Call<Map<String, Object>> getLastSeen(@Path("trackingId") String trackingId);

    @GET("/api/tracked/contacts/{firebaseUid}")
    Call<ContactResponse> getContacts(@Path("firebaseUid") String firebaseUid);

    // Fetches the list of activity logs for a specific contact.
    @GET("/api/wa/activity_logs")
    Call<ActivityLogResponse> getActivityLogs(@Query("contactId") String contactId);

    // Adds a new contact to the backend.
    @POST("/api/wa/contacts")
    Call<Void> addContact(@Body Contact contact);
}
