#!/bin/sh

# -----------------------------------------------------------------------------
# Stop script for the CATALINA Server
#
# $Id: shutdown.sh,v 1.3 2002/08/04 18:19:43 patrickl Exp $
# -----------------------------------------------------------------------------
CATALINA_HOME={{APPSERVER_ROOT}}
export CATALINA_HOME

CATALINA_BASE=$CATALINA_HOME
export CATALINA_BASE

JAVA_HOME="$CATALINA_HOME"/JRE
export JAVA_HOME


# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR="$CATALINA_HOME"/bin
#commented by VAMSI PRGDIR=`dirname "$PRG"`
EXECUTABLE=catalina.sh

# Check that target executable exists
if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
  echo "Cannot find $PRGDIR/$EXECUTABLE"
  echo "This file is needed to run this program"
  exit 1
fi

exec "$PRGDIR"/"$EXECUTABLE" stop "$@"
