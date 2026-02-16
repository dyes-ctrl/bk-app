@echo off
echo ==========================================
echo  BK - Push du fichier gradlew manquant
echo ==========================================
echo.

cd /d "%~dp0"

echo Verification du fichier gradlew...
if not exist "gradlew" (
    echo ERREUR CRITIQUE: gradlew n'existe pas!
    echo Le fichier n'a pas ete cree correctement.
    pause
    exit /b 1
)

echo Fichier gradlew trouve!
echo.
echo Ajout au repository Git...
git add gradlew

echo Commit des changements...
git commit -m "Ajout du fichier gradlew manquant"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Le fichier gradlew a ete push sur GitHub.
echo Allez sur GitHub Actions pour voir le build.
pause
