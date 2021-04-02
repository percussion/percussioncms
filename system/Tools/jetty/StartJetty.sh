#!/bin/bash -bm
#
#     Percussion CMS
#     Copyright (C) 1999-2021 Percussion Software, Inc.
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

scriptname="$(basename "$0")"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rxDir=$(dirname ${DIR})
echo rxDir=$rxDir

JAVA_HOME=${rxDir}/JRE
echo JAVA_HOME=$JAVA_HOME

JETTY_HOME=${DIR}/upstream
echo JETTY_HOME=$JETTY_HOME

JETTY_BASE=${DIR}/base
echo JETTY_BASE=$JETTY_BASE

JETTY_DEFAULTS=${DIR}/defaults
JETTY_DEFAULTS=$JETTY_DEFAULTS

PID=0

#  Check if service is not installed but process is already running from this script
function check_pid {
	if [ -f "${PID_FILE}" ];then
		testpid=$(<"$PID_FILE")
		if ps -p $testpid >/dev/null 2>&1 ; then
			PID=$testpid
		else
			echo removing stale pidfile ${PID_FILE}
			rm -f ${PID_FILE}
		fi  
	fi
}

##  Checking User and change to correctu user if running under root or sudo
RX_USER=$(ls -ld ${rxDir} | awk '{print $3}')
if [ "${RX_USER}" != `whoami` ];then
	if [ $UID -eq 0 ]; then
		#rerun script changing user
		exec su "${RX_USER}" -- "$0" "$@"
	fi
	echo "You must run as root or the user \""${RX_USER}"\" that owns the root directory ${rxDir}"
	exit 1 
fi

PID_FILE="${JETTY_BASE}/etc/running.pid"

check_pid 

if [ ${PID} -gt 0 ]; then
	runningOnConsole=true
fi
## Find if this instance has been installed as a service
while read -r line; do
	service=$(basename ${line})
	serviceHome=$(bash -c "source ${line} >/dev/null 2>&1 ; echo \${JETTY_BASE}")
	echo "Found Jetty service $service in $line pointing to jetty base $serviceHome"
	if [ "$serviceHome" == "$JETTY_BASE" ]; then
		currentService=$service
	fi 
done < <(grep -l /etc/default/* -e 'JETTY_BASE' 2>/dev/null)

if [ ! -z $currentService ]; then
	PID_FILE="/var/run/rxjetty/${currentService}/rxjetty.pid"
	check_pid  
	echo
	echo "This instance is currently installed as service ${currentService}"
fi

## If the service is started we restrict the command line options that will pass 
## through to the jetty start.jar

RUN_CMD="${JAVA_HOME}/bin/java -XX:+DisableAttachMechanism -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar ${JETTY_HOME}/start.jar -Djetty_perc_defaults=${JETTY_DEFAULTS} -Drxdeploydir=${rxDir} -DTIKA_CONFIG=${rxDir}/rxconfig/tika-config.xml -Djetty.base=${JETTY_BASE} --include-jetty-dir=${JETTY_DEFAULTS} $@"
echo RUN_CMD=$RUN_CMD

if [ ${PID} -gt 0 ]; then
	for arg in "$@"; do
		case "$arg" in
		--help)
			nostart=true
			;;
		--version)
			nostart=true
			;;
		--list-classpath)
			nostart=true
			;;
		--list-config)
			nostart=true
			;;
		--dry-run)
			nostart=true
			;;
		--list-modules)
			nostart=true
			;;
		--list-all-modules)
			nostart=true
			;;
		--add-to-start)
			nostart=true
			;;
		--upadate-ini)
			nostart=true
			;;
		--create-startd)
			nostart=true
			;;
		--write-module-=*)
			nostart=true
			;;

		esac
	done
	if [ "$nostart" != true ];then
		echo
		if [ "$runningOnConsole" != true ];then
			
			echo "Service $currentService is already started with process id ${PID}"
		else
			echo "${scriptname} is already is already started with process id ${PID}"
		fi
		echo "  you can still use Jetty arguments on this script that do not start the service. see \"${scriptname} --help\" to show jetty help"
		exit 1
	fi
exec $RUN_CMD

fi


cleanup(){
	rm -rf ${PID_FILE}
}
## add current processid to pid file Change directory and run Jetty start.jar
_term() {
	kill -TERM "$child" 2>/dev/null
}

trap _term SIGINT SIGQUIT SIGTERM 

cd ${JETTY_BASE}
${RUN_CMD} &
child=$!
echo /var/run/rxjetty/${currentService}

echo $child > "${PID_FILE}"
wait $child
#Child process died so cleanup and exit
cleanup
echo
echo

