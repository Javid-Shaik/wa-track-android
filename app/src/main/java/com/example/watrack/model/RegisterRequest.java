package com.example.watrack.model;

public class RegisterRequest {
    private String idToken;
    private String deviceToken;
    private int subscriptionLimit;

    public RegisterRequest(String idToken, String deviceToken, int subscriptionLimit) {
        this.idToken = idToken;
        this.deviceToken = deviceToken;
        this.subscriptionLimit = subscriptionLimit;
    }
}
