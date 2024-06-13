#!/bin/bash
# chkconfig: 444 66 22
echo "Script To Install Staging DTS Linux Service."

SERVICE_NAME=PercussionStagingDTS

if [ "$(id -u)" != "0" ]; then
	echo "This script must be run with sudo or as root"
	exit 1
fi

function usage() {
	echo "Usage: $0 [ Service name default : PercussionStagingDTS ] {install | uninstall }"
	exit 1
}

if [ $# -gt 1 ];then
	SERVICE_NAME="$1"
	shift
fi

if [ "$1" == "uninstall" ]; then
	uninstall=true
elif [ "$1" == "install" ]; then
	uninstall=false
else
	usage
fi

function checkForStagingDTSService() {
	while read -r line; do
		service=$(basename ${line})
		echo "Found DTS service $service in $line"
		serviceHome=$(bash -c "source ${line} >/dev/null 2>1 ; echo \${CTATALINA_HOME}")
		echo Service Name: ${SERVICE_NAME} 
		if [ "$serviceHome" == "$CATALINA_HOME" ]; then
			currentService=${service}
		fi
		echo CurrentService: ${currentService}
	done < <(grep -l /etc/default/* -e 'CATALINA_HOME')
}

function removeServiceFromStartup {

	echo "Uninstalling service $1 from startup"
	if [[ $(type -P "chkconfig") ]]; then
		echo "Using command 'chkconfig "${SERVICE_NAME}" off'"
		chkconfig ${SERVICE_NAME} off
	elif [[ $(type -P "update-rc.d") ]]; then
		echo "Using command 'update-rc.d -f ${1} remove'"
		update-rc.d -f ${SERVICE_NAME} remove
	else
		echo "Cannot find chkconfig or update-rc.d to remove service looking to removing from rc.d folders"
	fi

	if [ -d "/etc/rc.d/rc2.d" ]; then
		echo "Removing links to /etc/rc.d/rc*.d/S??${1} and /etc/rc.d/rc*.d/K??${1}"
		rm -f /etc/rc.d/rc?.d/S??${SERVICE_NAME}
		rm -f /etc/rc.d/rc?.d/K??${SERVICE_NAME}
	fi

	if [ -d "/etc/rc2.d" ]; then
		echo "Removing links to /etc/rc*.d/S??${1} and /etc/rc*.d/K??${1}"
		rm -f /etc/rc?.d/S??${SERVICE_NAME}
		rm -f /etc/rc?.d/K??${SERVICE_NAME}
	fi 

}

function removeServiceScript() {
	echo "Removing files."
	set -x
	rm -f "/etc/systemd/${SERVICE_NAME}"
	rm -f "/etc/default/${SERVICE_NAME}"
	{ set +x; } > /dev/null 2>&1 
}

function abspath() {
    # generate absolute path from relative path
    # $1     : relative filename
    # return : absolute path
    if [ -d "$1" ]; then
        # dir
        (cd "$1"; pwd)
    elif [ -f "$1" ]; then
        # file
        if [[ $1 = /* ]]; then
            echo "$1"
        elif [[ $1 == */* ]]; then
            echo "$(cd "${1%/*}"; pwd)/${1##*/}"
        else
            echo "$(pwd)/$1"
        fi
    fi
}
distVersion=$(cat /proc/version 2>&1)

CATALINA_HOME=$(dirname $(abspath $0))
EXECUTABLE="${CATALINA_HOME}/bin/catalina.sh"
rxDir=$(dirname $(abspath ../../../Version.properties))
RX_USER=$(ls -ld ${rxDir} | awk '{print $3}')
RX_GROUP=$(ls -ld ${rxDir} | awk '{print $4}')
TOMCAT_RUN=/var/run/PercussionStagingService/${SERVICE_NAME}
echo ${EXECUTABLE}

if [[ $(type -P "service") ]]; then
	serviceCmd="service ${SERVICE_NAME}"
else
	serviceCmd="/etc/systemd/${SERVICE_NAME}"
fi

echo before uninstall $uninstall

if [ "$uninstall" != "true" ]; then
	if [ -f "/etc/systemd/${SERVICE_NAME}" ]; then
		echo "Service $SERVICE_NAME already installed"
		exit 1
	fi

checkForStagingDTSService

if [ ! -z "$currentService"]; then
	echo "A service with configuration at /etc/default/${service} is already set up to start this instance"
	exit 1
fi

echo "CATALINA_HOME=${CATALINA_HOME}"
echo "rxDir=${rxDir}"

if [ -d ${rxDir}/JRE ]; then
	echo "Found ${rxDir}/JRE to use as JRE folder"
	JAVA_HOME=${rxDir}/JRE
else
	JAVA_HOME=${rxDir}/Staging/JRE
fi

echo "Ensuring permissions are set correctly and match top level ${rxDir} folder user=${RX_USER} group=${RX_GROUP}"
chown -R "${RX_USER}:${RX_GROUP}" "${rxDir}"

echo "Setting up pid folder /var/run/${SERVICE_NAME} setting ownership to user=${RX_USER} group=${RX_GROUP}"
mkdir -p ${TOMCAT_RUN}
chown -R "${RX_USER}:${RX_GROUP}" "${TOMCAT_RUN}"
echo "Copying startup script ${CATALINA_HOME}/bin/catalina.sh /etc/systemd/${SERVICE_NAME}"
sed -e "s/\${PercussionStagingDTS_service}/$SERVICE_NAME/" ${CATALINA_HOME}/bin/catalina.sh  >> /etc/systemd/${SERVICE_NAME}
sed -i "3 a CATALINA_HOME=${CATALINA_HOME}" /etc/systemd/${SERVICE_NAME}
sed -i "4 a JAVA_HOME=${JAVA_HOME}" /etc/systemd/${SERVICE_NAME}
chmod 755 "/etc/systemd/${SERVICE_NAME}"


cat <<-EOF > /etc/default/${SERVICE_NAME}
	JAVA_HOME="${JAVA_HOME}"
	CATALINA_HOME="${CATALINA_HOME}"
	CATALINA_BASE="${CATALINA_HOME}"
	CATALINA_OUT="${CATALINA_HOME}/logs/catalina.log"
	CATALINA_PID="${TOMCAT_RUN}/PercussionStagingDTS.pid"
EOF

echo "Configuration for service ${SERVICE_NAME} in /etc/systemd/${SERVICE_NAME} must reinstall or update if paths change"

echo "************"
cat /etc/default/${SERVICE_NAME}
echo "************"
${serviceCmd} check
echo "************"
echo "Attempting to use chkconfig or update-rc.d to start the service automatically on server startup"

if [[ $(type -P "chkconfig") ]]; then
	echo "Using 'chkconfig ${SERVICE_NAME} on ' to add to server startup"
	echo "Ignore if error is shown about runlevel 4"
	chkconfig ${SERVICE_NAME} off > /dev/null 2>&1
	chkconfig ${SERVICE_NAME} on
elif [[ $(type -P "update-rc.d") ]]; then
	echo "Using 'update-rc.d ${SERVICE_NAME} defaults' to add to server startup"
	update-rc.d ${SERVICE_NAME} defaults
elif [ -d "/etc/rc2.d" ]; then
	echo "Fall back to symbolic linking  into /etc/rcx.d folders"
	ln /etc/systemd/${SERVICE_NAME} /etc/rc2.d/S99${SERVICE_NAME}
	ln /etc/systemd/${SERVICE_NAME} /etc/rc0.d/K99${SERVICE_NAME}
else
	echo "Cannot find  chkconfig or update-rc.d or /etc/rc2.d to run service on startup consult documentation for alternative"
	echo ${distVersion}
fi

echo "*************"
echo "       Start service with '${serviceCmd} start'"
echo "       Stop service with '${serviceCmd} stop'"
echo "       use '${serviceCmd}' without parameters to check other options"
echo "*************"

else #Uninstall
checkForStagingDTSService

if [ ! -f "/etc/systemd/${SERVICE_NAME}" ]; then
	echo "Service $SERVICE_NAME not installed in /etc/systemd/${SERVICE_NAME}"
	exit 1
fi

if cat "/etc/systemd/${SERVICE_NAME}" | grep -q "catalina"; then
	echo "Found service installed to /etc/systemd/${SERVICE_NAME}"
else
	cat "/etc/systemd/${SERVICE_NAME}" | grep -q "catalina"
	echo "Service installed to /etc/systemd/${SERVICE_NAME} is not a Percussion Staging DTS service"
	exit 1
fi

echo "Posting service shutdown command..."
service "${SERVICE_NAME}" stop


removeServiceFromStartup ${SERVICE_NAME}
removeServiceScript ${SERVICE_NAME}

fi
echo "Done"
