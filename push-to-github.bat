@echo off
echo ==========================================
echo  BK - Push vers GitHub
echo ==========================================
echo.

git --version >nul 2>&1
if errorlevel 1 (
    echo ERREUR : Git n'est pas installe !
    echo Telechargez Git depuis : https://git-scm.com/download/win
    pause
    exit /b 1
)

cd /d "%~dp0"

echo Configuration de Git pour ce dossier...
git config --global --add safe.directory E:/BK
echo.

echo Suppression de l'ancien repo (si existe)...
if exist ".git" rmdir /s /q ".git"
echo.

echo Initialisation du repository...
git init

echo Configuration de l'identite Git...
git config user.email "user@bk-app.local"
git config user.name "BK User"
echo.

git add .
git commit -m "Initial commit BK call blocker"

echo.
echo ==========================================
echo  Connexion a GitHub necessaire
echo ==========================================
echo.
echo Creez d'abord un repo sur GitHub :
echo 1. Allez sur https://github.com/new
echo 2. Nom du repo : bk-app
echo 3. Ne cochez PAS "Initialize with README"
echo 4. Cliquez sur "Create repository"
echo.
echo Copiez l'URL du repo (HTTPS) :
echo Exemple : https://github.com/votre-pseudo/bk-app.git
echo.
set /p repo_url="URL du repo : "

git remote add origin %repo_url%
git branch -M main
git push -u origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Votre code est maintenant sur GitHub
echo Allez sur votre repo pour voir le workflow Actions
pause
