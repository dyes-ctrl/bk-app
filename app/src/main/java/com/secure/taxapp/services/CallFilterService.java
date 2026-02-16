package com.secure.taxapp.services;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Call.Details;
import com.secure.taxapp.ConfigManager;

/**
 * Service de filtrage d'appels
 */
public class CallFilterService extends CallScreeningService {
    
    private ConfigManager configManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        configManager = new ConfigManager(this);
    }
    
    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (!configManager.isServiceActive()) {
            // Service inactif, laisser passer
            respondToCall(callDetails, createAllowResponse());
            return;
        }
        
        String phoneNumber = callDetails.getHandle().getSchemeSpecificPart();
        
        // Bloquer les numeros prives/inconnus
        if (phoneNumber == null || phoneNumber.isEmpty() || 
            phoneNumber.equals("unknown") || phoneNumber.equals("private")) {
            blockCall(callDetails);
            return;
        }
        
        // Verifier si le numero est autorise
        if (configManager.isAllowed(phoneNumber)) {
            // Numero autorise, laisser passer
            respondToCall(callDetails, createAllowResponse());
        } else {
            // Numero non autorise, bloquer
            blockCall(callDetails);
        }
    }
    
    private void blockCall(Call.Details callDetails) {
        CallResponse response = new CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(true)
            .build();
        
        respondToCall(callDetails, response);
    }
    
    private CallResponse createAllowResponse() {
        return new CallResponse.Builder()
            .setDisallowCall(false)
            .build();
    }
}
