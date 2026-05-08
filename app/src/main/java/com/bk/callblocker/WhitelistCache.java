package com.bk.callblocker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Static in-memory cache of the whitelist.
 * Eliminates SharedPreferences disk I/O from the critical call-screening path.
 * Loaded once at app startup (BKApplication.onCreate) and refreshed after any mutation.
 */
public final class WhitelistCache {

    private static volatile Set<String> numbers = Collections.emptySet();
    private static volatile boolean enabled      = true;
    private static volatile boolean blockPrivate = true;

    private WhitelistCache() {}

    /** Load (or refresh) the cache from SharedPreferences. */
    public static synchronized void reload(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences("BKPrefs", Context.MODE_PRIVATE);

        enabled      = prefs.getBoolean("enabled", true);
        blockPrivate = prefs.getBoolean("block_private", true);

        Set<String> raw = prefs.getStringSet("whitelist", new HashSet<>());
        numbers = Collections.unmodifiableSet(new HashSet<>(raw));
    }

    public static boolean isEnabled()          { return enabled; }
    public static boolean isPrivateBlocked()   { return blockPrivate; }
    public static Set<String> getNumbers()     { return numbers; }
}
