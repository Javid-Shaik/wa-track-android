package com.example.watrack.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ActivityLogResponse {
    @SerializedName("activityLogs")
    private List<ActivityLog> activityLogs;

    public List<ActivityLog> getActivityLogs() {
        return activityLogs;
    }
}
