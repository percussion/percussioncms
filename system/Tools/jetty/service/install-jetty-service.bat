@echo off


IF "%1"=="" (
  ECHO Invalid argument: %1
  ECHO.
  ECHO Usage:  %~n0  command
  ECHO.
  ECHO Where:  command may be install, delete, update, start, stop
  GOTO:EOF
)


SET parent=%~dp0
set SERVICE_NAME=%2

IF "%2"=="" (
set SERVICE_NAME=PercussionCMS
)

FOR %%a IN ("%parent:~0,-1%") DO SET JETTY_ROOT=%%~dpa




set PR_DESCRIPTION="Percussion Services For CMS - Jetty Server"
set JETTY_HOME="%JETTY_ROOT%upstream"
set JETTY_BASE="%JETTY_ROOT%base"
set JETTY_DEFAULTS="%JETTY_ROOT%defaults"
set STOPKEY=SHUTDOWN

REM Note stop port number must be available and unique on the server.  Copy this file,
REM Change this number and the service name if you need
REM to create a separate service.
set STOPPORT=50011

set PR_INSTALL="%JETTY_ROOT%service\win\prunsrv.exe"

@REM Service Log Configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=%JETTY_ROOT%base\logs\
REM set value of following to variables to auto to create log file.  Usually log will go into server.log.  use this for debugging.
set PR_STDOUTPUT=auto
set PR_STDERROR=auto
set PR_LOGLEVEL=Error

@REM Path to Java Installation

CALL :NORMALIZEPATH "%JETTY_ROOT%..\JRE"
set JAVA_HOME=%RETVAL%
set PR_JVM="%JAVA_HOME%\bin\server\jvm.dll"
set PR_CLASSPATH="%JETTY_HOME%\start.jar;%JAVA_HOME%\lib\tools.jar"

@REM JVM Configuration
set PR_JVMMS=128
set PR_JVMMX=512
set PR_JVMSS=4000
set PR_JVMOPTIONS=-Djetty.home="%JETTY_HOME%";-Djetty_perc_defaults="JETTY_DEFAULTS";-Djetty.base="%JETTY_BASE%"
@REM Startup Configuration
set JETTY_START_CLASS=%JETTY_ROOT%StartJetty.bat
set JETTY_STOP_CLASS=%JETTY_ROOT%StopJetty.bat

set PR_STARTUP=auto
set PR_STARTMODE=exe
set PR_STARTCLASS=%JETTY_START_CLASS%
set PR_START_PATH=%JETTY_ROOT%
set PR_STARTPARAMS=-Drxdeploydir=%JETTY_ROOT%..;-DSTOP.PORT=%STOPPORT%;-DSTOP.KEY=%STOPKEY%

@REM Shutdown Configuration
set PR_STOPMODE=exe
set PR_STOPCLASS=%JETTY_STOP_CLASS%
set PR_STOP_PATH=%JETTY_ROOT%
set PR_STOPPARAMS=--stop;-Drxdeploydir=%JETTY_ROOT%..;-DSTOP.PORT=%STOPPORT%;-DSTOP.KEY=%STOPKEY%;

%PR_INSTALL% %1 %SERVICE_NAME% ^
  --DisplayName="%SERVICE_NAME%" ^
  --Install=%PR_INSTALL% ^
  --Startup=%PR_STARTUP% ^
  --LogPath=%PR_LOGPATH% ^
  --LogPrefix=%PR_LOGPREFIX% ^
  --LogLevel=%PR_LOGLEVEL% ^
  --StdOutput=%PR_STDOUTPUT% ^
  --StdError=%PR_STDERROR% ^
  --JavaHome=%JAVA_HOME% ^
  --Jvm=%PR_JVM% ^
  --StartMode=%PR_STARTMODE% ^
  --StartImage=%PR_STARTCLASS% ^
  --StartPath=%JETTY_ROOT% ^
  --StopMode=%PR_STOPMODE% ^
  --StopImage=%PR_STOPCLASS%


if not errorlevel 1 goto installed
echo Failed to run %1 on service "%SERVICE_NAME%".  Refer to log in %PR_LOGPATH%
goto end

:installed
echo The %1 command on Service "%SERVICE_NAME%" succeeded.

REM REMOVEJBOSSSERVICE script will be called only on install command for installing jetty i.e. calling install-jetty-service.bat install
IF "%1"=="install" goto REMOVEJBOSSSERVICE
goto end

:REMOVEJBOSSSERVICE
REM Removing existing JBOSS service after successful jetty service install.
REM Remove JBOSS script will be called only after successful install of jetty service.
set JBOSS_SERVICE_NAME=
REM User Input for existing JBOSS Service Name. If blank input provided default value will be "Percussion Service"
set /p JBOSS_SERVICE_NAME=Enter the existing JBOSS Service Name to be removed and press Enter (default is "Percussion Service") :
if "%JBOSS_SERVICE_NAME%" equ "" set JBOSS_SERVICE_NAME="Percussion Service"
sc query %JBOSS_SERVICE_NAME% > NUL
if errorlevel 1060 (goto JBOSSMISSING) ELSE (goto JBOSSFOUND)
goto end

:JBOSSMISSING
echo JBOSS SERVICE NAMED %JBOSS_SERVICE_NAME% NOT FOUND
goto end

:JBOSSFOUND
echo JBOSS SERVICE NAMED %JBOSS_SERVICE_NAME% EXISTS
sc query %JBOSS_SERVICE_NAME% | find "RUNNING"
if "%ERRORLEVEL%"=="0" (
    echo JBOSS SERVICE NAMED %JBOSS_SERVICE_NAME% IS RUNNING
	echo STOPPING JBOSS SERVICE NAMED %JBOSS_SERVICE_NAME%
	net stop %JBOSS_SERVICE_NAME%
	echo DELETING JBOSS SERVICE NAMED %JBOSS_SERVICE_NAME%
	sc delete %JBOSS_SERVICE_NAME%
) else (
    echo JBOSS SERVICE NAMED %JBOSS_SERVICE_NAME% IS NOT RUNNING
	echo DELETING JBOSS SERVICE NAMED %JBOSS_SERVICE_NAME%
	sc delete %JBOSS_SERVICE_NAME%
)
goto end

:end

:: ========== FUNCTIONS ==========
EXIT /B

:NORMALIZEPATH
  SET RETVAL=%~dpfn1
  EXIT /B
