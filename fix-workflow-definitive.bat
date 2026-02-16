@echo off
echo ==========================================
echo  BK - Solution definitive workflow
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout du workflow corrige...
git add .github/workflows/build.yml

echo Commit...
git commit -m "Solution definitive: Utilisation de Gradle directement au lieu de gradlew"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Le workflow a ete modifie pour utiliser Gradle directement.
echo Plus besoin de gradlew ou gradle-wrapper.jar
echo Allez sur GitHub Actions pour voir le build.
pause
