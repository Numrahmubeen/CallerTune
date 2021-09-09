package com.caller.tune.params;

import android.content.Context;
import android.content.SharedPreferences;

public class Preference {
    Context context;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    public final static String PREFS_NAME = "ongoingCall";

    public Preference(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, 0);
        editor = prefs.edit();
    }

    public void setNumber(String value) {
        editor.putString("number", value);
        editor.apply();
    }

    public String getNumber() {
        return prefs.getString("number", "empty");
    }

    public void setRingMode(int value) {
        editor.putInt("ringMode", value);
        editor.apply();
    }

    public int getRingMode() {
        return prefs.getInt("ringMode", 1111);
    }
    public void setRequiredRingMode(int value) {
        editor.putInt("requiredRingMode", value);
        editor.apply();
    }
    public int getRequiredRingMode() {
        return prefs.getInt("requiredRingMode", 1111);
    }
    public void setRingerModeName(String value) {
        editor.putString("ringerModeName", value);
        editor.apply();
    }

    public String getRingerModeName() {
        return prefs.getString("ringerModeName", "empty");
    }
}
