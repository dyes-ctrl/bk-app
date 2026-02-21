@echo off
echo ==========================================
echo  BK - InCallService pour blocage instantane
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout de toutes les modifications...
git add .

echo Commit...
git commit -m "CRITICAL: InCallService pour blocage instantane (Android 9+)"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  TERMINE !
echo ==========================================
echo.
echo Cette version utilise InCallService pour
echo bloquer les appels INSTANTANEMENT sur
echo Android 9 (Huawei P Smart 2019).
echo.
echo IMPORTANT: BK doit etre defini comme
echo application Telephone par defaut.
echo.
pause
