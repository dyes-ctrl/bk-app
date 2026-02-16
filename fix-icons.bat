@echo off
echo ==========================================
echo  BK - Correction des icones manquantes
echo ==========================================
echo.

cd /d "%~dp0"

echo Ajout des modifications...
git add app/src/main/AndroidManifest.xml

echo Commit des changements...
git commit -m "Correction: Utilisation des icones systeme pour eviter l'erreur mipmap"

echo Push vers GitHub...
git push origin main

echo.
echo ==========================================
echo  FAIT !
echo ==========================================
echo.
echo Le manifest a ete mis a jour avec des icones systeme.
echo Allez sur GitHub Actions pour voir le nouveau build.
pause
