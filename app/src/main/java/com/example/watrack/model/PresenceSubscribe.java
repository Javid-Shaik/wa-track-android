package com.example.watrack.model;

public class PresenceSubscribe {

    private String phoneNumber;
    private String firebaseUid;
    private String sessionId;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSessionId() {        return sessionId;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }
}
