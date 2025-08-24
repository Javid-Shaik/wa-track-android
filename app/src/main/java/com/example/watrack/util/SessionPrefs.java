package com.example.watrack.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public final class SessionPrefs {

    // Legacy plain prefs (for migration)
    private static final String PLAIN_PREF = "watrack_prefs";
    // New encrypted prefs file
    private static final String SECURE_PREF = "watrack_prefs.secure";

    // Keys (keep legacy key for migration)
    private static final String KEY_SESSION_ID = "wa_session_id";

    // New fields for local-first routing
    private static final String KEY_STATUS = "wa_session_status";         // "LINKED", "PENDING", "NONE"
    private static final String KEY_UPDATED_AT = "wa_session_updated_at"; // epoch millis
    private static final String KEY_TTL_SECONDS = "wa_session_ttl_seconds";

    private static volatile SharedPreferences prefs; // cached instance

    private SessionPrefs() {}

    private static SharedPreferences getPrefs(Context ctx) {
        if (prefs == null) {
            synchronized (SessionPrefs.class) {
                if (prefs == null) {
                    try {
                        Context app = ctx.getApplicationContext();
                        MasterKey masterKey = new MasterKey.Builder(app)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                .build();

                        SharedPreferences encrypted = EncryptedSharedPreferences.create(
                                app,
                                SECURE_PREF,
                                masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        );

                        migrateIfNeeded(app, encrypted);
                        prefs = encrypted;
                    } catch (Exception e) {
                        // Fail-safe: fall back to legacy plain prefs to avoid crashes.
                        // Consider logging this exception and investigating why encryption failed.
                        prefs = ctx.getApplicationContext()
                                .getSharedPreferences(PLAIN_PREF, Context.MODE_PRIVATE);
                    }
                }
            }
        }
        return prefs;
    }

    // Move existing sessionId from plain prefs to encrypted prefs (one-time).
    private static void migrateIfNeeded(Context app, SharedPreferences encrypted) {
        // If the encrypted store already has sessionId, assume migrated/initialized.
        if (encrypted.contains(KEY_SESSION_ID)) return;

        SharedPreferences legacy = app.getSharedPreferences(PLAIN_PREF, Context.MODE_PRIVATE);
        if (legacy.contains(KEY_SESSION_ID)) {
            String id = legacy.getString(KEY_SESSION_ID, null);
            if (id != null) {
                encrypted.edit()
                        .putString(KEY_SESSION_ID, id)
                        .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
                        .apply();
            }
            // Clear legacy to avoid divergence
            legacy.edit().remove(KEY_SESSION_ID).apply();
        }
    }

    // -----------------------
    // Backward-compatible API
    // -----------------------
    public static void saveSessionId(Context ctx, String sessionId) {
        SharedPreferences sp = getPrefs(ctx);
        sp.edit().putString(KEY_SESSION_ID, sessionId).apply();
        sp.edit().putLong(KEY_UPDATED_AT, System.currentTimeMillis()).apply();
        // Note: does not modify status/ttl; use saveSession(...) for richer caching.
    }

    @Nullable
    public static String getSessionId(Context ctx) {
        return getPrefs(ctx).getString(KEY_SESSION_ID, null);
    }

    public static void clearSessionId(Context ctx) {
        getPrefs(ctx).edit()
                .remove(KEY_SESSION_ID)
                .remove(KEY_STATUS)
                .remove(KEY_UPDATED_AT)
                .remove(KEY_TTL_SECONDS)
                .apply();
    }

    // -----------------------
    // New richer cache API
    // -----------------------
    public static void saveSession(Context ctx, String sessionId, String status, long ttlSeconds) {
        long now = System.currentTimeMillis();
        getPrefs(ctx).edit()
                .putString(KEY_SESSION_ID, sessionId)
                .putString(KEY_STATUS, status)         // expected: "LINKED", "PENDING", "NONE"
                .putLong(KEY_TTL_SECONDS, ttlSeconds)  // server-suggested TTL (seconds) or 0
                .putLong(KEY_UPDATED_AT, now)
                .apply();
    }

    @Nullable
    public static SessionCache getCachedSession(Context ctx) {
        SharedPreferences sp = getPrefs(ctx);
        String id = sp.getString(KEY_SESSION_ID, null);
        if (id == null) return null;
        String status = sp.getString(KEY_STATUS, null);
        long updatedAt = sp.getLong(KEY_UPDATED_AT, 0L);
        long ttl = sp.getLong(KEY_TTL_SECONDS, 0L);
        return new SessionCache(id, status, updatedAt, ttl);
    }

    public static final class SessionCache {
        public final String sessionId;
        @Nullable public final String status; // may be null after legacy migration
        public final long updatedAt;
        public final long ttlSeconds;

        public SessionCache(String sessionId, @Nullable String status, long updatedAt, long ttlSeconds) {
            this.sessionId = sessionId;
            this.status = status;
            this.updatedAt = updatedAt;
            this.ttlSeconds = ttlSeconds;
        }

        // Use defaultTtlSeconds if server didn't provide one.
        public boolean isFresh(long defaultTtlSeconds) {
            long ttl = ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds;
            if (ttl <= 0 || updatedAt <= 0) return false;
            long ageMs = System.currentTimeMillis() - updatedAt;
            return ageMs <= ttl * 1000L;
        }
    }
}
