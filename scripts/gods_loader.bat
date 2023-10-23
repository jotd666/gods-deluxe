rem @echo off

rem
rem  Gods run script
rem
rem  written by JOTD
rem

set GODS_ROOT_DIR=%~dp0\..

java -Djava.library.path="%GODS_ROOT_DIR%\bin" -DGODS_ASSETS_DIR="%GODS_ROOT_DIR%" -jar "%GODS_ROOT_DIR%\bin\gods-deluxe.jar" %1 %2 %3 %4 %5 %6 %7 %8 %9

