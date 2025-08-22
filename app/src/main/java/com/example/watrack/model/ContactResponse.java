package com.example.watrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ContactResponse {
    @SerializedName("contacts")
    private List<Contact> contacts;

    public List<Contact> getContacts() {
        return contacts;
    }
}