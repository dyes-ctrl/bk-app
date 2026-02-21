@echo off
echo ==========================================
echo  BK - Correction erreur compilation
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout de la correction...
git add .

echo Commit...
git commit -m "Fix: Correction signature methode reject()"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  TERMINE !
echo ==========================================
pause
