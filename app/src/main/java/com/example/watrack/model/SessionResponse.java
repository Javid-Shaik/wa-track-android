package com.example.watrack.model;

public class SessionResponse {
    private String sessionId;
    private String qr;     // base64 QR or URL
    private String status; // "pending" | "connected"

    public String getSessionId() { return sessionId; }
    public String getQr() { return qr; }
    public String getStatus() { return status; }

    public String getQrImageUrl() {
        if (qr == null) return null;
        if (qr.startsWith("data:image")) return qr;
        return "data:image/png;base64," + qr;
    }
}
