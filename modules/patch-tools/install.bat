@echo off
@setlocal

echo ------------------------------------------------
echo Percussion CMS Patch Install Script  - 8.1.2.1
echo ------------------------------------------------
echo(



IF "%~1" == "" (
    echo Installation directory parameter is missing.
    GOTO helpFunction
)


rem Get install dir parameter
set INSTALL_DIR = %~pd1

if exist  %~pd1Version.properties ( echo PercussionCMS installation detected... ) else ( echo "Version.properties not found at %INSTALL_DIR%Version.properties. Please confirm that %INSTALL_DIR% contains a Percussion installation."; pause; exit 1)

if exist backup ( echo Backup directory detected... ) else ( mkdir backup; echo "Created patch backup folder to enable patch rollback with uninstall.bat")

echo Backing up existing files...
if not exist backup\sys_resources\webapps\secure\WEB-INF\lib ( 
    mkdir backup\sys_resources\webapps\secure\WEB-INF\lib
)
if not exist backup\jetty\defaults\lib\perc (
    mkdir backup\jetty\defaults\lib\perc
)
if not exist backup\rxconfig\SiteConfigs\$log$\lib\ (
    mkdir backup\rxconfig\SiteConfigs\$log$\lib\
)
if not exist backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\ ( 
    mkdir backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if not exist backup\jetty\base\webapps\Rhythmyx\test\ (
    mkdir backup\jetty\base\webapps\Rhythmyx\test\
)

if exist %~pd1sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.9.jar (
    echo "Updating Commons Text for CVE-2022-42889..."
    copy /V /Z /D /Y %~pd1sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.9.jar backup\sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.9.jar
    del /F /Q  %~pd1sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.9.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar %~pd1sys_resources\webapps\secure\WEB-INF\lib\
)

if exist %~pd1jetty\defaults\lib\perc\commons-text-1.9.jar (
    copy /V /Z /D /Y %~pd1jetty\defaults\lib\perc\commons-text-1.9.jar backup\jetty\defaults\lib\perc\commons-text-1.9.jar
    del /F /Q  %~pd1jetty\defaults\lib\perc\commons-text-1.9.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar %~pd1jetty\defaults\lib\perc\
)

if exist %~pd1rxconfig\SiteConfigs\$log$\lib\commons-text-1.9.jar (
    copy /V /Z /D /Y %~pd1rxconfig\SiteConfigs\$log$\lib\commons-text-1.9.jar backup\rxconfig\SiteConfigs\$log$\lib\commons-text-1.9.jar
    del /F /Q  %~pd1rxconfig\SiteConfigs\$log$\lib\commons-text-1.9.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar %~pd1rxconfig\SiteConfigs\$log$\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.9.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.9.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.9.jar
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.9.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-config-5.6.2.jar (
    echo "Updating Spring Security for CVE-2022-31692.."
    copy /V /Z /D /Y %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-config-5.6.2.jar backup\sys_resources\webapps\secure\WEB-INF\lib\
    del /F /Q  %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-config-5.6.2.jar
    copy /V /Z /D /Y sys_resources\webapps\secure\WEB-INF\lib\spring-security-config-5.6.9.jar %~pd1sys_resources\webapps\secure\WEB-INF\lib\
)

if exist %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-core-5.6.2.jar (
    copy /V /Z /D /Y %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-core-5.6.2.jar backup\sys_resources\webapps\secure\WEB-INF\lib\
    del /F /Q  %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-core-5.6.2.jar
    copy /V /Z /D /Y sys_resources\webapps\secure\WEB-INF\lib\spring-security-core-5.6.9.jar %~pd1sys_resources\webapps\secure\WEB-INF\lib\
)

if exist %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-crypto-5.6.2.jar (
    copy /V /Z /D /Y %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-crypto-5.6.2.jar backup\sys_resources\webapps\secure\WEB-INF\lib\
    del /F /Q  %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-crypto-5.6.2.jar
    copy /V /Z /D /Y sys_resources\webapps\secure\WEB-INF\lib\spring-security-crypto-5.6.9.jar %~pd1sys_resources\webapps\secure\WEB-INF\lib\
)

if exist %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-ldap-5.6.2.jar (
    copy /V /Z /D /Y %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-ldap-5.6.2.jar backup\sys_resources\webapps\secure\WEB-INF\lib\
    del /F /Q  %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-ldap-5.6.2.jar
    copy /V /Z /D /Y sys_resources\webapps\secure\WEB-INF\lib\spring-security-ldap-5.6.9.jar %~pd1sys_resources\webapps\secure\WEB-INF\lib\
)

if exist %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-web-5.6.2.jar (
    copy /V /Z /D /Y %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-web-5.6.2.jar backup\sys_resources\webapps\secure\WEB-INF\lib\
    del /F /Q  %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-web-5.6.2.jar
    copy /V /Z /D /Y sys_resources\webapps\secure\WEB-INF\lib\spring-security-web-5.6.9.jar %~pd1sys_resources\webapps\secure\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-cache-1.7.1.jar (
    echo "Updating Apache Shiro for CVE-2022-40664 ..."
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-cache-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-cache-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-cache-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-core-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-core-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-core-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-core-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-ogdl-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-ogdl-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-ogdl-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-ogdl-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-core-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-core-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-core-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-core-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-cipher-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-cipher-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-cipher-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-cipher-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-core-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-core-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-core-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-core-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-hash-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-hash-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-hash-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-hash-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-event-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-event-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-event-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-event-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-lang-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-lang-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-lang-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-lang-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-web-1.7.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-web-1.7.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-web-1.7.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-web-1.10.0.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

echo "Updating Percussion application to resolve issues..."
if exist %~pd1jetty\base\webapps\Rhythmyx\test\sql.jsp (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\test\sql.jsp backup\jetty\base\webapps\Rhythmyx\test\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\test\sql.jsp
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\test\sql.jsp %~pd1jetty\base\webapps\Rhythmyx\test\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

rem Check for previous patch update
if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

rem Check for previous patch update
if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

rem check for prior patch update
if exist %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar (
    copy /V /Z /D /Y %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar
    copy /V /Z /D /Y jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

echo --------------------------------------------
echo Percussion CMS patching completed.
echo --------------------------------------------
echo(
echo To uninstall this patch use the provided uninstall.bat script.
echo The CMS service should be stopped and started after applying this patch.
echo For example:
echo(
echo net stop PercussionCMS
echo net start PercussionCMS
echo(
echo The backup folder may be deleted after the patching is confirmed.  Note that this patch cannot be uninstalled once the backup folder is removed.
echo(
pause
exit 0

:helpFunction
   echo(
   echo "Usage: %0 <Path To PercussionCMS Installation> For example: %0 C:\Percussion\"
   echo(
   pause
