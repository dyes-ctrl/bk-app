@echo off
echo ==========================================
echo  BK - Mise a jour du workflow
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout des modifications...
git add .github/workflows/build.yml

echo Commit des changements...
git commit -m "Correction: Mise a jour des actions GitHub (v3 -> v4)"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Le workflow a ete corrige.
echo Allez sur GitHub Actions pour voir le nouveau build.
pause
