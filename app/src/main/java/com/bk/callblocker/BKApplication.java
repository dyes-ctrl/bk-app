package com.bk.callblocker;

import android.app.Application;

/**
 * Application entry point.
 * Pre-warms the whitelist cache before any CallScreeningService request arrives,
 * so onScreenCall() has zero I/O latency when it checks the whitelist.
 */
public class BKApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WhitelistCache.reload(this);
    }
}
