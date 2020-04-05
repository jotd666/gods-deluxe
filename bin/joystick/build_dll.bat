@echo off

set JDK_HOME=C:\Program Files\Java\jdk1.8.0_102
gcc --version
rem 64-bit version
set OPTS=-Wall -shared -I"%JDK_HOME%/include" -I"%JDK_HOME%/include/win32" Joystick.c Joystick.def -o
gcc %OPTS% ../../bin/joystick_x64.dll -lwinmm
rem 32-bit version
C:\GNATPRO\19.0\bin\gcc.exe -Wl,--enable-stdcall-fixup %OPTS% ../../bin/joystick_x86.dll -lwinmm 
pause
