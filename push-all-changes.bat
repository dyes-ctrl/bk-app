@echo off
echo ==========================================
echo  BK - Envoi de toutes les corrections
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout de tous les fichiers (Code + Manifest + Textes)...
git add .

echo Commit des changements...
git commit -m "Correction critique: Permissions Dialer et RoleManager"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  TERMINE !
echo ==========================================
echo.
echo Le build va demarrer sur GitHub.
pause
