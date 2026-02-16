package com.secure.taxapp.services;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import com.secure.taxapp.ConfigManager;
import java.util.Set;
import java.util.HashSet;

/**
 * Service de filtrage d'appels - Optimise pour blocage instantane
 */
public class CallFilterService extends CallScreeningService {
    
    private ConfigManager configManager;
    private Set<String> cachedAllowedNumbers;
    
    @Override
    public void onCreate() {
        super.onCreate();
        configManager = new ConfigManager(this);
        // Cache les numeros en memoire pour acceleration
        cachedAllowedNumbers = new HashSet<>(configManager.getNumbers());
    }
    
    @Override
    public void onScreenCall(Call.Details callDetails) {
        // Verifier le service IMMEDIATEMENT
        if (!configManager.isServiceActive()) {
            respondToCall(callDetails, createAllowResponse());
            return;
        }
        
        String phoneNumber = callDetails.getHandle().getSchemeSpecificPart();
        
        // Bloquer INSTANTANEMENT les numeros prives/inconnus
        if (phoneNumber == null || phoneNumber.isEmpty() || 
            phoneNumber.equals("unknown") || phoneNumber.equals("private") ||
            phoneNumber.equals("Anonymous") || phoneNumber.equals("-1") ||
            phoneNumber.equals("-2")) {
            blockCallInstantly(callDetails);
            return;
        }
        
        // Normalisation rapide inline
        String normalized = normalizeQuick(phoneNumber);
        
        // Verifier dans le cache (plus rapide que configManager)
        boolean isAllowed = false;
        for (String allowedNumber : cachedAllowedNumbers) {
            if (normalized.equals(allowedNumber) || 
                normalized.endsWith(allowedNumber.substring(Math.max(0, allowedNumber.length() - 9))) ||
                allowedNumber.endsWith(normalized.substring(Math.max(0, normalized.length() - 9)))) {
                isAllowed = true;
                break;
            }
        }
        
        if (isAllowed) {
            respondToCall(callDetails, createAllowResponse());
        } else {
            blockCallInstantly(callDetails);
        }
    }
    
    /**
     * Blocage INSTANTANE sans sonnerie
     * Utilise UNIQUEMENT setDisallowCall pour eviter toute sonnerie
     */
    private void blockCallInstantly(Call.Details callDetails) {
        CallResponse response = new CallResponse.Builder()
            .setDisallowCall(true)           // BLOQUE avant etablissement
            .setSkipCallLog(true)             // Ne laisse aucune trace
            .setSkipNotification(true)        // Aucune notification
            .build();
        
        respondToCall(callDetails, response);
    }
    
    private CallResponse createAllowResponse() {
        return new CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .build();
    }
    
    /**
     * Normalisation ultra-rapide inline
     */
    private String normalizeQuick(String number) {
        if (number == null) return "";
        
        // Supprime tout sauf chiffres et +
        String cleaned = number.replaceAll("[^\\d+]", "");
        
        // Conversion rapide 0033 -> +33
        if (cleaned.startsWith("0033")) {
            return "+33" + cleaned.substring(4);
        }
        
        // Conversion rapide 0 -> +33
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            return "+33" + cleaned.substring(1);
        }
        
        return cleaned;
    }
}
