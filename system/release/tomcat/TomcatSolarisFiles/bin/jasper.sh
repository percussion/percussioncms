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
#      https://www.percusssion.com
#
#     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
#

# -----------------------------------------------------------------------------
# Script for Jasper compiler
#
# Environment Variable Prequisites
#
#   JASPER_HOME   May point at your Catalina "build" directory.
#
#   JASPER_OPTS   (Optional) Java runtime options used when the "start",
#                 "stop", or "run" command is executed.
#
#   JAVA_HOME     Must point at your Java Development Kit installation.
#
#   JAVA_OPTS     (Optional) Java runtime options used when the "start",
#                 "stop", or "run" command is executed.
#
# $Id: jasper.sh,v 1.2 2002/06/02 21:22:41 remm Exp $
# -----------------------------------------------------------------------------

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`
JASPER_HOME=`cd "$PRGDIR/.." ; pwd`
if [ -r "$JASPER_HOME"/bin/setenv.sh ]; then
  . "$JASPER_HOME"/bin/setenv.sh
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JASPER_HOME" ] && JASPER_HOME=`cygpath --unix "$JASPER_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# Get standard Java environment variables
if [ -r "$JASPER_HOME"/bin/setclasspath.sh ]; then
  BASEDIR="$JASPER_HOME"
  . "$JASPER_HOME"/bin/setclasspath.sh
else
  echo "Cannot find $JASPER_HOME/bin/setclasspath.sh"
  echo "This file is needed to run this program"
  exit 1
fi

# Add on extra jar files to CLASSPATH
for i in "$JASPER_HOME"/common/endorsed/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
for i in "$JASPER_HOME"/common/lib/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
for i in "$JASPER_HOME"/shared/lib/*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
CLASSPATH="$CLASSPATH":"$JASPER_HOME"/shared/classes

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  JASPER_HOME=`cygpath --path --windows "$JASPER_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# ----- Execute The Requested Command -----------------------------------------

if [ "$1" = "jspc" ] ; then

  shift
  exec "$_RUNJAVA" $JAVA_OPTS $JASPER_OPTS \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
    -Djasper.home="$JASPER_HOME" \
    org.apache.jasper.JspC "$@"

else

  echo "Usage: jasper.sh ( jspc )"
  echo "Commands:"
  echo "  jspc - Run the offline JSP compiler"
  exit 1

fi
