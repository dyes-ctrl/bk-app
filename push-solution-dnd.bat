@echo off
echo ==========================================
echo  BK - Push solution DND (filtrage reel)
echo ==========================================
echo.
echo Cette mise a jour inclut :
echo  - Solution DND : appels bloques vont DIRECTEMENT en messagerie
echo  - Zero sonnerie pour l'appelant (comme mode avion)
echo  - Contacts favoris = liste blanche synchronisee
echo  - Surveillance DND automatique (anti-EMUI)
echo  - Redemarrage robuste apres boot / kill
echo.

cd /d "%~dp0"

echo Ajout de tous les fichiers modifies...
git add .
echo.

echo Commit...
git commit -m "Refonte complete : solution DND - appels bloques vers messagerie sans sonnerie"
echo.

echo Push vers GitHub (le build APK va demarrer automatiquement)...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Le build GitHub Actions va demarrer.
echo Allez sur votre repo GitHub ^> onglet Actions
echo Attendez 2-3 minutes puis telechargez l'APK
echo dans l'onglet Artifacts du workflow termine.
echo.
pause
