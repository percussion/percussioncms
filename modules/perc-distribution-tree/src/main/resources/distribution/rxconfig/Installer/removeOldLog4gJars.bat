@echo off

del /F /Q ..\..\sys_resources\webapps\secure\WEB-INF\lib\log4j*.jar

del /F /Q ..\..\Deployment\Server\lib\log4j-api-2.7.jar
del /F /Q ..\..\Deployment\Server\lib\log4j-core-2.7.jar
del /F /Q ..\..\Deployment\Server\lib\log4j-1.2-api-2.7.jar

echo Updating Staging DTS files...
del /F /Q ..\..\Staging\Deployment\Server\lib\log4j-api-2.7.jar
del /F /Q ..\..\Staging\Deployment\Server\lib\log4j-core-2.7.jar
del /F /Q ..\..\Staging\Deployment\Server\lib\log4j-1.2-api-2.7.jar

echo done updating Staging DTS files - ignore errors if you do not have a Staging DTS in this location.

del /F /Q ..\..\jetty\defaults\lib\perc-logging\log4j*.jar

del /F /Q ..\..\jetty\defaults\lib\perc\log4j*.jar

del /F /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\log4j*.jar

del /F /Q ..\..\Deployment\Server\log4j2\lib\log4j*.jar

del /F /Q ..\Deployment\Server\common\lib\log4j*.jar

echo Updating Staging DTS files...
del /F /Q ..\..\Staging\Deployment\Server\log4j2\lib\log4j*.jar

del /F /Q ..\..\Staging\Deployment\Server\common\lib\log4j*.jar
echo done updating Staging DTS files - ignore errors if you do not have a Staging DTS in this location.

del /S /Q ..\..\PreInstall\Backups\log4j*.jar