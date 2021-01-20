@echo off
setlocal

SET SCRIPT_DIR=%~dp0
SET SCRIPT_DIR=%SCRIPT_DIR:~0,-1%
SET SERVER_DIR=%SCRIPT_DIR%\Deployment\Server
SET SERVER_URL_PATH=file:///%SERVER_DIR:\=/%

set "currentpath=%CD%"
set JAVA_HOME=%SCRIPT_DIR%\JRE

if NOT EXIST %JAVA_HOME% (
SET JAVA_HOME=%SCRIPT_DIR%\..\JRE
)

set JRE_HOME=%SCRIPT_DIR%\JRE

if NOT EXIST %JRE_HOME% (
SET JRE_HOME=%SCRIPT_DIR%\..\JRE
)

set JAVA_OPTS=%JAVA_OPTS% -Dhttps.protocols=TLSv1.2 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Dfile.encoding=UTF-8 -Xmx1024m -Dnet.sf.ehcache.skipUpdateCheck=true -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:NewSize=256m -XX:SurvivorRatio=16 -Djava.endorsed.dirs=%SERVER_DIR%\endorsed  -Dcatalina.base=%SERVER_DIR% -Dcatalina.home=%SERVER_DIR% -Djava.io.tmpdir=%SERVER_DIR%\temp -Dderby.system.home=%SERVER_DIR%\derbydata
set CATALINA_HOME=%SERVER_DIR%
"%SERVER_DIR%\bin\catalina.bat" run
