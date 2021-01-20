#!/usr/bin/env bash

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

# LSB Tags
### BEGIN INIT INFO
# Provides:          ${percussion-service_service}
# Required-Start:    $local_fs $network
# Required-Stop:     $local_fs $network
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: percussion-dts start script.
# Description:       Start Percussion services.  Will start any
# service under the current directory.
### END INIT INFO

# Startup script for Percussion Services under *nix systems.

######################################################
# Display usage menu if no command line args detected
######################################################
usage()
{
    echo "Usage: ${0##*/} {start|stop|version|status}"
    exit 1
}

[ $# -gt 0 ] || usage

##################################################
# Some utility functions
##################################################
readConfig()
{
  (( DEBUG )) && echo "Reading $1.."
  source "$1"
}

findDirectory()
{
  local L OP=$1
  shift
  for L in "$@"; do
    [ "$OP" "$L" ] || continue
    printf %s "$L"
    break
  done
}

running()
{
  if  ${SVC_WRAPPER_CMD} --status | grep -q "Production DTS is STARTING" ; then
    return 0
  elif ${SVC_WRAPPER_CMD} --status | grep -q "Production DTS is STARTED" ; then
    return 0
  fi
  return 1
}

started()
{
  STATUS_CMD="${SVC_WRAPPER_CMD} --status"
  for T in 1 2 3 4 5 6 7 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32
  do
    sleep 5
    [ -z "$($STATUS_CMD | grep STARTING)" ] || echo -n ". "
    [ -z "$($STATUS_CMD | grep STARTED)" ] || return 0
    [ -z "$($STATUS_CMD | grep STOPPED)" ] || return 1
    [ -z "$($STATUS_CMD | grep FAILED)" ] || return 1
    #echo -n ". "
  done

  return 1;
}

#####################################################
# Set the name which is used by other variables.
# Defaults to the file name without extension.
#####################################################
NAME=$(echo $(basename $0) | sed -e 's/^[SK][0-9]*//' -e 's/\.sh$//')

##################################################
# Read any configuration files
##################################################
ETC=/etc
if [ $UID != 0 ]
then
  ETC=$HOME/etc
fi

for CONFIG in {/etc,~/etc}/default/${NAME}{,9} $HOME/.${NAME}rc; do
  if [ -f "$CONFIG" ] ; then
    readConfig "$CONFIG"
  fi
done

##################################################
# Try to determine PERC_ROOT if not set
##################################################
if [ -z "$PERC_ROOT" ]
then
  DTS_SH=$0
  case "$DTS_SH" in
    /*)     PERC_ROOT=${DTS_SH%/*/*} ;;
    ./*/*)  PERC_ROOT=${DTS_SH%/*/*} ;;
    ./*)    PERC_ROOT=.. ;;
    */*/*)  PERC_ROOT=./${DTS_SH%/*/*} ;;
    */*)    PERC_ROOT=. ;;
    *)      PERC_ROOT=.. ;;
  esac
fi

##################################################
# No PERC_ROOT yet? We're out of luck!
##################################################
if [ -z "$PERC_ROOT" ]; then
  echo "** ERROR: PERC_ROOT not set, you need to set it or install in a standard location."
  exit 1
fi

cd "$DTS_HOME"
DTS_HOME=$PWD

##################################################
# Setup DTS_STATE if not set
##################################################
if [ -z "$DTS_STATE" ]
then
  DTS_STATE=${PERC_ROOT}/Deployment/dts.state
fi

##################################################
# Setup JAVA if unset
##################################################
if [ -z "$JAVA" ]
then
  JAVA=$(which java)
fi

if [ -z "$JAVA" ]
then
  echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.8) in your PATH." >&2
  exit 1
fi

##################################################
# Setup SVC_WRAPPER if not set
##################################################
if [ -z "$SVC_WRAPPER" ]
then
  SVC_WRAPPER=${PERC_ROOT}/perc-service-wrapper.jar
fi

##################################################
# Setup SVC_WRAPPER_CMD if not set
##################################################
if [ -z "$SVC_WRAPPER_CMD" ]
then
  SVC_WRAPPER_CMD=${JAVA} -jar ${SVC_WRAPPER}
fi

RETVAL=0

case "$1" in

start)
    logger -s "Running start command..."
    /bin/su -l $PERC_USER -c "cd $PERC_ROOT 0<&- &>/dev/null
    nohup ${SVC_WRAPPER_CMD} --start 0<&- &>/dev/null &"
    if started
      then
        echo "OK `date`"
        ${SVC_WRAPPER_CMD} --status
      else
        echo "FAILED `date`"
        ${SVC_WRAPPER_CMD} --status
        exit 1
    fi
    ;;
stop)
    logger -s "Running stop command..."
    /bin/su -l $PERC_USER -c "cd $PERC_ROOT 0<&- &>/dev/null
    nohup ${SVC_WRAPPER_CMD} --stop 0<&- &>/dev/null &"
    ;;
version)
    echo "Version not implemented."
    ;;
status)
    /bin/su -l $PERC_USER -c "cd $PERC_ROOT 0<&- &>/dev/null
    ${SVC_WRAPPER_CMD} --status"
    ;;
*)
echo $"Usage: $0 {start|stop|status|version}"
exit 1
;;
esac
exit $RETVAL
