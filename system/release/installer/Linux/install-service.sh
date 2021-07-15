#!/bin/bash

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

SERVICE_NAME=percussion-service

if [ "$(id -u)" != "0" ]; then
    echo "This script must be run with sudo or as root" 2>&1
    exit 1
fi

function usage() {
    echo "Usage: $0 [ service name default : ${SERVICE_NAME} ] {install | uninstall}"
    exit 1
}

if [ $# -gt 1 ]; then
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

function checkForService() {
    while read -r line; do
        service=$(basename ${line})
        serviceHome=$(bash -c "source ${line} >/dev/null 2>&1 ; echo \${PERC_ROOT}")
        if [ "$serviceHome" == "$PERC_ROOT" ]; then
            currentService=$service
        fi
    done < <(grep -l /etc/default/* -e 'PERC_ROOT')
}

function removeServiceFromStartup() {
    echo "Uninstalling service $1 from startup"
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
    echo "Removing files"
    set -x
    rm -f "/etc/init.d/${1}"

    rm -f "/etc/default/${1}"
    { set +x; } > /dev/null 2>&1
}

distVersion=$(cat /proc/version 2>&1)

PARENT_DIR=$(dirname $(dirname $(readlink -f "$0")))
PERC_ROOT=$(dirname ${PARENT_DIR}) # rxconfig is 2 levels below root directory
PERC_USER=$(ls -ld ${PERC_ROOT} | awk '{print $3}')
RX_GROUP=$(ls -ld ${PERC_ROOT} | awk '{print $4}')

echo "PERC_ROOT is: " $PERC_ROOT
echo "PERC_USER is: " $PERC_USER
echo "RX_GROUP is: " $RX_GROUP

if [[ $(type -P "service") ]]; then
    serviceCmd="service ${SERVICE_NAME}"
else
    serviceCmd="/etc/init.d/${SERVICE_NAME}"
fi

echo "Service command is:" $serviceCmd
echo "**********"

if [ "$uninstall" != "true" ]; then
    echo
    echo "Installing the ${SERVICE_NAME} service..."

    if [  -f "/etc/init.d/${SERVICE_NAME}" ]; then
        echo "Service $SERVICE_NAME already installed"
        exit 1
    fi

    checkForService
    if [ ! -z "$currentService" ];
    then
        echo "A service with name $service with configuration at /etc/default/${service} is already set up to start this instance"
        exit 1
    fi

    echo "Setting location of Java executable..."

    JAVA=$(cat ${ls -l}/java.properties | sed -n -e 's/^JAVA=//p')

    if [ -z "$JAVA" ]
    then
        echo "Warning: did not find java.properties file in the root of the installation."
        echo "Attempting to use system JRE."
         if [ -d ${PERC_ROOT}/JRE/bin ]; then
            echo "Found ${PERC_ROOT}/JRE64 to use as JRE Folder"
            JAVA=${PERC_ROOT}/JRE/bin/java
        else
            JAVA=${PERC_ROOT}/JRE64/bin/java
        fi
    fi

    # If still not set, need to set java.properties
    if [ -z "$JAVA" ]; then
        echo "Did not find any JRE on the system.  Please update the java.properties file"
        echo "in the root of the server.  Add the 'JAVA=' property and set it to point"
        echo "to the java executable.  Must be version 1.8 of the JRE."
        exit 1
    fi

    echo
    echo "Java executable is:" ${JAVA}
    echo

    echo "Ensuring permissions are set correctly and match top level ${PERC_ROOT} folder user=${PERC_USER} group=${RX_GROUP}"
    chown -R "${PERC_USER}:${RX_GROUP}" "${PERC_ROOT}"
    echo

    echo "Copying startup script ${PERC_ROOT}/rxconfig/Installer/percussion-service.sh to /etc/init.d/${SERVICE_NAME}"

    sed -e "s/\${percussion-service_service}/$SERVICE_NAME/" ${PERC_ROOT}/rxconfig/Installer/percussion-service.sh > /etc/init.d/${SERVICE_NAME}
    chmod 755 "/etc/init.d/${SERVICE_NAME}"

    cat <<-EOF > /etc/default/${SERVICE_NAME}
    JAVA="${JAVA}"
    PERC_ROOT="${PERC_ROOT}"
    CATALINA_HOME="${PERC_ROOT}/Deployment/Server"
    PERC_USER="${PERC_USER}"
    SVC_WRAPPER="${PERC_ROOT}/perc-service-wrapper.jar"
    SVC_WRAPPER_CMD="${JAVA} -jar $PERC_ROOT/perc-service-wrapper.jar"
EOF

    echo "Configuration for service ${SERVICE_NAME} in /etc/init.d/${SERVICE_NAME} must be reinstalled or updated if paths change."
    echo

    echo "**********"
    cat /etc/default/${SERVICE_NAME}
    echo "**********"
    echo

    echo "Attempting to use chkconfig or update-rc.d to start service automatically on server startup"

    if [[ $(type -P "chkconfig" ) ]]; then
        echo "Using 'chkconfig ${SERVICE_NAME} on' to add to add to server startup"
        echo "Ignore if error is shown about runlevel 4"
        chkconfig ${SERVICE_NAME} off > /dev/null 2>&1
        chkconfig ${SERVICE_NAME} on
    elif [[ $(type -P "update-rc.d") ]]; then
        echo "Using 'update-rc.d ${SERVICE_NAME} defaults' to add to server startup"
        update-rc.d ${SERVICE_NAME} defaults
    elif [ -d "/etc/rc2.d" ]; then
        echo "Fall back to symbolic linking into /etc/rcx.d folders e.g. Solaris 9"
        ln /etc/init.d/${SERVICE_NAME} /etc/rc2.d/S99${SERVICE_NAME}
        ln /etc/init.d/${SERVICE_NAME} /etc/rc0.d/K99${SERVICE_NAME}
    else
        echo "Cannot find chkconfig or update-rc.d or /etc/rc2.d to run service on startup consult documentation on alternatives for your distro"
        echo ${distVersion}
    fi

    echo
    echo "********"
    echo "  Start service with '${serviceCmd} start'"
    echo "  Stop service with '${serviceCmd} stop'"
    echo "  Use '${serviceCmd}' without parameters to check other options"
    echo "********"
    echo

else # Uninstall
    echo
    echo "Uninstalling the ${SERVICE_NAME} service"
    echo "Checking for the ${SERVICE_NAME} service"
    checkForService
    echo

    if [ ! -f "/etc/init.d/${SERVICE_NAME}" ]; then
        echo "Service $SERVICE_NAME not installed in /etc/init.d/${SERVICE_NAME}"
        exit 1
    fi

    if cat "/etc/init.d/${SERVICE_NAME}" | grep -q "perc-service-wrapper.jar"; then
        echo "Found service installed to /etc/init.d/${SERVICE_NAME}"
    else
        cat "/etc/init.d/${SERVICE_NAME}" | grep -q "perc-service-wrapper.jar"
        echo "Service installed to /etc/init.d/${SERVICE_NAME} is not a Percussion Service"
        exit 1
    fi
    if  ${serviceCmd} status | grep -q "STARTED"; then
        echo "Service still running, shutting down...."
        "${serviceCmd}" stop
    fi

    echo

    removeServiceFromStartup ${SERVICE_NAME}
    removeServiceScript ${SERVICE_NAME}

    echo

fi
echo "Done"
