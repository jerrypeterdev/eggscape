@echo off
setlocal
cd /d "%~dp0"
for /d %%J in ("%~dp0.tools\jdk-21*") do set "JAVA_HOME=%%~fJ"
set "GRADLE_USER_HOME=%~dp0.gradle-user"
set "GRADLE_EXE=%~dp0.tools\gradle-8.10\bin\gradle.bat"
if not exist "%JAVA_HOME%\bin\javac.exe" (
  echo Java 21 JDK is missing. Extract it into .tools or set JAVA_HOME.
  exit /b 1
)
if not exist "%GRADLE_EXE%" (
  echo Gradle 8.10 is missing. Extract it into .tools.
  exit /b 1
)
call "%GRADLE_EXE%" %*
exit /b %errorlevel%
