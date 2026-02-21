@echo off
echo ==========================================
echo  BK - Correction CRITIQUE blocage instantane
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout des modifications critiques...
git add app/src/main/java/com/secure/taxapp/services/CallFilterService.java

echo Commit...
git commit -m "CORRECTION CRITIQUE: Blocage instantane sans sonnerie (setDisallowCall only)"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  TERMINE !
echo ==========================================
echo.
echo Cette version bloque les appels AVANT qu'ils sonnent.
echo Installez le nouvel APK des qu'il est pret.
pause
