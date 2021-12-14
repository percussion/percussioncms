#! /bin/sh
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
#      https://www.percussion.com
#
#     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
#
DB_PORT=1527
SERVER_HOME=`pwd`
export SERVER_HOME

rm -f $SERVER_HOME/log.xml

if [ ! -d $SERVER_HOME/bin ] ; then
    echo "This script must be executed from the root of the Rhythmyx installation."
    exit 1
fi

EXEC_INET_PORT=9650
export EXEC_INET_PORT

PATH=.:$SERVER_HOME/bin:$PATH
export PATH

LD_LIBRARY_PATH=$SERVER_HOME/bin:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH

#Startup unless server hasn't been installed
LAUNCHER_FILE_PATH=""
if [ -f $SERVER_HOME/PercussionServer.bin ] ; then
   LAUNCHER_FILE_PATH=$SERVER_HOME/PercussionServer.bin
else
   echo "$SERVER_HOME/PercussionServer.bin does not exist."
   exit 1
fi

#Checks if embedded database is being used
if [ -f $SERVER_HOME/DatabaseStartup.sh ] ; then
#Start Derby
netstat -ant|egrep -iq "\:$DB_PORT .*LISTEN"
if [ $? -gt 0 ]
then
   echo "Starting embedded database..."
   $SERVER_HOME/DatabaseStartup.sh &
else
   echo "Embedded database already up..."
fi

#Verify Derby Started
echo "Check DB status"
databaseUp=1
looptimes=30
currentloop=0
while [ $databaseUp -gt 0 ]
do
   if [ $currentloop -eq $looptimes ]
   then
      echo "Database did not startup."
      exit
   fi
   sleep 10
   netstat -ant|egrep -iq "\:$DB_PORT .*LISTEN"
   databaseUp=$?
   currentloop=`expr $currentloop + 1`
done
fi

#Start Production Tomcat
if [ -f $SERVER_HOME/TomcatStartup.sh ] ; then
   echo "Starting Production Tomcat"
   $SERVER_HOME/TomcatStartup.sh &
else
   echo "Production DTS not detected in installation directory...Skipping startup command."
fi
#Start Staging Tomcat
if [ -d $SERVER_HOME/Staging ] ; then
   echo "Starting Staging Tomcat"
   cd Staging
   ./TomcatStartup.sh &
   cd ..
else
   echo "Staging DTS not detected in installation directory...Skipping startup command."
fi

# Start server
echo "Starting Server"
$LAUNCHER_FILE_PATH
