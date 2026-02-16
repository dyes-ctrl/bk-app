@echo off
echo ==========================================
echo  BK - Correction des fins de ligne
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout des modifications...
git add .github/workflows/build.yml

echo Commit des changements...
git commit -m "Correction: Conversion des fins de ligne LF pour gradlew"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Le workflow a ete corrige pour gerer les fins de ligne.
echo Allez sur GitHub Actions pour voir le nouveau build.
pause
