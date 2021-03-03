@echo off
rem This script will invoke the JBoss shutdown script
set JAVA_HOME=.\JRE
set NOPAUSE=true
START /B .\AppServer\bin\jboss_shutdown.bat -s localhost:{{NAMING_PORT}}

REM Shutdown Tomcat if the Shutdown Script is installed.
IF EXIST .\TomcatShutdown.bat (
	START /B .\TomcatShutdown.bat
)

:CKSRVR
del .\server_run_lock >> .\shutdown.tmp 2>&1
IF EXIST .\server_run_lock (
    GOTO WAIT
) ELSE (
    GOTO END
)

:WAIT
ping 127.0.0.1 -n 10 -w 1000 >> .\shutdown.tmp 2>&1
GOTO CKSRVR

:END
IF EXIST .\DatabaseShutdown.bat (
    START /B /WAIT .\DatabaseShutdown.bat demo
)

rem Verify port is released
:checkport
netstat -n | find ":{{NAMING_PORT}} " > netstat.tmp 2<&1

find "FIN_WAIT" netstat.tmp >> shutdown.tmp 2<&1
if %errorlevel%==0 (
   GOTO checkport
)
find "TIME_WAIT" netstat.tmp >> shutdown.tmp 2<&1
if %errorlevel%==0 (
   GOTO checkport
)

:exit
del .\shutdown.tmp
del .\netstat.tmp
EXIT
