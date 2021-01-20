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

if [ -d "../../JRE" ]; then
    JAVA_HOME=../../JRE
    else
    JAVA_HOME=../../Staging/JRE
fi
export JAVA_HOME
ANT_HOME=../InstallToolkit
export ANT_HOME
CLASSPATH=
export CLASSPATH

chmod u+x $ANT_HOME/bin/ant
if [ "$1" = "--skipVersion" ]
then
    echo Skipping Version Check...
    exec $ANT_HOME/bin/ant --noconfig -buildfile config/deploy.xml uninstall -DSKIP_VERSION=true
else
    echo Including Version Check...
    exec $ANT_HOME/bin/ant --noconfig -buildfile config/deploy.xml uninstall -DSKIP_VERSION=false
fi