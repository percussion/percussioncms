#!/usr/bin/env sh

echo "------------------------------------------------"
echo "Percussion CMS Patch Uninstall Script  - 8.1.2.1"
echo "------------------------------------------------"
echo ""

# Get install dir parameter and drop trailing slash
INSTALL_DIR=${1%/}

helpFunction()
{
   echo ""
   echo "Usage: $0 <Path To PercussionCMS Installation> For example: $0 /opt/Percussion "
   echo ""
   exit 1 # Exit script after printing help
}

if [ -z "$1" ]
then
	echo "Installation directory parameter is missing.";
	helpFunction
fi

if [ ! -f $1/Version.properties ];
then
	echo "Version.properties not found at $INSTALL_DIR/Version.properties. Please confirm that $INSTALL_DIR contains a Percussion installation."
    exit 2
fi

if [ ! -d backup ];
then
	echo "The backup folder has been removed.  This patch can no longer be uninstalled."
    exit 2
fi


echo "Rolling back patch..."
if [ -e "backup/sys_resources/webapps/secure/WEB-INF/lib/commons-text-1.9.jar" ]
then
    echo "Reverting Commons Text for CVE-2022-42889..."
    /bin/cp -rf backup/sys_resources/webapps/secure/WEB-INF/lib/commons-text-1.9.jar $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/commons-text-1.9.jar
    rm -f $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/commons-text-1.10.0.jar

    /bin/cp -rf backup/jetty/defaults/lib/perc/commons-text-1.9.jar $INSTALL_DIR/jetty/defaults/lib/perc/commons-text-1.9.jar
    rm -f $INSTALL_DIR/jetty/defaults/lib/perc/commons-text-1.10.0.jar

    /bin/cp -rf backup/rxconfig/SiteConfigs/\$log\$/lib/commons-text-1.9.jar $INSTALL_DIR/rxconfig/SiteConfigs/\$log\$/lib/commons-text-1.9.jar
    rm -f $INSTALL_DIR/rxconfig/SiteConfigs/\$log\$/lib/commons-text-1.10.0.jar

    /bin/cp -rf backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.9.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.9.jar
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.10.0.jar
fi

if [ -e "backup/sys_resources/webapps/secure/WEB-INF/lib/spring-security-core-5.6.2.jar" ]
then
    echo "Reverting Spring Security for CVE-2022-31692.."
    /bin/cp -rf backup/sys_resources/webapps/secure/WEB-INF/lib/spring-security-*-5.6.2.jar $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib
    rm -f $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-*-5.6.9.jar
fi

if [ -e "backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-core-1.7.1.jar" ]
then
    echo "Reverting Apache Shiro for CVE-2022-40664 ..."
    /bin/cp -rf backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-*-1.7.1.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-*-1.10.0.jar
fi

echo "Reverting Percussion application updates that resolved issues..."

/bin/cp -rf backup/jetty/base/webapps/Rhythmyx/test/sql.jsp $INSTALL_DIR/jetty/base/webapps/Rhythmyx/test/sql.jsp

if [ -e "backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.1.jar" ]
then
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.1.jar
    /bin/cp -rf backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.1.jar  $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.1.jar

else
    /bin/cp -rf backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.jar  $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.jar
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.1.jar
fi

if [ -e "backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.1.jar" ]
then
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.1.jar
    /bin/cp -rf  backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.1.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.1.jar
else
    /bin/cp -rf  backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.jar
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.1.jar
fi

if [ -e "backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.1.jar" ]
then
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.1.jar
    /bin/cp -rf  backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.1.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.1.jar

else
    /bin/cp -rf  backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.jar
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.1.jar
fi

if [ -e "backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/extensions-workflow-8.1.2.1.jar" ]
then
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/extensions-workflow-8.1.2.1.jar
    /bin/cp -rf  backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/extensions-workflow-8.1.2.1.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/extensions-workflow-8.1.2.1.jar

else
    /bin/cp -rf  backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/extensions-workflow-8.1.2.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/extensions-workflow-8.1.2.jar
    rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/extensions-workflow-8.1.2.1.jar
fi

echo "--------------------------------------------"
echo "Uninstall of Percussion CMS patch completed."
echo "--------------------------------------------"
echo ""
echo "To re-install this patch use the provided install.sh script."
echo "The CMS service should be stopped and started after applying this patch."
echo "For example:"
echo ""
echo "service PercussionCMS stop"
echo "service PercussionCMS start"
echo ""