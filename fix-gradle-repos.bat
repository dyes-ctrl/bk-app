@echo off
echo ==========================================
echo  BK - Correction Gradle 8.2
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout de build.gradle corrige...
git add build.gradle

echo Commit...
git commit -m "Correction: Suppression des repositories de build.gradle (Gradle 8.2+)"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Les repositories sont maintenant uniquement dans settings.gradle
echo comme exige par Gradle 8.2+
echo Allez sur GitHub Actions pour voir le build.
pause
