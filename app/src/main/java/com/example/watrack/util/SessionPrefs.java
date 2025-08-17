package com.example.watrack.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionPrefs {
    private static final String PREF = "watrack_prefs";
    private static final String KEY_SESSION = "wa_session_id";

    public static void saveSessionId(Context ctx, String sessionId) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(KEY_SESSION, sessionId).apply();
    }

    public static String getSessionId(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY_SESSION, null);
    }

    public static void clearSessionId(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().remove(KEY_SESSION).apply();
    }
}
