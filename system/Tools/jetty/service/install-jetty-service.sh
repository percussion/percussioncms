#!/bin/bash
SERVICE_NAME=PercussionCMS
if [ "$(id -u)" != "0" ]; then
    echo "This script must be run with sudo or as root" 1>&2
    exit 1
fi


function usage() {
    echo "Usage: $0 [ service name default : PercussionCMS ] {install | uninstall | cleanupJBoss }"
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
elif [ "$1" == "cleanupJBoss" ]; then
    cleanupJBoss=true
else
    usage
fi

function checkForJettyService() {
    while read -r line; do
        service=$(basename ${line})
        echo "Found Jetty service $service in $line"
        serviceHome=$(bash -c "source ${line} >/dev/null 2>&1 ; echo \${JETTY_BASE}")
        if [ "$serviceHome" == "$JETTY_BASE" ]; then
            currentService=$service
        fi
    done < <(grep -l /etc/default/* -e 'JETTY_BASE')

}

function checkForJbossService() {
    while read -r line; do
        service=$(basename ${line})
        echo "Found JBoss service $service in $line"
        serviceHome=$(grep "^SERVER_DIR=" /etc/init.d/${service} | cut -d "=" -f 2)
        echo $serviceHome
        if [ "$serviceHome" == "$rxDir" ]; then
            currentService=$service
        fi
    done < <(grep -l /etc/init.d/* -e 'RhythmyxD')

    if [ ! -z "$currentService" ];
    then
        if [ "$cleanupJBoss" != "true" ]; then
            echo "Warning Jboss startup /etc/init.d/${currentService} for this instance ${rxDir} exists use  to remove"
            usage
        fi
        echo "Cleaning up JBoss init scripts"
        removeServiceFromStartup $currentService
        removeServiceScript $currentService
        exit 1
    fi
}

function removeServiceFromStartup() {

    echo "uninstalling service $1 from startup"
    if [[ $(type -P "chkconfig") ]]; then
        echo "Using command 'chkconfig "${1}" off'"
        chkconfig ${1} off
    elif [[ $(type -P "update-rc.d") ]]; then
        echo "using command 'update-rc.d -f ${1} remove'"
        update-rc.d -f ${SERVICE_NAME} remove
    else
        echo "Cannot find chkconfig or update-rc.d to remove service looking to removing from rc?.d folders"
    fi

    if [ -d "/etc/rc.d/rc2.d" ]; then
        echo "Removing links to etc/rc.d/rc*.d/S??${1} and /etc/rc.d/rc*.d/K??${1}"
        rm -f /etc/rc.d/rc?.d/S??${1}
        rm -f /etc/rc.d/rc?.d/K??${1}
    fi
    if [ -d "/etc/rc2.d" ]; then
        echo "Removing links to etc/rc*.d/S??${1} and /etc/rc*.d/K??${1} "
        rm -f /etc/rc?.d/S??${1}
        rm -f /etc/rc?.d/K??${1}
    fi


}

function removeServiceScript() {

    echo "removing files"
    set -x
    rm -f "/etc/init.d/${1}"
    rm -f "/etc/default/${1}"
    rm -rf "$JETTY_RUN"
    rm -f "$JETTY_BASE/${SERVICE_NAME}.state"
    { set +x; } > /dev/null 2>&1

}



distVersion=$(cat /proc/version 2>&1)


JETTY_ROOT=$(dirname $(dirname $(readlink -f "$0")))
JETTY_HOME=${JETTY_ROOT}/upstream
JETTY_BASE=${JETTY_ROOT}/base
JETTY_DEFAULTS=${JETTY_ROOT}/defaults
rxDir=$(dirname ${JETTY_ROOT})
RX_USER=$(ls -ld ${rxDir} | awk '{print $3}')
RX_GROUP=$(ls -ld ${rxDir} | awk '{print $4}')
JETTY_RUN=/var/run/rxjetty/${SERVICE_NAME}

if [[ $(type -P "service") ]]; then
    serviceCmd="service ${SERVICE_NAME}"
else
    serviceCmd="/etc/init.d/${SERVICE_NAME}"
fi
echo before uninstall $uninstall
if [ "$uninstall" != "true" ];then
echo in uninstall


    if [  -f "/etc/init.d/${SERVICE_NAME}" ];then
        echo "Service $SERVICE_NAME already installed"
        exit 1
    fi


    checkForJbossService
    checkForJettyService
    if [ ! -z "$currentService" ];
    then
        echo "A service with name $service with configuration at /etc/default/${service} is already set up to start this instance. Should be removed."
    fi

    echo "JETTY_ROOT=${JETTY_ROOT}"
    echo "rxDir=${rxDir}"

    if [ -d ${rxDir}/JRE ]; then
        echo "Found ${rxDir}/JRE to use as JRE Folder"
        JAVA_HOME=${rxDir}/JRE
    else
        JAVA_HOME=${rxDir}/JRE64
    fi


    if [ -f ${JETTY_BASE}/etc/jetty.conf ];then
        JETTY_CONF=${JETTY_BASE}/etc/jetty.conf
    else
        JETTY_CONF=${JETTY_DEFAULTS}/etc/jetty.conf
    fi
    echo "Please identify the user id that the percussion service should run as: <default: root>"
    read -r suppliedUser
    echo "Using user id: ${suppliedUser}"
    echo "Generating ${rxDir}/rx_user.id file"
    echo "SYSTEM_USER_ID=${suppliedUser}" > ${rxDir}/rx_user.id
    RX_USER=${suppliedUser}
    RX_GROUP=${suppliedUser}
    echo "Ensuring permissions are set correctly and match top level ${rxDir} folder user=${RX_USER} group=${RX_GROUP}"
    chown -R "${RX_USER}:${RX_GROUP}" "${rxDir}"
    echo
    echo

    echo "setting up pid folder /var/run/${SERVICE_NAME} setting ownership to  user=${RX_USER} group=${RX_GROUP}"

    mkdir -p ${JETTY_RUN}/${SERVICE_NAME}
    chown -R "${RX_USER}:${RX_GROUP}" "/var/run/rxjetty/${SERVICE_NAME}"
    chown -R "${RX_USER}:${RX_GROUP}" "${JETTY_RUN}/${SERVICE_NAME}"
    chmod -R ugo+rw /var/run/rxjetty/${SERVICE_NAME}

    echo "copying startup script ${JETTY_DEFAULTS}/bin/jetty.sh /etc/init.d/${SERVICE_NAME}"

    sed -e "s/\${rxjetty_service}/$SERVICE_NAME/" ${JETTY_DEFAULTS}/bin/rxjetty.sh > /etc/init.d/${SERVICE_NAME}
    chmod 755 "/etc/init.d/${SERVICE_NAME}"


    cat <<-EOF > /etc/default/${SERVICE_NAME}
    JAVA_HOME=${JAVA_HOME}
    JAVA=${JAVA_HOME}/bin/java
    JETTY_HOME=${JETTY_HOME}
    JETTY_BASE=${JETTY_BASE}
    JETTY_DEFAULTS=${JETTY_DEFAULTS}
    JETTY_CONF=${JETTY_CONF}
    JETTY_START_LOG=${JETTY_BASE}/logs/start.log
    JAVA_OPTIONS="-XX:+DisableAttachMechanism -Drxdeploydir=${rxDir} -Djetty_perc_defaults=${JETTY_DEFAULTS}"
    JETTY_RUN=${JETTY_RUN}
    JETTY_PID=${JETTY_RUN}/rxjetty.pid
    JETTY_ARGS="--include-jetty-dir=${JETTY_DEFAULTS} jetty-started.xml"
    JETTY_USER=${RX_USER}
EOF

    echo "configuration for service ${SERVICE_NAME} in /etc/init.d/${SERVICE_NAME} must reinstall or update if paths change"

    echo "**********"
    cat /etc/default/${SERVICE_NAME}
    echo "**********"
    ${serviceCmd} check
    echo "********"

    echo "Attempting to use chkconfig or update-rc.d to start service automatically on server startup"


    if [[ $(type -P "chkconfig" ) ]]; then
        echo "Using 'chkconfig ${SERVICE_NAME} on' to add to add to server startup"
        echo "Ignore if error is shown about runlevel 4"
        chkconfig ${SERVICE_NAME} off > /dev/null 2>&1
        chkconfig ${SERVICE_NAME} on
    elif [[ $(type -P "update-rc.d") ]]; then
        echo "using 'update-rc.d ${SERVICE_NAME} defaults' to add to server startup"
        update-rc.d ${SERVICE_NAME} defaults
    elif [ -d "/etc/rc2.d" ]; then
        echo "Fall back to symbolic linking into /etc/rcx.d folders e.g. Solaris 9"
        ln /etc/init.d/${SERVICE_NAME} /etc/rc2.d/S99${SERVICE_NAME}
        ln /etc/init.d/${SERVICE_NAME} /etc/rc0.d/K99${SERVICE_NAME}
    else
        echo "Cannot find chkconfig or update-rc.d or /etc/rc2.d to run service on startup consult documentation on alternatives for your distro"
        echo ${distVersion}
    fi

    echo "********"
    echo "  Start service with '${serviceCmd} start'"
    echo "  Stop service with '${serviceCmd} stop'"
    echo "  use '${serviceCmd}' without parameters to check other options"
    echo "********"

else # Uninstall
echo checking for jetty service
    checkForJettyService

    if [ ! -f "/etc/init.d/${SERVICE_NAME}" ];then
        echo "Service $SERVICE_NAME not installed in /etc/init.d/${SERVICE_NAME}"
        exit 1
    fi

    if cat "/etc/init.d/${SERVICE_NAME}" | grep -q "jetty"; then
        echo "Found service installed to /etc/init.d/${SERVICE_NAME}"
    else
        cat "/etc/init.d/${SERVICE_NAME}" | grep -q "jetty"
        echo "Service installed to /etc/init.d/${SERVICE_NAME} is not a Rhythmyx Jetty Service"
        exit 1
    fi
    if  ${serviceCmd} status | grep "Jetty running" ; then
        echo "Service still running, shutting down...."
        ${serviceCmd} stop
    fi



    removeServiceFromStartup ${SERVICE_NAME}
    removeServiceScript ${SERVICE_NAME}


fi
echo "done"
