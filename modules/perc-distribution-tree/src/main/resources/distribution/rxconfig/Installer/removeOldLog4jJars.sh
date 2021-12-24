#!/bin/bash

rm -f ..sys_resources/webapps/secure/WEB-INF/lib/log4j*.jar

rm -f ../../Deployment/Server/lib/log4j-api-2.7.jar
rm -f ../../Deployment/Server/lib/log4j-core-2.7.jar
rm -f ../../Deployment/Server/lib/log4j-1.2-api-2.7.jar

echo Updating Staging DTS files...
rm -f ../../Staging/Deployment/Server/lib/log4j-api-2.7.jar
rm -f ../../Staging/Deployment/Server/lib/log4j-core-2.7.jar
rm -f ../../Staging/Deployment/Server/lib/log4j-1.2-api-2.7.jar

echo done updating Staging DTS files - ignore errors if you do not have a Staging DTS in this location.

rm -f ../../jetty/defaults/lib/perc-logging/log4j*.jar

rm -f ../../jetty/defaults/lib/perc/log4j*.jar

rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/log4j*.jar

rm -f ../../Deployment/Server/log4j2/lib/log4j*.jar

rm -f ../../Deployment/Server/common/lib/log4j*.jar

echo Updating Staging DTS files...
rm -f ../../Staging/Deployment/Server/log4j2/lib/log4j*.jar

rm -f ../../Staging/Deployment/Server/common/lib/log4j*.jar
echo done updating Staging DTS files - ignore errors if you do not have a Staging DTS in this location.

find ../../PreInstall/Backups/ -type f -name 'log4j*.jar' -delete