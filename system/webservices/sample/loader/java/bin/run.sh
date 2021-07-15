#!/bin/sh

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

# run this program from $RxRoot/WebServices/6.0.0/sample/loader/java directory.  Where the $RxRoot is the Rhythmyx installation directory.

CLASSPATH=lib/axis-1.4.1.jar:lib/axis-jaxrpc-1.4.1.jar:lib/jaxrpc.jar:lib/commons-logging-1.0.4.jar:lib/commons-discovery-0.5.jar:lib/axis-saaj-1.4.1.jar:lib/wsdl4j-1.5.1.jar:lib/mail.jar:lib/activation.jar:build/classes
export CLASSPATH

../../../../../JRE/bin/java -classpath $CLASSPATH com.percussion.webservices.sample.loader.PSLoader
