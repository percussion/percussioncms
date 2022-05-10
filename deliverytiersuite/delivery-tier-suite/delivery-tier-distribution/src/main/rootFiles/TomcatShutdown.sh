#!/bin/bash
################
# Start Tomcat
################
#set -x
CURDIR=`pwd`
export SERVER_DIR=$CURDIR/Deployment/Server

if [ -f "JRE/bin/java" ]
then
    cd JRE
elif [ -f "../JRE/bin/java" ]
then
    cd ../JRE
fi

export JAVA_HOME=`pwd`
export JRE_HOME=`pwd`

cd bin

export JAVA_OPTS="$JAVA_OPTS -Dhttps.protocols=TLSv1.2 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Dfile.encoding=UTF-8 -Xmx1024m -Dnet.sf.ehcache.skipUpdateCheck=true -Djava.endorsed.dirs=$SERVER_DIR/endorsed  -Dcatalina.base=$SERVER_DIR -Dcatalina.home=$SERVER_DIR -Djava.io.tmpdir=$SERVER_DIR/temp -Dderby.system.home=$SERVER_DIR/derbydata"
export CATALINA_HOME=$SERVER_DIR
$SERVER_DIR/bin/catalina.sh stop
