rem @echo off

rem
rem  Gods run script
rem
rem  written by JOTD
rem

set OLDCD=%CD%

set GODS_ROOT_DIR=%CD%

rem increase heap size if necessary

set HEAP_SIZE=200m

rem echo %GODS_ROOT_DIR%

set cmd=start javaw

if !%DEBUG%==! goto normal

set cmd=java

:normal

%cmd% -Dsun.java2d.d3d=false -Djava.library.path="%GODS_ROOT_DIR%\bin" -classpath "%GODS_ROOT_DIR%\bin;%GODS_ROOT_DIR%\lib\exp4j-0.2.9.jar" -DROOT_DIR="%GODS_ROOT_DIR%" -Xmx%HEAP_SIZE% %1 %2 %3 %4

