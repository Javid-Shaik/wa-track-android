package com.example.watrack.model;

import com.google.gson.annotations.SerializedName;

public class QrResponse {

    @SerializedName("qr")
    private String qr;

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }
}