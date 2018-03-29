@REM
@REM

@REM
@REM invoke integration test
@REM

cd %~dp0\..

@REM .\mvnw.cmd clean verify -B -D invoker.test=test-cross

@REM .\mvnw.cmd clean verify -B -D invoker.test=test-native

.\mvnw.cmd clean verify -B -D invoker.test=test-setup
