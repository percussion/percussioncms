@echo off

setlocal

REM Get the disk from the first two chars in the %CD% environment var,
REM and get the directory from the remainder of %CD% after the first 3 chars.
REM See "help set" in a DOS shell window for info
set disk=%CD:~0,2%
set dir=%CD:~3%
set SANDBOX_ROOT=%disk%\%dir%
set path=%SANDBOX_ROOT%\Tools\Ant\bin;%JAVA_SDK_HOME%\bin;%MKS_DIR%\mksnt
set ANT_HOME=%SANDBOX_ROOT%\Tools\Ant
set JAVA_HOME=%JAVA_SDK1.5_HOME%

set classpath=.
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\saxon\saxon.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\mail\mail.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\mail\activation.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\regex\jakarta-oro-2.0.6.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\Ant\lib\ant.jar
set classpath=%classpath%;%JAVA_HOME%\lib\tools.jar

ant -lib %SANDBOX_ROOT%\Tools\Ant\lib -f rxCopyJarsToAppServer.xml -DRXROOT=%1

endlocal

