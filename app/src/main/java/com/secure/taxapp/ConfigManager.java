package com.secure.taxapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.secure.taxapp.utils.NumberNormalizer;
import java.util.HashSet;
import java.util.Set;

/**
 * Gestionnaire de configuration.
 * Coordonne la liste blanche, le DND et les contacts favoris.
 */
public class ConfigManager {

    private static final String TAG = "ConfigManager";
    private static final String PREFS_NAME = "TaxCalculationData";
    private static final String KEY_NUMBERS = "allowed_entries";
    private static final String KEY_SERVICE_ACTIVE = "service_state";
    private static final String KEY_MASKED = "display_mode";

    private final SharedPreferences prefs;
    private final DndManager dndManager;
    private boolean masked;

    public ConfigManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.dndManager = new DndManager(appContext);
        this.masked = prefs.getBoolean(KEY_MASKED, true);
    }

    // =========================================================
    // GESTION DES NUMEROS (liste blanche)
    // =========================================================

    /**
     * Ajoute un numero a la liste blanche ET aux contacts favoris Android.
     */
    public boolean addNumber(String number) {
        String normalized = NumberNormalizer.normalize(number);
        if (normalized.isEmpty()) {
            Log.w(TAG, "Invalid number, not added: " + number);
            return false;
        }

        // 1. Sauvegarder dans les preferences
        Set<String> numbers = getNumbersSet();
        if (numbers.contains(normalized)) {
            Log.d(TAG, "Number already in whitelist: " + normalized);
            return false; // Deja present
        }
        numbers.add(normalized);
        saveNumbers(numbers);

        // 2. Synchroniser avec les contacts favoris Android (pour le DND)
        boolean contactAdded = dndManager.addNumberToFavorites(normalized);
        Log.d(TAG, "Number added: " + normalized + " | Contact synced: " + contactAdded);
        return true;
    }

    /**
     * Supprime un numero de la liste blanche ET des contacts favoris Android.
     * Le numero passe en parametre est celui stocke (deja normalise).
     */
    public boolean removeNumber(String normalizedNumber) {
        // 1. Supprimer des preferences
        Set<String> numbers = getNumbersSet();
        numbers.remove(normalizedNumber);
        saveNumbers(numbers);

        // 2. Synchroniser avec les contacts favoris Android
        boolean contactRemoved = dndManager.removeNumberFromFavorites(normalizedNumber);
        Log.d(TAG, "Number removed: " + normalizedNumber + " | Contact synced: " + contactRemoved);
        return true;
    }

    /**
     * Recupere tous les numeros de la liste blanche (normalises)
     */
    public Set<String> getNumbers() {
        return getNumbersSet();
    }

    /**
     * Verifie si un numero est autorise (apres normalisation)
     */
    public boolean isAllowed(String number) {
        String normalized = NumberNormalizer.normalize(number);
        return getNumbersSet().contains(normalized);
    }

    // =========================================================
    // GESTION DU SERVICE (ON/OFF)
    // =========================================================

    /**
     * Active ou desactive le service de blocage.
     * Active/desactive aussi le mode DND automatiquement.
     */
    public void setServiceActive(boolean active) {
        prefs.edit().putBoolean(KEY_SERVICE_ACTIVE, active).apply();

        if (active) {
            // Activer le mode DND avec les contacts favoris
            boolean dndOk = dndManager.enableDnd();
            if (!dndOk) {
                Log.w(TAG, "DND could not be enabled - permission missing?");
            }
            // Re-synchroniser tous les contacts au cas ou
            dndManager.syncAllNumbers(getNumbersSet());
        } else {
            // Desactiver le mode DND -> tous les appels passent normalement
            dndManager.disableDnd();
        }
    }

    /**
     * Retourne true si le service est actif
     */
    public boolean isServiceActive() {
        // Par defaut actif (true)
        return prefs.getBoolean(KEY_SERVICE_ACTIVE, true);
    }

    /**
     * S'assure que le DND est bien dans l'etat attendu.
     * Appele periodiquement par le service de fond pour corriger
     * si Android ou EMUI a desactive le DND sans autorisation.
     */
    public void ensureDndState() {
        if (!isServiceActive()) return;
        if (!dndManager.isDndActive()) {
            Log.w(TAG, "DND was disabled externally - re-enabling...");
            dndManager.enableDnd();
        }
    }

    // =========================================================
    // GESTION DE L'AFFICHAGE (masque/affiche)
    // =========================================================

    public void setMasked(boolean masked) {
        this.masked = masked;
        prefs.edit().putBoolean(KEY_MASKED, masked).apply();
    }

    public boolean isMasked() {
        return masked;
    }

    public String formatForDisplay(String number) {
        if (masked) return "••••••••••••";
        return number;
    }

    // =========================================================
    // ACCES AU DND MANAGER
    // =========================================================

    public DndManager getDndManager() {
        return dndManager;
    }

    // =========================================================
    // METHODES PRIVEES
    // =========================================================

    private Set<String> getNumbersSet() {
        return new HashSet<>(prefs.getStringSet(KEY_NUMBERS, new HashSet<>()));
    }

    private void saveNumbers(Set<String> numbers) {
        prefs.edit().putStringSet(KEY_NUMBERS, numbers).apply();
    }
}
