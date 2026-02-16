package com.secure.taxapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/**
 * Gestionnaire de configuration
 */
public class ConfigManager {
    
    private static final String PREFS_NAME = "TaxCalculationData";
    private static final String KEY_NUMBERS = "allowed_entries";
    private static final String KEY_SERVICE_ACTIVE = "service_state";
    private static final String KEY_MASKED = "display_mode";
    
    private SharedPreferences prefs;
    private boolean masked = true;
    
    public ConfigManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.masked = prefs.getBoolean(KEY_MASKED, true);
    }
    
    /**
     * Ajoute un numero a la liste
     */
    public void addNumber(String number) {
        Set<String> numbers = getNumbersSet();
        String normalized = com.secure.taxapp.utils.NumberNormalizer.normalize(number);
        if (!normalized.isEmpty()) {
            numbers.add(normalized);
            saveNumbers(numbers);
        }
    }
    
    /**
     * Supprime un numero de la liste
     */
    public void removeNumber(String number) {
        Set<String> numbers = getNumbersSet();
        numbers.remove(number);
        saveNumbers(numbers);
    }
    
    /**
     * Recupere tous les numeros
     */
    public Set<String> getNumbers() {
        return getNumbersSet();
    }
    
    /**
     * Verifie si un numero est autorise
     */
    public boolean isAllowed(String number) {
        String normalized = com.secure.taxapp.utils.NumberNormalizer.normalize(number);
        return getNumbersSet().contains(normalized);
    }
    
    /**
     * Active/desactive le masquage
     */
    public void setMasked(boolean masked) {
        this.masked = masked;
        prefs.edit().putBoolean(KEY_MASKED, masked).apply();
    }
    
    public boolean isMasked() {
        return masked;
    }
    
    /**
     * Formate un numero pour affichage (masque ou non)
     */
    public String formatForDisplay(String number) {
        if (masked) {
            return "••••••••••••";
        }
        return number;
    }
    
    /**
     * Active/desactive le service
     */
    public void setServiceActive(boolean active) {
        prefs.edit().putBoolean(KEY_SERVICE_ACTIVE, active).apply();
    }
    
    public boolean isServiceActive() {
        // Par defaut active (true)
        return prefs.getBoolean(KEY_SERVICE_ACTIVE, true);
    }
    
    private Set<String> getNumbersSet() {
        return new HashSet<>(prefs.getStringSet(KEY_NUMBERS, new HashSet<String>()));
    }
    
    private void saveNumbers(Set<String> numbers) {
        prefs.edit().putStringSet(KEY_NUMBERS, numbers).apply();
    }
}
