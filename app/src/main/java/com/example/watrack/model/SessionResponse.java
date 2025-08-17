package com.example.watrack.model;

public class SessionResponse {

    public String userId;
    private String sessionId;
    private String status; // "pending" | "connected"

    public String getSessionId() { return sessionId; }
    public String getStatus() { return status; }

    public String getUserId() { return userId; }

}
