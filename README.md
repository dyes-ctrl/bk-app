# BK - Application de Blocage d'Appels

Application Android qui bloque tous les appels entrants sauf les numéros explicitement autorisés dans une liste blanche.

## Fonctionnalités

- ✅ **Blocage immédiat** : Les appels non autorisés sont rejetés sans sonnerie
- ✅ **Liste blanche** : Interface simple pour ajouter/supprimer des numéros autorisés
- ✅ **Masquage** : Les numéros peuvent être masqués pour la confidentialité (••••••••••••)
- ✅ **Persistant** : Fonctionne en arrière-plan, redémarre automatiquement après reboot
- ✅ **Confidentialité** : Obfuscation du code, noms de fichiers trompeurs
- ✅ **Normalisation** : Accepte tous les formats (06, +33, 0033, espaces, tirets)
- ✅ **Numéros privés** : Bloqués systématiquement
- ✅ **Par défaut ON** : S'active automatiquement au premier lancement

## Installation via GitHub Actions

### Étape 1 : Créer le repo GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/TON_USER/bk-app.git
git push -u origin main
```

### Étape 2 : Attendre le build

1. Allez sur votre repo GitHub : `https://github.com/TON_USER/bk-app`
2. Cliquez sur l'onglet **Actions**
3. Attendez que le workflow "Build APK" termine (2-3 minutes)

### Étape 3 : Télécharger l'APK

1. Cliquez sur le workflow terminé (vert ✓)
2. En bas de la page, trouvez la section **Artifacts**
3. Cliquez sur **bk-app-debug** pour télécharger le ZIP
4. Extrayez l'APK du ZIP

### Étape 4 : Installer sur Android

1. Transférez l'APK sur votre téléphone (USB, email, cloud)
2. Sur le téléphone, allez dans **Paramètres > Sécurité**
3. Activez **Sources inconnues** ou **Installer des apps inconnues**
4. Ouvrez l'APK et installez

## Utilisation

### Premier lancement

1. Ouvrez l'application **BK**
2. Accordez les permissions demandées :
   - Téléphone et appels
   - Ignorer les optimisations batterie
3. Définissez **BK** comme application d'appel par défaut (obligatoire)
4. Le service démarre automatiquement (interrupteur ON par défaut)

### Gérer les numéros autorisés

**Ajouter un numéro :**
1. Saisissez le numéro dans le champ en bas
2. Cliquez sur **Ajouter**
3. Le numéro est ajouté à la liste blanche

**Formats acceptés :**
- `06 12 34 56 78` (avec espaces)
- `0612345678` (sans espaces)
- `+33 6 12 34 56 78` (format international avec espaces)
- `+33612345678` (format international sans espaces)
- `0033 6 12 34 56 78` (avec indicatif 00)
- `0033612345678` (indicatif 00 sans espaces)

**Tous ces formats sont reconnus comme le même numéro automatiquement.**

**Masquer/Afficher les numéros :**
- Cliquez sur **Masquer** pour cacher tous les numéros (••••••••••••)
- Cliquez sur **Afficher** pour les voir en clair

**Supprimer un numéro :**
- Cliquez sur l'icône corbeille (🗑️) à côté du numéro à supprimer

**Désactiver le service :**
- Décochez l'interrupteur principal en haut
- Le service s'arrête et tous les appels passent normalement

## Comportement des appels

| Type d'appel | Résultat |
|--------------|----------|
| Numéro dans la liste blanche | ✅ Appel normal, sonnerie |
| Numéro inconnu/non autorisé | ❌ Rejet immédiat, messagerie |
| Numéro privé/masqué | ❌ Bloqué systématiquement |
| Numéro d'urgence (15, 17, 18, 112) | ❌ Bloqué si pas dans la liste |

**Important** : L'application est très restrictive. Seuls les numéros explicitement ajoutés peuvent appeler.

## Persistance

- **Démarrage automatique** : Le service redémarre après chaque reboot du téléphone
- **Anti-kill** : Si Android tue le service, il se redémarre automatiquement
- **Mémorisation** : L'état ON/OFF est sauvegardé (activé par défaut)

## Sécurité et obfuscation

- ✅ **ProGuard** : Code obfusqué en release
- ✅ **Noms trompeurs** : `TaxCalculator`, `CalculationService` au lieu de `CallBlocker`
- ✅ **Splitting** : Logique répartie dans plusieurs fichiers
- ✅ **Anti-debugging** : Détection de debug intégrée
- ✅ **Stockage chiffré** : Les préférences sont protégées

## Permissions requises

- **Téléphone** : Détecter les appels entrants
- **Appels** : Filtrer les appels
- **Démarrage** : Lancer au boot
- **Batterie** : Ignorer les optimisations pour rester actif

## Notes importantes

⚠️ **Application par défaut** : BK doit rester définie comme application d'appel par défaut pour fonctionner. Si vous changez cette setting, le blocage ne marchera plus.

⚠️ **Désinstallation** : Si vous désinstallez l'app, les numéros sauvegardés seront perdus. Notez-les avant si besoin.

⚠️ **Pas de SMS** : Cette version ne filtre que les appels, pas les SMS.

## Compilation locale (optionnel)

Si vous avez Android Studio :

```bash
# Avec Gradle wrapper
./gradlew assembleDebug  # Linux/Mac
gradlew.bat assembleDebug  # Windows
```

L'APK sera dans : `app/build/outputs/apk/debug/app-debug.apk`

## Structure du projet

```
E:\BK\
├── .github/workflows/build.yml    # CI/CD GitHub Actions
├── app/
│   ├── src/main/java/com/secure/taxapp/     # Code source (noms trompeurs)
│   │   ├── MainActivity.java               # Interface utilisateur
│   │   ├── ConfigManager.java              # Gestion des numéros
│   │   ├── NumberAdapter.java              # Liste des numéros
│   │   ├── services/
│   │   │   ├── CallFilterService.java      # Filtre d'appels
│   │   │   ├── CalculationService.java     # Service persistant
│   │   │   ├── BootReceiver.java           # Démarrage auto
│   │   │   └── ServiceRestartReceiver.java # Redémarrage
│   │   └── utils/
│   │       ├── NumberNormalizer.java       # Normalisation formats
│   │       ├── NumberValidator.java        # Validation
│   │       └── SecurityGuard.java          # Anti-debugging
│   └── build.gradle
├── build.gradle
└── README.md
```

## Support

En cas de problème :
1. Vérifiez que BK est bien l'application d'appel par défaut
2. Vérifiez que toutes les permissions sont accordées
3. Redémarrez le téléphone
4. Réinstallez l'APK si nécessaire

## Licence

Usage privé uniquement.
