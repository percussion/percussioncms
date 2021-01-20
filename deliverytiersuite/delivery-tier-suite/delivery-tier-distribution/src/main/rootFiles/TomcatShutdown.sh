#!/bin/bash
#
#     Percussion CMS
#     Copyright (C) 1999-2020 Percussion Software, Inc.
#
#     This program is free software: you can redistribute it and/or modify
#     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#     This program is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU Affero General Public License for more details.
#
#     Mailing Address:
#
#      Percussion Software, Inc.
#      PO Box 767
#      Burlington, MA 01803, USA
#      +01-781-438-9900
#      support@percussion.com
#      https://www.percusssion.com
#
#     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
#

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