@REM
@REM

@REM
@REM invoke integration test
@REM

cd %~dp0\..

./mvnw.cmd clean verify -B -V
