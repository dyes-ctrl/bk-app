package com.secure.taxapp;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.secure.taxapp.services.CalculationService;

/**
 * Application principale.
 * Demarre le service de surveillance DND au lancement.
 */
public class TaxCalculatorApplication extends Application {

    private static final String TAG = "TCA";

    @Override
    public void onCreate() {
        super.onCreate();

        ConfigManager configManager = new ConfigManager(this);
        if (configManager.isServiceActive()) {
            try {
                Intent serviceIntent = new Intent(this, CalculationService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                Log.d(TAG, "DND monitoring service started");
            } catch (Exception e) {
                Log.e(TAG, "Failed to start service", e);
            }
        }
    }
}
