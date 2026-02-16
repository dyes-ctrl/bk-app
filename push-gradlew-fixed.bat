@echo off
echo ==========================================
echo  BK - Push du gradlew corrige
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout du gradlew corrige...
git add gradlew

echo Commit...
git commit -m "Correction: gradlew simplifie et fonctionnel"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Le fichier gradlew a ete corrige et push.
echo Allez sur GitHub Actions pour voir le build.
pause
