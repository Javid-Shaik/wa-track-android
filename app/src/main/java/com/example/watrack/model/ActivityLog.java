package com.example.watrack.model;
public class ActivityLog {
    private String text;
    private String time;

    public ActivityLog(String text, String time) {
        this.text = text;
        this.time = time;
    }

    public String getText() { return text; }
    public String getTime() { return time; }

    public void setText(String text) { this.text = text; }
    public void setTime(String time) { this.time = time; }
}