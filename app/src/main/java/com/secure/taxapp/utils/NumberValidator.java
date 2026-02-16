package com.secure.taxapp.utils;

import java.util.regex.Pattern;

/**
 * Validateur de formats
 */
public class NumberValidator {
    
    // Pattern pour numero francais valide
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+33|0033|0)[1-9]([-. ]?[0-9]{2}){4}$"
    );
    
    public static boolean isValid(String number) {
        if (number == null || number.trim().isEmpty()) {
            return false;
        }
        String cleaned = number.trim();
        return PHONE_PATTERN.matcher(cleaned).matches();
    }
    
    public static String cleanNumber(String number) {
        if (number == null) return "";
        return number.replaceAll("[^\\d+]", "");
    }
}
