package com.example.watrack.model;

public class QrResponse {
    private String qr;          // some backends return 'qr' as dataUrl
    private String qrImageUrl;  // optional
    private String sessionId;
    private long expiresAt;

    public String getQr() { return qr; }
    public String getQrImageUrl() { return qrImageUrl; }
    public String getSessionId() { return sessionId; }
    public long getExpiresAt() { return expiresAt; }
}
