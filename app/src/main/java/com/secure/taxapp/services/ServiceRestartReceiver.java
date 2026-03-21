package com.secure.taxapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.secure.taxapp.ConfigManager;

/**
 * Receiver pour redemarrage du service
 * Gere les cas ou le service est tue par le systeme ou EMUI
 */
public class ServiceRestartReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SRR";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ServiceRestartReceiver triggered: " + 
            (intent != null ? intent.getAction() : "null"));
        
        ConfigManager configManager = new ConfigManager(context);
        
        if (configManager.isServiceActive()) {
            try {
                Intent serviceIntent = new Intent(context, CalculationService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                Log.d(TAG, "Service restarted successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to restart service", e);
            }
        }
    }
}
