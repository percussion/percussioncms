@echo off
@setlocal
set JAVA_HOME=..\..\JRE64
set ANT_HOME=..\InstallToolkit
set CLASSPATH=

IF "%~1"=="--skipVersion" (
ECHO Skipping Version Check...
%ANT_HOME%\bin\ant.bat -buildfile config\deploy.xml uninstall -DSKIP_VERSION=true
) ELSE (
ECHO Including Version Check...
%ANT_HOME%\bin\ant.bat -buildfile config\deploy.xml uninstall -DSKIP_VERSION=false
)

reg Query "HKLM\Hardware\Description\System\CentralProcessor\0" | find /i "x86" NUL && set OS=32BIT || set OS=64BIT

if %OS%==32BIT (	
)

if %OS%==64BIT (
)

@endlocal