package com.example.watrack.model;

public class Contact {
    private String name;
    private int avatarResId;
    private String lastSeen;
    private boolean isOnline;
    private String duration;

    public Contact(String name, int avatarResId, String lastSeen, boolean isOnline, String duration) {
        this.name = name;
        this.avatarResId = avatarResId;
        this.lastSeen = lastSeen;
        this.isOnline = isOnline;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getAvatarResId() {
        return avatarResId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return name.equals(contact.name);
    }
}