package com.example.watrack.model;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

@Keep // Prevents proguard/shrinker from stripping/renaming fields used by JSON
public final class SessionResponse {

    // Server/user identifiers
    @SerializedName("userId")
    private String userId;

    // Session info
    @SerializedName("sessionId")
    private String sessionId;

    // Server returns: "pending" | "connected"
    @SerializedName("status")
    private String status;

    // Optional profile/context
    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("userName")
    private String userName;

    @SerializedName("profilePicUrl")
    private String profilePicUrl;

    @SerializedName("online")
    private boolean online;

    @SerializedName("lastSeen")
    private String lastSeen;

    // Server-suggested cache TTL in seconds (may be 0 / absent)
    @SerializedName("ttlSeconds")
    private long ttlSeconds;

    // Status constants to avoid typos across the app
    public static final String STATUS_CONNECTED = "LINKED";
    public static final String STATUS_PENDING = "PENDING";

    // Convenience checks
    public boolean isConnected() {
        return STATUS_CONNECTED.equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status);
    }

    // Getters (no setters needed for typical JSON libs)
    @Nullable public String getUserId() { return userId; }
    @Nullable public String getSessionId() { return sessionId; }
    @Nullable public String getStatus() { return status; }

    @Nullable public String getPhoneNumber() { return phoneNumber; }
    @Nullable public String getUserName() { return userName; }
    @Nullable public String getProfilePicUrl() { return profilePicUrl; }

    public boolean isOnline() { return online; }
    @Nullable public String getLastSeen() { return lastSeen; }

    public long getTtlSeconds() { return ttlSeconds; }

    @Override
    public String toString() {
        return "SessionResponse{" +
                "userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", status='" + status + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", userName='" + userName + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", online=" + online +
                ", lastSeen='" + lastSeen + '\'' +
                ", ttlSeconds=" + ttlSeconds +
                '}';
    }
}
