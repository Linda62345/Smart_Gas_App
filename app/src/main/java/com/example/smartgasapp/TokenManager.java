package com.example.smartgasapp;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static TokenManager instance;
    private final SharedPreferences sharedPreferences;
    private final String ACCESS_TOKEN_KEY = "access_token";
    private final String EXPIRATION_TIMESTAMP_KEY = "expiration_timestamp";

    private TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences("token_data", Context.MODE_PRIVATE);
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void setAccessToken(String accessToken, long expirationTimestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.putLong(EXPIRATION_TIMESTAMP_KEY, expirationTimestamp);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    public long getExpirationTimestamp() {
        return sharedPreferences.getLong(EXPIRATION_TIMESTAMP_KEY, 0);
    }

    public boolean isTokenValid() {
        long currentTimestamp = System.currentTimeMillis() / 1000;
        long expirationTimestamp = getExpirationTimestamp();
        return expirationTimestamp > currentTimestamp;
    }

    public void clearToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.remove(EXPIRATION_TIMESTAMP_KEY);
        editor.apply();
    }
}
