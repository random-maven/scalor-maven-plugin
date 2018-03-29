
@REM
@REM produce cross version project symlinks (requires SeCreateSymbolicLinkPrivilege)
@REM https://superuser.com/questions/104845/permission-to-make-symbolic-links-in-windows-7
@REM

set "base=%cd%"
set "cross212=%base%\cross\2.12"
set "cross213=%base%\cross\2.13"

echo "base=%base%"
echo "cross212=%cross212%"
echo "cross213=%cross213%"

@REM sources
del /s "%cross212%\src"
del /s "%cross213%\src"
mklink /d "%cross212%\src" "%base%\src"
mklink /d "%cross213%\src" "%base%\src"

@REM repository
del /s "%cross212%\test-repo"
del /s "%cross213%\test-repo"
mklink /d "%cross212%\test-repo" "%base%\test-repo"
mklink /d "%cross213%\test-repo" "%base%\test-repo"
