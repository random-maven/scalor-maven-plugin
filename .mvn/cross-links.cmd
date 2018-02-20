
@REM
@REM produce cross version project symlinks
@REM

set "base=%cd%"

set "cross212=%base%/cross/2.12"
set "cross213=%base%/cross/2.13"

echo "base=%base%"
echo "cross212=%cross212%"
echo "cross213=%cross213%"

@REM sources
rm -f "%cross212%/src"
rm -f "%cross213%/src"
mklink /d "%cross212%/src" "%base%/src" 
mklink /d "%cross213%/src" "%base%/src" 

@REM repository
rm -f "%cross212%/test-repo"
rm -f "%cross213%/test-repo"
mklink /d "%cross212%/test-repo" "%base%/test-repo" 
mklink /d "%cross213%/test-repo" "%base%/test-repo" 
