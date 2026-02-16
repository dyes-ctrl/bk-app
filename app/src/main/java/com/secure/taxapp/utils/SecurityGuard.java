package com.secure.taxapp.utils;

import android.content.Context;
import android.os.Debug;

/**
 * Utilitaires de securite
 */
public class SecurityGuard {
    
    public static boolean isDebugMode() {
        return Debug.isDebuggerConnected() || android.os.Debug.waitingForDebugger();
    }
    
    public static void checkIntegrity(Context context) {
        if (isDebugMode()) {
            throw new RuntimeException("Security violation detected");
        }
    }
    
    public static void scrambleData(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] ^ 0x5A);
        }
    }
}
