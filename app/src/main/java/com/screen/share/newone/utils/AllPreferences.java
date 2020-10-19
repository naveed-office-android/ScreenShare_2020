package com.screen.share.newone.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AllPreferences {
    public static final String MY_PREF = "MyPreferences";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public AllPreferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(MY_PREF, 0);
        this.editor = this.sharedPreferences.edit();
    }

    public void set(String key, String value) {
        this.editor.putString(key, value);
        this.editor.apply();
    }
    public void set(String key, boolean value) {
        this.editor.putBoolean(key, value);
        this.editor.apply();
    }



    public String get(String key) {
        return this.sharedPreferences.getString(key, "");
    }
    public boolean getbol(String key) {
        return this.sharedPreferences.getBoolean(key, false);
    }

    public void clear(String key) {
        this.editor.remove(key);
        this.editor.apply();
    }



    public void clear() {
        this.editor.clear();
        this.editor.apply();
    }
}