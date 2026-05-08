package com.bk.callblocker;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public class BKScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {
        CallResponse.Builder response = new CallResponse.Builder();

        // Use in-memory cache — zero I/O on the critical call path
        if (!WhitelistCache.isEnabled()) {
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
            if (WhitelistCache.isPrivateBlocked()) {
                blockCall(callDetails, response);
            } else {
                respondToCall(callDetails, response.setDisallowCall(false).build());
            }
            return;
        }

        String normalized = WhitelistManager.normalize(number);
        if (normalized == null) normalized = number.replaceAll("[\\s.\\-()]", "");

        boolean allowed = false;
        for (String stored : WhitelistCache.getNumbers()) {
            if (numbersMatch(stored, normalized)) {
                allowed = true;
                break;
            }
        }

        if (allowed) {
            respondToCall(callDetails, response.setDisallowCall(false).build());
        } else {
            blockCall(callDetails, response);
        }
    }

    private void blockCall(Call.Details callDetails, CallResponse.Builder response) {
        response.setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false);
        respondToCall(callDetails, response.build());
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
