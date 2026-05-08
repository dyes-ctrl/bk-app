package com.bk.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * High-priority BroadcastReceiver for incoming calls.
 *
 * Registered at android:priority="2147483647" so it fires BEFORE MIUI's own
 * telephony UI has a chance to display the ringing screen.
 *
 * When a number that is NOT in the whitelist is detected in RINGING state,
 * we call TelecomManager.endCall() (API 28+, requires ANSWER_PHONE_CALLS).
 * Fallback: ITelephony.endCall() via reflection (works on some MIUI builds).
 *
 * This layer works ALONGSIDE CallScreeningService. The screening service is the
 * primary mechanism; this receiver is the zero-latency safety net for MIUI.
 */
public class CallInterceptor extends BroadcastReceiver {

    private static final String TAG = "BK-Interceptor";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) return;

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) return;

        // Ensure cache is up-to-date (may be first run after process restart)
        WhitelistCache.reload(context);

        if (!WhitelistCache.isEnabled()) return;

        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        boolean isPrivate = incomingNumber == null
                || incomingNumber.isEmpty()
                || incomingNumber.equalsIgnoreCase("anonymous")
                || incomingNumber.equalsIgnoreCase("unknown")
                || incomingNumber.equalsIgnoreCase("blocked");

        if (isPrivate) {
            if (WhitelistCache.isPrivateBlocked()) {
                Log.d(TAG, "Private number detected — ending call");
                endCall(context);
            }
            return;
        }

        String normalized = WhitelistManager.normalize(incomingNumber);
        if (normalized == null) normalized = incomingNumber.replaceAll("[\\s.\\-()]", "");

        boolean allowed = false;
        for (String stored : WhitelistCache.getNumbers()) {
            if (numbersMatch(stored, normalized)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            Log.d(TAG, "Number not whitelisted — ending call immediately: " + normalized);
            endCall(context);
        }
    }

    private void endCall(Context context) {
        // Primary: TelecomManager.endCall() — API 28+, requires ANSWER_PHONE_CALLS
        try {
            TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (tm != null) {
                boolean ended = tm.endCall();
                if (ended) {
                    Log.d(TAG, "Call ended via TelecomManager");
                    return;
                }
            }
        } catch (SecurityException se) {
            Log.w(TAG, "TelecomManager.endCall() denied — trying reflection fallback");
        } catch (Exception e) {
            Log.w(TAG, "TelecomManager.endCall() failed: " + e.getMessage());
        }

        // Fallback: ITelephony reflection (works on some MIUI builds)
        try {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) return;

            Method getITelephony = telephonyManager.getClass()
                    .getDeclaredMethod("getITelephony");
            getITelephony.setAccessible(true);
            Object iTelephony = getITelephony.invoke(telephonyManager);

            Method endCall = iTelephony.getClass().getDeclaredMethod("endCall");
            endCall.invoke(iTelephony);
            Log.d(TAG, "Call ended via ITelephony reflection");
        } catch (Exception e) {
            Log.w(TAG, "ITelephony reflection failed: " + e.getMessage());
        }
    }

    // Mirrors WhitelistManager.numbersMatch — last 9 digits comparison
    private boolean numbersMatch(String a, String b) {
        String aD = a.replaceAll("[^\\d]", "");
        String bD = b.replaceAll("[^\\d]", "");
        if (aD.isEmpty() || bD.isEmpty()) return a.equals(b);
        int len = Math.min(9, Math.min(aD.length(), bD.length()));
        if (len < 7) return aD.equals(bD);
        return aD.substring(aD.length() - len).equals(bD.substring(bD.length() - len));
    }
}
