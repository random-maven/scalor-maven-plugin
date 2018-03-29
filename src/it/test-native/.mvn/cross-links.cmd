
@REM
@REM produce cross version project symlinks (requires SeCreateSymbolicLinkPrivilege)
@REM https://superuser.com/questions/104845/permission-to-make-symbolic-links-in-windows-7
@REM

set "base=%cd%"
set "cross211=%base%\cross\2.11"
set "cross212=%base%\cross\2.12"
set "cross213=%base%\cross\2.13"

echo "base=%base%"
echo "cross211=%cross211%"
echo "cross212=%cross212%"
echo "cross213=%cross213%"

del /s "%cross211%\src"
del /s "%cross212%\src"
del /s "%cross213%\src"

mklink /d "%cross211%\src" "%base%\src"
mklink /d "%cross212%\src" "%base%\src"
mklink /d "%cross213%\src" "%base%\src"
