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
#  Set CLASSPATH and Java options
#
#  $Id: setclasspath.sh,v 1.11 2005/01/10 18:16:10 remm Exp $
# -----------------------------------------------------------------------------

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
  echo "Neither the JAVA_HOME nor the JRE_HOME environment variable is defined"
  echo "At least one of these environment variable is needed to run this program"
  exit 1
fi
if [ -z "$JAVA_HOME" -a "$1" = "debug" ]; then
  echo "JAVA_HOME should point to a JDK in order to run in debug mode."
  exit 1
fi
if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi

# If we're running under jdb, we need a full jdk.
if [ "$1" = "debug" -o "$1" = "javac" ] ; then
  if [ "$os400" = "true" ]; then
    if [ ! -x "$JAVA_HOME"/bin/java -o ! -x "$JAVA_HOME"/bin/javac ]; then
      echo "The JAVA_HOME environment variable is not defined correctly"
      echo "This environment variable is needed to run this program"
      echo "NB: JAVA_HOME should point to a JDK not a JRE"
      exit 1
    fi
  else
#    if [ ! -x "$JAVA_HOME"/bin/java -o ! -x "$JAVA_HOME"/bin/jdb -o ! -x "$JAVA_HOME"/bin/javac ]; then
  if [ ! -x "$JAVA_HOME"/bin/java ]; then
      echo "The JAVA_HOME environment variable is not defined correctly"
      echo "This environment variable is needed to run this program"
      echo "NB: JAVA_HOME should point to a JDK not a JRE"
      exit 1
    fi
  fi
fi
if [ -z "$BASEDIR" ]; then
  echo "The BASEDIR environment variable is not defined"
  echo "This environment variable is needed to run this program"
  exit 1
fi
if [ ! -x "$BASEDIR"/bin/setclasspath.sh ]; then
  if $os400; then
    # -x will Only work on the os400 if the files are:
    # 1. owned by the user
    # 2. owned by the PRIMARY group of the user
    # this will not work if the user belongs in secondary groups 
    eval
  else
    echo "The BASEDIR environment variable is not defined correctly"
    echo "This environment variable is needed to run this program"
    exit 1
  fi
fi

# Set the default -Djava.endorsed.dirs argument
JAVA_ENDORSED_DIRS="$BASEDIR"/common/endorsed

# Set standard CLASSPATH
if [ "$1" = "debug" -o "$1" = "javac" ] ; then
  CLASSPATH="$JAVA_HOME"/lib/tools.jar
fi

# OSX hack to CLASSPATH
JIKESPATH=
if [ `uname -s` = "Darwin" ]; then
  OSXHACK="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Classes"
  if [ -d "$OSXHACK" ]; then
    for i in "$OSXHACK"/*.jar; do
      JIKESPATH="$JIKESPATH":"$i"
    done
  fi
fi

# Set standard commands for invoking Java.
  _RUNJAVA="$JRE_HOME"/bin/java
if [ "$os400" != "true" ]; then
  _RUNJDB="$JAVA_HOME"/bin/jdb
fi
_RUNJAVAC="$JAVA_HOME"/bin/javac
