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
#      https://www.percusssion.com
#
#     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
#

EXECJAVA=java

if [ -r "../../JRE/bin/java" ]; then
  EXECJAVA="../../JRE/bin/java"
else
  echo "JRE does not exist in this location: ..\..\JRE\bin\java.exe"
  exit -1
fi


$EXECJAVA -classpath ../server/rx/deploy/RxServices.war/WEB-INF/lib/rxtablefactory.jar:../server/rx/deploy/RxServices.war/WEB-INF/lib/rxclient.jar:../server/rx/deploy/RxServices.war/WEB-INF/lib/rxutils.jar:../lib/endorsed/xml-apis.jar:../lib/endorsed/xercesImpl.jar:../server/rx/lib/jtds.jar:../server/rx/lib/ojdbc14.jar:../server/rx/lib/db2jcc.jar:../server/rx/lib/db2jcc_license_cu.jar:../server/rx/lib/mysql-connector-java-5.1.6-bin.jar:../server/rx/lib/saxon.jar: com.percussion.tablefactory.tools.PSTDToolDialog




