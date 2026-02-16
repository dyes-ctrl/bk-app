package com.secure.taxapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.secure.taxapp.ConfigManager;

/**
 * Receiver pour redemarrage du service
 */
public class ServiceRestartReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        ConfigManager configManager = new ConfigManager(context);
        
        if (configManager.isServiceActive()) {
            Intent serviceIntent = new Intent(context, CalculationService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
