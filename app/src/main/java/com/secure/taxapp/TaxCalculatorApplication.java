package com.secure.taxapp;

import android.app.Application;
import android.content.Intent;
import com.secure.taxapp.services.CalculationService;

/**
 * Application principale
 */
public class TaxCalculatorApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Verifier si le service doit demarrer
        ConfigManager configManager = new ConfigManager(this);
        if (configManager.isServiceActive()) {
            Intent serviceIntent = new Intent(this, CalculationService.class);
            startForegroundService(serviceIntent);
        }
    }
}
