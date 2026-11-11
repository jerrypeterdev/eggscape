@echo off
setlocal
cd /d "%~dp0"
call "%~dp0build.bat" runRelease --console plain
if errorlevel 1 (
  echo The release preview stopped with an error. See the messages above.
  pause
)
