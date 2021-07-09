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

# This script will invoke the JBoss shutdown script
JAVA_HOME=./JRE
export JAVA_HOME
./AppServer/bin/jboss_shutdown.sh -s localhost:{{NAMING_PORT}} &
sleep 60
#Shutdown Production Tomcat only if Deployment directory exists
if [ -d Deployment ] ; then
  if [ -f TomcatShutdown.sh ] ; then
  	echo "Issuing Production DTS shutdown command"
    ./TomcatShutdown.sh &
  fi
else
  echo "Production DTS not detected in installation directory...Skipping shutdown command."
fi
#Shutdown Staging Tomcat
if [ -d Staging ] ; then
   echo "Issuing Staging DTS shutdown command"
   cd Staging
   ./TomcatShutdown.sh &
   cd ..
else
   echo "Staging DTS not detected in installation directory...Skipping shutdown command."
fi
sleep 60
if [ -f  ./DatabaseShutdown.sh ] ; then
   ./DatabaseShutdown.sh demo
fi
