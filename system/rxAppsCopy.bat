@echo off

set OLDPATH=%path%
set OLDCLASSPATH=%classpath%

REM Get the disk from the first two chars in the %CD% environment var,
REM and get the directory from the remainder of %CD% after the first 3 chars.
REM See "help set" in a DOS shell window for info
set disk=%CD:~0,2%
set dir=%CD:~3%
set SANDBOX_ROOT=%disk%\%dir%
set path=%SANDBOX_ROOT%\Tools\Ant\bin;%JAVA_SDK_HOME%\bin;%MKS_DIR%\mksnt
set TMPBATCH=_tmp999.bat
set ANT_HOME=%SANDBOX_ROOT%\Tools\Ant
set JAVA_HOME=%JAVA_SDK_HOME%

set classpath=.
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\saxon\saxon.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\mail\mail.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\mail\activation.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\regex\jakarta-oro-2.0.6.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\Ant\lib\ant.jar
set classpath=%classpath%;%JAVA_SDK1.4_HOME%\lib\tools.jar

echo %1

if "%1" == "WITH_FF" goto withFF
if "%1" == "FF_ONLY" goto ffOnly


ant -lib %SANDBOX_ROOT%\Tools\Ant\lib -f rxAppsCopy.xml
goto End

:withFF
ant -lib %SANDBOX_ROOT%\Tools\Ant\lib -f rxAppsCopy.xml -DWITHFF=true
goto End

:ffOnly
ant -lib %SANDBOX_ROOT%\Tools\Ant\lib -f rxAppsCopy.xml -DFFONLY=true
goto End

:End
set path=%OLDPATH%
set classpath=%OLDCLASSPATH%
set OLDPATH=
set OLDCLASSPATH=
set SANDBOX_ROOT=
set dir=
set disk=
set ARGS=

