@echo off 
setlocal
SET mypath=%~dp0

SET JETTY_HOME=%mypath%upstream
SET rxDir=%mypath%..
SET JETTY_BASE=%mypath%base
SET JETTY_DEFAULTS=%mypath%defaults
SET JAVA_HOME=%rxDir%\JRE
SET PATH=%JAVA_HOME%\bin;%PATH%
set STOPPORT=50011

cd %JETTY_BASE%
%JAVA_HOME%\bin\java.exe -jar %JETTY_HOME%\start.jar -DSTOP.PORT=%STOPPORT% -DSTOP.KEY="SHUTDOWN" -Drxdeploydir="%rxDir%" -Djetty.base="%JETTY_BASE%" -Djetty_perc_defaults="%JETTY_DEFAULTS%" --include-jetty-dir="%JETTY_DEFAULTS%" --stop

endlocal
