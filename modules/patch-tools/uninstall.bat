@echo off
@setlocal

echo ------------------------------------------------
echo Percussion CMS Patch Uninstall Script  - 8.1.2.1
echo ------------------------------------------------
echo(



IF "%~1" == "" (
    echo Installation directory parameter is missing.
    GOTO helpFunction
)


rem Get install dir parameter
set INSTALL_DIR = %~pd1

if exist  %~pd1Version.properties ( echo PercussionCMS installation detected... ) else ( echo "Version.properties not found at %INSTALL_DIR%Version.properties. Please confirm that %INSTALL_DIR% contains a Percussion installation."; pause; exit 1)

if exist backup ( echo Backup directory detected... ) else ( echo "Unable to rollback patch due to missing backup folder.";exit)

if exist backup\sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.9.jar (
    echo "Reverting Commons Text for CVE-2022-42889..."
    copy /V /Z /D /Y backup\sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.9.jar %~pd1sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.9.jar
    del /F /Q  %~pd1sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.10.0jar

    copy /V /Z /D /Y backup\jetty\defaults\lib\perc\commons-text-1.9.jar %~pd1jetty\defaults\lib\perc\
    del /F /Q  %~pd1jetty\defaults\lib\perc\commons-text-1.10.0.jar

    copy /V /Z /D /Y backup\rxconfig\SiteConfigs\$log$\lib\commons-text-1.9.ja %~pd1rxconfig\SiteConfigs\$log$\lib\
    del /F /Q  %~pd1rxconfig\SiteConfigs\$log$\lib\commons-text-1.10.0.jar

    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.9.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar
)

if exist backup\sys_resources\webapps\secure\WEB-INF\lib\spring-security-core-5.6.2.jar (
    echo "Reverting Spring Security for CVE-2022-31692.."
    copy /V /Z /D /Y  backup\sys_resources\webapps\secure\WEB-INF\lib\spring-security-*-5.6.2.jar %~pd1sys_resources\webapps\secure\WEB-INF\lib\
    del /F /Q  %~pd1sys_resources\webapps\secure\WEB-INF\lib\spring-security-*-5.6.9.jar
)

if exist backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-core-1.7.1.jar (
    echo "Reverting Apache Shiro for CVE-2022-40664 ..."
    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-*-1.7.1.jar %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-*-1.10.0.jar
)

echo "Reverting Percussion application updates to resolve issues..."

if exist backup\jetty\base\webapps\Rhythmyx\test\sql.jsp (
    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\test\sql.jsp  %~pd1jetty\base\webapps\Rhythmyx\test\
)

if exist backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar (
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar
    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
) else (
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar
    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.jar  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar (
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar
    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
) else (
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar
    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.jar  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)

if exist backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar (
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar
    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\

) else (
    del /F /Q  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.1.jar
    copy /V /Z /D /Y backup\jetty\base\webapps\Rhythmyx\WEB-INF\lib\rxutils-8.1.2.jar  %~pd1jetty\base\webapps\Rhythmyx\WEB-INF\lib\
)


echo --------------------------------------------
echo Percussion CMS patching completed.
echo --------------------------------------------
echo(
echo To re-install this patch use the provided install.bat script.
echo The CMS service should be stopped and started after applying this patch.
echo For example:
echo(
echo net stop PercussionCMS
echo net start PercussionCMS
echo(
echo The backup folder may be deleted after the patching is confirmed.  Note that this patch cannot be uninstalled once the backup folder is removed.
echo A full publish of all sites is recommended after patching
echo(
pause
exit 0

:helpFunction
   echo(
   echo "Usage: %0 <Path To PercussionCMS Installation> For example: %0 C:\Percussion\"
   echo(
   pause
