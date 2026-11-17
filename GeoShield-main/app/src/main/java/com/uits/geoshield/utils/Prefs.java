package com.uits.geoshield.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Thin wrapper over SharedPreferences for app settings. */
public final class Prefs {
    public static final String LANG_BN = "bn";
    public static final String LANG_EN = "en";

    private static final String FILE = "geoshield_prefs";
    private static final String K_NAME = "user_name";
    private static final String K_PHONE = "user_phone";
    private static final String K_LANG = "lang";
    private static final String K_CALLER = "caller_name";
    private static final String K_DELAY = "call_delay";
    private static final String K_RING = "ringtone";

    private final SharedPreferences sp;

    public Prefs(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public String getUserName() { return sp.getString(K_NAME, "Ayesha"); }
    public void setUserName(String v) { sp.edit().putString(K_NAME, v).apply(); }

    public String getPhone() { return sp.getString(K_PHONE, ""); }
    public void setPhone(String v) { sp.edit().putString(K_PHONE, v).apply(); }

    public String getLanguage() { return sp.getString(K_LANG, LANG_BN); }
    public void setLanguage(String v) { sp.edit().putString(K_LANG, v).apply(); }

    public String getCallerName() { return sp.getString(K_CALLER, "Ammu"); }
    public void setCallerName(String v) { sp.edit().putString(K_CALLER, v).apply(); }

    public int getCallDelaySeconds() { return sp.getInt(K_DELAY, 5); }
    public void setCallDelaySeconds(int v) { sp.edit().putInt(K_DELAY, v).apply(); }

    public boolean isRingtoneEnabled() { return sp.getBoolean(K_RING, true); }
    public void setRingtoneEnabled(boolean v) { sp.edit().putBoolean(K_RING, v).apply(); }
}
