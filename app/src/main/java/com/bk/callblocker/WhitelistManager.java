package com.bk.callblocker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhitelistManager {

    private static final String PREFS_NAME    = "BKPrefs";
    private static final String KEY_NUMBERS   = "whitelist";
    private static final String KEY_ENABLED   = "enabled";
    private static final String KEY_BLOCK_PRIVATE = "block_private";

    private final SharedPreferences prefs;
    private final Context appContext;

    public WhitelistManager(Context context) {
        appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, true);
    }

    public void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
        WhitelistCache.reload(appContext);
    }

    public boolean isPrivateNumberBlocked() {
        return prefs.getBoolean(KEY_BLOCK_PRIVATE, true);
    }

    public void setBlockPrivateNumbers(boolean block) {
        prefs.edit().putBoolean(KEY_BLOCK_PRIVATE, block).apply();
        WhitelistCache.reload(appContext);
    }

    public List<String> getNumbers() {
        Set<String> set = prefs.getStringSet(KEY_NUMBERS, new HashSet<>());
        return new ArrayList<>(set);
    }

    public void addNumber(String raw) {
        String normalized = normalize(raw);
        if (normalized == null || normalized.length() < 4) return;
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_NUMBERS, new HashSet<>()));
        set.add(normalized);
        prefs.edit().putStringSet(KEY_NUMBERS, set).apply();
        WhitelistCache.reload(appContext);
    }

    public void removeNumber(String number) {
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_NUMBERS, new HashSet<>()));
        set.remove(number);
        prefs.edit().putStringSet(KEY_NUMBERS, set).apply();
        WhitelistCache.reload(appContext);
    }

    public boolean isAllowed(String incomingNumber) {
        if (incomingNumber == null) return false;
        String normalized = normalize(incomingNumber);
        if (normalized == null) normalized = incomingNumber.replaceAll("[\\s.\\-()]", "");

        Set<String> stored = prefs.getStringSet(KEY_NUMBERS, new HashSet<>());
        for (String s : stored) {
            if (numbersMatch(s, normalized)) return true;
        }
        return false;
    }

    // Normalize phone number to a canonical form
    public static String normalize(String number) {
        if (number == null) return null;
        String d = number.replaceAll("[\\s.\\-()]+", "");
        if (d.isEmpty()) return null;

        if (d.startsWith("0033")) {
            d = "+" + d.substring(2);
        }
        if (d.matches("^0[67]\\d{8}$")) {
            d = "+33" + d.substring(1);
        }
        if (d.matches("^0[1-9]\\d{8}$")) {
            d = "+33" + d.substring(1);
        }
        return d;
    }

    private boolean numbersMatch(String a, String b) {
        String aD = a.replaceAll("[^\\d]", "");
        String bD = b.replaceAll("[^\\d]", "");
        if (aD.isEmpty() || bD.isEmpty()) return a.equals(b);
        int len = Math.min(9, Math.min(aD.length(), bD.length()));
        if (len < 7) return aD.equals(bD);
        return aD.substring(aD.length() - len).equals(bD.substring(bD.length() - len));
    }
}
