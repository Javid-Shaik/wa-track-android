package com.example.watrack.model;

public class LinkStatusResponse {
    private boolean linked;
    private String status; //  "WAITING_FOR_SCAN", "SCANNED", "READY"

    public boolean isLinked() { return linked; }
    public String getStatus() { return status; }
}
