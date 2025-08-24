package com.example.watrack.model;
public class Contact {
    private String name;
    private String profilePicUrl; // <-- changed from int to String
    private String lastSeen;
    private boolean isOnline;
    private String duration;

    public Contact(String name, String profilePicUrl, String lastSeen, boolean isOnline, String duration) {
        this.name = name;
        this.profilePicUrl = profilePicUrl;
        this.lastSeen = lastSeen;
        this.isOnline = isOnline;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getDuration() {
        return duration;
    }
}
