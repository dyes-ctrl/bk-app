@echo off
cd /d "%~dp0"
echo  BK Call Blocker - Push vers GitHub
echo =====================================

git add .
git commit -m "Refonte v3 : CallScreeningService - blocage reel sans sonnerie (Redmi 9A)"
git push origin main

echo.
echo  Push effectue ! GitHub Actions compile l'APK automatiquement...
echo  Dans 2-3 minutes, allez sur :
echo  https://github.com/dyes-ctrl/bk-app/actions
echo  Cliquez sur le dernier workflow puis sur "Artifacts" pour telecharger l'APK.
echo.
pause
