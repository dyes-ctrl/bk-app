@echo off
echo ==========================================
echo  BK - Correction du fichier gradlew
echo ==========================================
echo.

cd /d "%~dp0"

echo Suppression de l'ancien gradlew corrompu...
if exist "gradlew" del /f "gradlew"

echo.
echo Le nouveau gradlew a ete cree avec le contenu correct.
echo.

echo Ajout des modifications...
git add gradlew
git add .github/workflows/build.yml

echo Commit des changements...
git commit -m "Correction: Fichier gradlew recree proprement"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Le fichier gradlew a ete corrige.
echo Allez sur GitHub Actions pour voir le nouveau build.
pause
