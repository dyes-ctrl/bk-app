package com.secure.taxapp.utils;

/**
 * Normalisateur de numeros
 */
public class NumberNormalizer {
    
    /**
     * Normalise un numero en format international +33
     * Ex: 0612345678 -> +33612345678
     * Ex: +33 6 12 34 56 78 -> +33612345678
     * Ex: 0033612345678 -> +33612345678
     */
    public static String normalize(String number) {
        if (number == null || number.trim().isEmpty()) {
            return "";
        }
        
        // Supprime tous les caracteres sauf chiffres et +
        String cleaned = number.replaceAll("[^\\d+]", "");
        
        // Si commence par 0033, remplace par +33
        if (cleaned.startsWith("0033")) {
            cleaned = "+33" + cleaned.substring(4);
        }
        
        // Si commence par 0 (format national), remplace par +33
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            cleaned = "+33" + cleaned.substring(1);
        }
        
        return cleaned;
    }
    
    /**
     * Compare deux numeros apres normalisation
     */
    public static boolean match(String number1, String number2) {
        String norm1 = normalize(number1);
        String norm2 = normalize(number2);
        return norm1.equals(norm2) && !norm1.isEmpty();
    }
}
