package com.secure.taxapp.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.secure.taxapp.ConfigManager;
import com.secure.taxapp.MainActivity;

/**
 * Service de fond persistant.
 *
 * Roles :
 * 1. Surveiller periodiquement que le DND est bien actif
 *    (EMUI/Huawei peut desactiver le DND sans prevenir)
 * 2. Le retablir automatiquement si necessaire
 * 3. Garder l'app vivante en arriere-plan
 */
public class CalculationService extends Service {

    private static final String TAG = "CS";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "calculation_channel";
    
    public static final String ACTION_RESTORE_RINGER = "RESTORE_RINGER";
    public static final String ACTION_CHECK_DND = "CHECK_DND";

    // Verifier le DND toutes les 2 minutes
    private static final long DND_CHECK_INTERVAL_MS = 2 * 60 * 1000;

    private Handler handler;
    private ConfigManager configManager;
    private Runnable dndCheckRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        configManager = new ConfigManager(this);
        createNotificationChannel();
        startDndMonitoring();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Demarrer en foreground IMMEDIATEMENT
        startForeground(NOTIFICATION_ID, createNotification());

        // Traiter les actions specifiques
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_CHECK_DND.equals(action)) {
                configManager.ensureDndState();
            }
            // ACTION_RESTORE_RINGER n'est plus necessaire avec le DND
            // (le DND gere tout au niveau systeme)
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Quand l'utilisateur ferme l'app, programmer un redemarrage
        scheduleServiceRestart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Arreter la surveillance DND
        if (dndCheckRunnable != null) {
            handler.removeCallbacks(dndCheckRunnable);
        }
        // Programmer un redemarrage via AlarmManager
        if (configManager.isServiceActive()) {
            scheduleServiceRestart();
        }
    }

    /**
     * Demarre la surveillance periodique du mode DND.
     * EMUI peut desactiver le DND sans prevenir -> on le surveille.
     */
    private void startDndMonitoring() {
        dndCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (configManager.isServiceActive()) {
                    configManager.ensureDndState();
                    Log.d(TAG, "DND check performed");
                }
                handler.postDelayed(this, DND_CHECK_INTERVAL_MS);
            }
        };
        // Premier check apres 5 secondes, puis toutes les 2 min
        handler.postDelayed(dndCheckRunnable, 5000);
    }

    /**
     * Programme un redemarrage du service via AlarmManager.
     * Plus fiable que startService() dans onDestroy() sur EMUI.
     */
    private void scheduleServiceRestart() {
        try {
            Intent restartIntent = new Intent(this, ServiceRestartReceiver.class);
            restartIntent.setAction("RESTART_SERVICE");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 1001, restartIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 5000,
                        pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 5000,
                        pendingIntent
                    );
                }
                Log.d(TAG, "Service restart scheduled via AlarmManager");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule restart", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Calculs",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Calculs en arriere-plan");
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BK")
            .setContentText("Filtrage actif")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .build();
    }
}
