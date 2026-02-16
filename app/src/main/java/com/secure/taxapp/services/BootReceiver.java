package com.secure.taxapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.secure.taxapp.ConfigManager;

/**
 * Receiver pour demarrage automatique
 */
public class BootReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ConfigManager configManager = new ConfigManager(context);
            
            // Demarrer le service si actif
            if (configManager.isServiceActive()) {
                Intent serviceIntent = new Intent(context, CalculationService.class);
                context.startForegroundService(serviceIntent);
            }
        }
    }
}
