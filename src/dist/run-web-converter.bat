@echo off
setlocal

title MSBF-to-TachiBK

echo.
echo MSBF-to-TachiBK
echo =================
echo Starting local web converter...
echo.
echo After the converter starts, open:
echo http://localhost:8080
echo.
echo Leave this window open while converting.
echo Press Ctrl+C to stop the converter.
echo.

cd /d "%~dp0"

if exist "bin\MSBF-to-TachiBK.bat" (
    call "bin\MSBF-to-TachiBK.bat" serve
) else (
    echo Could not find bin\MSBF-to-TachiBK.bat
    echo Make sure you unzipped the full release package before running this file.
    pause
)