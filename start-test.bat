@echo off
setlocal
cd /d "%~dp0"
call "%~dp0build.bat" run --console plain
if errorlevel 1 (
  echo The test client stopped with an error. See the messages above.
  pause
)
