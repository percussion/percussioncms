#!/bin/bash

echo "------------------------------------------------"
echo "Percussion CMS Patch Install Script  - 8.1.2.1"
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
	mkdir backup
	echo "Created patch backup folder"
fi

echo "Backing up existing files..."
mkdir -p backup/sys_resources/webapps/secure/WEB-INF/lib
mkdir -p backup/jetty/defaults/lib/perc
mkdir -p backup/rxconfig/SiteConfigs/\$log\$/lib/
mkdir -p backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/
mkdir -p backup/jetty/base/webapps/Rhythmyx/test/

echo "Updating Commons Text for CVE-2022-42889..."
/bin/cp -rf $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/commons-text-1.9.jar backup/sys_resources/webapps/secure/WEB-INF/lib/commons-text-1.9.jar 
rm -f $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/commons-text-1.9.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.10.0.jar $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/defaults/lib/perc/commons-text-1.9.jar backup/jetty/defaults/lib/perc/commons-text-1.9.jar 
rm -f $INSTALL_DIR/jetty/defaults/lib/perc/commons-text-1.9.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.10.0.jar $INSTALL_DIR/jetty/defaults/lib/perc/

/bin/cp -rf $INSTALL_DIR/rxconfig/SiteConfigs/\$log\$/lib/commons-text-1.9.jar backup/rxconfig/SiteConfigs/\$log\$/lib/commons-text-1.9.jar 
rm -f $INSTALL_DIR/rxconfig/SiteConfigs/\$log\$/lib/commons-text-1.9.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.10.0.jar $INSTALL_DIR/rxconfig/SiteConfigs/\$log\$/lib/


/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.9.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.9.jar 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.9.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/commons-text-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/


echo "Updating Spring Security for CVE-2022-31692.."
/bin/cp -rf $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-config-5.6.2.jar backup/sys_resources/webapps/secure/WEB-INF/lib/ 
rm -f $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-config-5.6.2.jar
/bin/cp -rf sys_resources/webapps/secure/WEB-INF/lib/spring-security-config-5.6.9.jar $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-core-5.6.2.jar backup/sys_resources/webapps/secure/WEB-INF/lib/ 
rm -f $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-core-5.6.2.jar
/bin/cp -rf sys_resources/webapps/secure/WEB-INF/lib/spring-security-core-5.6.9.jar $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-crypto-5.6.2.jar backup/sys_resources/webapps/secure/WEB-INF/lib/ 
rm -f $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-crypto-5.6.2.jar
/bin/cp -rf sys_resources/webapps/secure/WEB-INF/lib/spring-security-crypto-5.6.9.jar $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-ldap-5.6.2.jar backup/sys_resources/webapps/secure/WEB-INF/lib/ 
rm -f $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-ldap-5.6.2.jar
/bin/cp -rf sys_resources/webapps/secure/WEB-INF/lib/spring-security-ldap-5.6.9.jar $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-web-5.6.2.jar backup/sys_resources/webapps/secure/WEB-INF/lib/ 
rm -f $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/spring-security-web-5.6.2.jar
/bin/cp -rf sys_resources/webapps/secure/WEB-INF/lib/spring-security-web-5.6.9.jar $INSTALL_DIR/sys_resources/webapps/secure/WEB-INF/lib/

echo "Updating Apache Shiro for CVE-2022-40664 ..."
/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-cache-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-cache-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-cache-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-config-core-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-config-core-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-config-core-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-config-ogdl-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-config-ogdl-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-config-ogdl-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-core-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-core-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-core-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-cipher-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-cipher-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-cipher-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-core-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-core-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-core-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-hash-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-hash-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-crypto-hash-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-event-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-event-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-event-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-lang-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-lang-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-lang-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-web-1.7.1.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-web-1.7.1.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/shiro-web-1.10.0.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

echo "Updating Percussion application to resolve issues: #890, #762, #851..."

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/test/sql.jsp backup/jetty/base/webapps/Rhythmyx/test/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/test/sql.jsp
/bin/cp -rf jetty/base/webapps/Rhythmyx/test/sql.jsp $INSTALL_DIR/jetty/base/webapps/Rhythmyx/test/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-8.1.2.1.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/ 
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/sitemanage-8.1.2.1.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

/bin/cp -rf $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.jar backup/jetty/base/webapps/Rhythmyx/WEB-INF/lib/
rm -f $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.jar
/bin/cp -rf jetty/base/webapps/Rhythmyx/WEB-INF/lib/rxutils-8.1.2.1.jar $INSTALL_DIR/jetty/base/webapps/Rhythmyx/WEB-INF/lib/

echo "--------------------------------------------"
echo "Percussion CMS patching completed."
echo "--------------------------------------------"
echo ""
echo "To uninstall this patch use the provided uninstall.sh script."
echo "The CMS service should be stopped and started after applying this patch."
echo "For example:"
echo ""
echo "service PercussionCMS stop"
echo "service PercussionCMS start"
echo ""
echo "The backup folder may be deleted after the patching is confirmed.  Note that this patch cannot be uninstalled once the backup folder is removed."
echo ""
