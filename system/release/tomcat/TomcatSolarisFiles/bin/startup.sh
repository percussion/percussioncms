#!/bin/sh

# -----------------------------------------------------------------------------
# Start Script for the CATALINA Server
#
# $Id: startup.sh,v 1.4 2004/11/17 20:17:46 yoavs Exp $
# -----------------------------------------------------------------------------

# Better OS/400 detection: see Bugzilla 31132
os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
esac

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
CATALINA_HOME={{APPSERVER_ROOT}}
export CATALINA_HOME
CATALINA_BASE=$CATALINA_HOME
export CATALINA_BASE

JAVA_HOME="$CATALINA_HOME"/JRE
export JAVA_HOME
# commented by VAMSI PRGDIR=`dirname "$PRG"`
PRGDIR="$CATALINA_HOME"/bin
EXECUTABLE=catalina.sh

# Check that target executable exists
if $os400; then
  # -x will Only work on the os400 if the files are: 
  # 1. owned by the user
  # 2. owned by the PRIMARY group of the user
  # this will not work if the user belongs in secondary groups
  eval
else
  if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
    echo "Cannot find $PRGDIR/$EXECUTABLE"
    echo "This file is needed to run this program"
    exit 1
  fi
fi 

exec "$PRGDIR"/"$EXECUTABLE" start "$@"
