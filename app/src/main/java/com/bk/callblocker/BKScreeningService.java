package com.bk.callblocker;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public class BKScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {
        WhitelistManager whitelist = new WhitelistManager(this);
        CallResponse.Builder response = new CallResponse.Builder();

        // Filtering disabled -> let everything through
        if (!whitelist.isEnabled()) {
            respondToCall(callDetails, response.setDisallowCall(false).build());
            return;
        }

        String number = "";
        if (callDetails.getHandle() != null) {
            number = callDetails.getHandle().getSchemeSpecificPart();
        }

        boolean isPrivate = number.isEmpty()
                || number.equalsIgnoreCase("anonymous")
                || number.equalsIgnoreCase("unknown")
                || number.equalsIgnoreCase("blocked");

        if (isPrivate) {
            if (whitelist.isPrivateNumberBlocked()) {
                blockCall(callDetails, response);
            } else {
                respondToCall(callDetails, response.setDisallowCall(false).build());
            }
            return;
        }

        if (whitelist.isAllowed(number)) {
            // Whitelisted -> allow, ring normally
            respondToCall(callDetails, response.setDisallowCall(false).build());
        } else {
            // Not whitelisted -> reject immediately, 0 ring for caller
            blockCall(callDetails, response);
        }
    }

    private void blockCall(Call.Details callDetails, CallResponse.Builder response) {
        // setRejectCall(true) = caller goes to voicemail immediately with no ringing
        response.setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)      // keep in call log as missed/blocked
                .setSkipNotification(false); // show missed call notification
        respondToCall(callDetails, response.build());
    }
}
