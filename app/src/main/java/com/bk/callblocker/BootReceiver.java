package com.bk.callblocker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class BootReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "bk_reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager rm = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
            if (rm != null && !rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                notifyRoleLost(context);
            }
            // Role is still held -> CallScreeningService will be bound automatically by system
        }
    }

    private void notifyRoleLost(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "BK Rappel", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(ch);
        }

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setContentTitle("BK — Filtre désactivé")
                .setContentText("Touchez pour réactiver le filtre d'appels")
                .setContentIntent(pi)
                .setAutoCancel(true);

        nm.notify(1001, builder.build());
    }
}
