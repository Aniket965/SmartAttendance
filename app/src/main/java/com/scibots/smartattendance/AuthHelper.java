package com.scibots.smartattendance;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AuthHelper {
    private static final String PREFS = "prefs";
    private static final String PREF_TOKEN = "pref_token";
    private static final String PREF_USER_NAME = "pref_name";
    private static final String PREF_USER_EMAIl = "pref_email";
    private SharedPreferences mPrefs;

    private static AuthHelper sInstance;
    private AuthHelper(@NonNull Context context) {
        mPrefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sInstance = this;
    }

    public static AuthHelper getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new AuthHelper(context);
        }
        return sInstance;
    }


    public void setUserName(@NonNull String name) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_USER_NAME, name);
        editor.apply();
    }
    public void setUserEmail(@NonNull String email) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_USER_EMAIl, email);
        editor.apply();
    }


    @Nullable
    public String getUserName() {
        return mPrefs.getString(PREF_USER_NAME, null);
    }

    @Nullable
    public String getUserEmail() {
        return mPrefs.getString(PREF_USER_EMAIl, null);
    }

    public void setIdToken(@NonNull String token) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_TOKEN, token);
        editor.apply();
    }

    @Nullable
    public String getIdToken() {
        return mPrefs.getString(PREF_TOKEN, null);
    }

    public boolean isLoggedIn() {
        String token = getIdToken();
        return token != null;
    }


    public void clear() {
        mPrefs.edit().clear().commit();
    }
}
