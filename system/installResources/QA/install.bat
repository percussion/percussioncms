@echo off
@setlocal
set JAVA_HOME=%JAVA_SDK1.6_HOME%\jre
set ANT_HOME=.
set CLASSPATH=

%ANT_HOME%\bin\ant.bat
@endlocal