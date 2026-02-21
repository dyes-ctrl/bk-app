package com.secure.taxapp.services;

import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import android.telecom.TelecomManager;
import com.secure.taxapp.ConfigManager;
import java.util.Set;

/**
 * Service InCall - Blocage INSTANTAN garanti sur TOUTES versions Android
 */
public class CallControlService extends InCallService {
    
    private ConfigManager configManager;
    private Set<String> allowedNumbers;
    
    @Override
    public void onCreate() {
        super.onCreate();
        configManager = new ConfigManager(this);
        allowedNumbers = configManager.getNumbers();
    }
    
    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        
        // VERIFIER IMMEDIATEMENT si le service est actif
        if (!configManager.isServiceActive()) {
            return; // Laisser l'appel passer normalement
        }
        
        // Recuperer le numero
        String phoneNumber = getPhoneNumber(call);
        
        // Bloquer les numeros masques/inconnus INSTANTANEMENT
        if (phoneNumber == null || phoneNumber.isEmpty() || 
            phoneNumber.equals("unknown") || phoneNumber.equals("private") ||
            phoneNumber.equals("Anonymous") || phoneNumber.equals("-1")) {
            rejectCallInstantly(call);
            return;
        }
        
        // Normaliser et verifier
        String normalized = normalizeNumber(phoneNumber);
        
        boolean isAllowed = isNumberAllowed(normalized);
        
        if (!isAllowed) {
            rejectCallInstantly(call);
        }
    }
    
    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
    }
    
    @Override
    public void onCallAudioStateChanged(CallAudioState audioState) {
    }
    
    /**
     * Recupere le numero de telephone depuis l'objet Call
     */
    private String getPhoneNumber(Call call) {
        try {
            if (call.getDetails() != null && call.getDetails().getHandle() != null) {
                return call.getDetails().getHandle().getSchemeSpecificPart();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    /**
     * REJET INSTANTANE - Aucune sonnerie pour l'appelant
     */
    private void rejectCallInstantly(Call call) {
        try {
            call.reject(false, null);
        } catch (Exception e) {
            try {
                call.disconnect();
            } catch (Exception e2) {
                // Dernier recours
            }
        }
    }
    
    /**
     * Normalisation du numero
     */
    private String normalizeNumber(String number) {
        if (number == null) return "";
        
        String cleaned = number.replaceAll("[^\\d+]", "");
        
        if (cleaned.startsWith("0033")) {
            return "+33" + cleaned.substring(4);
        }
        
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            return "+33" + cleaned.substring(1);
        }
        
        return cleaned;
    }
    
    /**
     * Verifie si le numero est autorise
     */
    private boolean isNumberAllowed(String normalized) {
        for (String allowed : allowedNumbers) {
            if (normalized.equals(allowed)) {
                return true;
            }
            // Comparaison des 9 derniers chiffres
            if (normalized.length() >= 9 && allowed.length() >= 9) {
                String normEnd = normalized.substring(normalized.length() - 9);
                String allowedEnd = allowed.substring(allowed.length() - 9);
                if (normEnd.equals(allowedEnd)) {
                    return true;
                }
            }
        }
        return false;
    }
}
