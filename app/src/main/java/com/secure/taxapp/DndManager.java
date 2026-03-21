package com.secure.taxapp;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.app.NotificationManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Gestionnaire du Mode Ne Pas Deranger (DND) et des contacts favoris.
 *
 * PRINCIPE DE FONCTIONNEMENT :
 * - En mode ACTIF : DND "Prioritaire" est active en permanence.
 *   Seuls les contacts marques comme Favoris (starred) peuvent appeler.
 *   Tous les autres appels -> messagerie directement, AUCUNE sonnerie.
 *
 * - En mode INACTIF : DND desactive, tous les appels passent normalement.
 *
 * La liste blanche de l'app est synchronisee avec les contacts favoris
 * Android pour que le systeme les laisse passer.
 */
public class DndManager {

    private static final String TAG = "DndManager";
    
    // Tag special pour identifier les contacts crees par BK
    // On les stocke dans le champ "Note" du contact
    private static final String BK_CONTACT_TAG = "BK_WHITELIST_MANAGED";

    private final Context context;
    private final NotificationManager notificationManager;

    public DndManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager)
            this.context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    // =========================================================
    // GESTION DU MODE DND
    // =========================================================

    /**
     * Verifie si l'app a l'autorisation de modifier le mode DND
     */
    public boolean hasDndPermission() {
        return notificationManager != null
            && notificationManager.isNotificationPolicyAccessGranted();
    }

    /**
     * Active le mode Ne Pas Deranger en mode PRIORITAIRE.
     * Seuls les contacts favoris (starred) peuvent appeler.
     * Tous les autres appels -> messagerie, zero sonnerie.
     */
    public boolean enableDnd() {
        if (!hasDndPermission()) {
            Log.w(TAG, "DND permission not granted");
            return false;
        }
        try {
            // Configurer la politique DND :
            // - Autoriser les appels des contacts favoris UNIQUEMENT
            // - Autoriser aussi les rappelants (si rappel dans les 15min)
            NotificationManager.Policy policy = new NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_CALLS
                | NotificationManager.Policy.PRIORITY_CATEGORY_REPEAT_CALLERS,
                NotificationManager.Policy.PRIORITY_SENDERS_STARRED,  // Appels entrants : favoris seulement
                NotificationManager.Policy.PRIORITY_SENDERS_STARRED   // Messages entrants : favoris seulement
            );
            notificationManager.setNotificationPolicy(policy);

            // Activer le mode DND en PRIORITAIRE (pas silencieux total,
            // car les favoris doivent pouvoir sonner)
            notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            );
            
            Log.d(TAG, "DND enabled in PRIORITY mode (starred contacts only)");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable DND", e);
            return false;
        }
    }

    /**
     * Desactive le mode Ne Pas Deranger.
     * Tous les appels passent normalement.
     */
    public boolean disableDnd() {
        if (!hasDndPermission()) {
            Log.w(TAG, "DND permission not granted");
            return false;
        }
        try {
            notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_ALL
            );
            Log.d(TAG, "DND disabled - all calls allowed");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable DND", e);
            return false;
        }
    }

    /**
     * Verifie si le DND est actuellement actif en mode prioritaire
     */
    public boolean isDndActive() {
        if (!hasDndPermission()) return false;
        int filter = notificationManager.getCurrentInterruptionFilter();
        return filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY;
    }

    // =========================================================
    // GESTION DES CONTACTS FAVORIS
    // =========================================================

    /**
     * Ajoute un numero dans les contacts favoris Android.
     * Les contacts favoris sont autorises a passer en mode DND.
     *
     * Si le numero existe deja dans les contacts -> on le met juste en favori.
     * Sinon -> on cree un nouveau contact BK et on le met en favori.
     */
    public boolean addNumberToFavorites(String normalizedNumber) {
        try {
            // Verifier si un contact avec ce numero existe deja
            long existingContactId = findContactByPhone(normalizedNumber);

            if (existingContactId != -1) {
                // Contact existant : on le met juste en favori
                starContact(existingContactId);
                Log.d(TAG, "Existing contact starred: " + existingContactId);
            } else {
                // Creer un nouveau contact gere par BK et le mettre en favori
                createBkContact(normalizedNumber);
                Log.d(TAG, "BK contact created for: " + normalizedNumber);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to add number to favorites: " + normalizedNumber, e);
            return false;
        }
    }

    /**
     * Supprime un numero des contacts favoris Android.
     *
     * Si c'est un contact cree par BK -> on le supprime.
     * Si c'est un contact existant de l'utilisateur -> on enleve juste le favori.
     */
    public boolean removeNumberFromFavorites(String normalizedNumber) {
        try {
            // D'abord, chercher si c'est un contact BK
            long bkRawContactId = findBkRawContactByPhone(normalizedNumber);

            if (bkRawContactId != -1) {
                // Supprimer completement le contact BK
                deleteBkContact(bkRawContactId);
                Log.d(TAG, "BK contact deleted for: " + normalizedNumber);
            } else {
                // Contact preexistant : enlever juste le favori
                long contactId = findContactByPhone(normalizedNumber);
                if (contactId != -1) {
                    unstarContact(contactId);
                    Log.d(TAG, "Contact unstarred: " + contactId);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to remove number from favorites", e);
            return false;
        }
    }

    /**
     * Synchronise TOUS les numeros de la liste blanche avec les contacts favoris.
     * A appeler au demarrage ou apres un changement important.
     */
    public void syncAllNumbers(Set<String> normalizedNumbers) {
        if (normalizedNumbers == null) return;
        
        // Recuperer les contacts BK actuels
        Set<String> currentBkNumbers = getBkManagedNumbers();
        
        // Ajouter les nouveaux
        for (String number : normalizedNumbers) {
            if (!currentBkNumbers.contains(number)) {
                addNumberToFavorites(number);
            }
        }
        
        // Supprimer ceux qui ne sont plus dans la liste
        for (String number : currentBkNumbers) {
            if (!normalizedNumbers.contains(number)) {
                removeNumberFromFavorites(number);
            }
        }
        
        Log.d(TAG, "Sync complete: " + normalizedNumbers.size() + " numbers in whitelist");
    }

    // =========================================================
    // METHODES PRIVEES - CONTACTS
    // =========================================================

    /**
     * Cherche un contact par son numero de telephone.
     * Retourne le contact ID ou -1 si non trouve.
     */
    private long findContactByPhone(String phoneNumber) {
        Uri uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        );

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                uri,
                new String[]{ContactsContract.PhoneLookup._ID},
                null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding contact by phone", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return -1;
    }

    /**
     * Cherche un contact RAW cree par BK pour ce numero.
     * Retourne le raw_contact_id ou -1 si non trouve.
     */
    private long findBkRawContactByPhone(String phoneNumber) {
        // Chercher dans les donnees de contacts le tag BK et le numero
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.RAW_CONTACT_ID},
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.CommonDataKinds.Note.NOTE + " = ?",
                new String[]{
                    ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
                    BK_CONTACT_TAG + "_" + phoneNumber
                },
                null
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding BK contact", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return -1;
    }

    /**
     * Recupere tous les numeros geres par BK
     */
    private Set<String> getBkManagedNumbers() {
        Set<String> numbers = new HashSet<>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Note.NOTE},
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.CommonDataKinds.Note.NOTE + " LIKE ?",
                new String[]{
                    ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
                    BK_CONTACT_TAG + "_%"
                },
                null
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String note = cursor.getString(0);
                    if (note != null && note.startsWith(BK_CONTACT_TAG + "_")) {
                        numbers.add(note.substring((BK_CONTACT_TAG + "_").length()));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting BK numbers", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return numbers;
    }

    /**
     * Marque un contact comme favori (starred)
     */
    private void starContact(long contactId) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Contacts.STARRED, 1);
        context.getContentResolver().update(
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId),
            values, null, null
        );
    }

    /**
     * Retire le favori d'un contact
     */
    private void unstarContact(long contactId) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Contacts.STARRED, 0);
        context.getContentResolver().update(
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId),
            values, null, null
        );
    }

    /**
     * Cree un nouveau contact gere par BK, marque comme favori
     */
    private void createBkContact(String normalizedNumber) throws Exception {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // Operation 0 : Creer le raw contact (en local, pas de compte cloud)
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .withValue(ContactsContract.RawContacts.STARRED, 1)
            .build());

        // Operation 1 : Ajouter le numero de telephone
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.Phone.NUMBER, normalizedNumber)
            .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
            .build());

        // Operation 2 : Ajouter un nom affichable
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, "[BK] " + normalizedNumber)
            .build());

        // Operation 3 : Ajouter le tag BK pour identifier ce contact comme gere par nous
        // Le tag contient aussi le numero pour retrouver le contact facilement
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                CommonDataKinds.Note.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.Note.NOTE, BK_CONTACT_TAG + "_" + normalizedNumber)
            .build());

        context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
    }

    /**
     * Supprime un contact BK par son raw_contact_id
     */
    private void deleteBkContact(long rawContactId) {
        Uri rawContactUri = ContentUris.withAppendedId(
            ContactsContract.RawContacts.CONTENT_URI, rawContactId
        );
        context.getContentResolver().delete(rawContactUri, null, null);
    }
}
