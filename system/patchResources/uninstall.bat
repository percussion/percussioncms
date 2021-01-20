@echo off
@setlocal
set JAVA_HOME=..\..\JRE
set ANT_HOME=..\InstallToolkit
set CLASSPATH=

IF "%~1"=="--skipVersion" (
ECHO Skipping Version Check...
%ANT_HOME%\bin\ant.bat -buildfile config\deploy.xml uninstall -DSKIP_VERSION=true
) ELSE (
ECHO Including Version Check...
%ANT_HOME%\bin\ant.bat -buildfile config\deploy.xml uninstall -DSKIP_VERSION=false
)
@endlocal