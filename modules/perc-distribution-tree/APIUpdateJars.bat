@echo off
@echo Building Audit Log, CMLightMain, SiteManage, Rest, WebUI, TinyMCE
cd ..\..\
start /WAIT /B cmd /c mvn clean install -DskipTests=true -pl :CMLite-Main,:sitemanage,:rest,:CMLite-WebUI,:perc-tinymce
cd modules\perc-distribution-tree\
@echo Updating Site Manage Jar..
copy /Y ..\..\projects\sitemanage\target\sitemanage-8.0.0-SNAPSHOT.jar target\classes\distribution\jetty\base\webapps\Rhythmyx\WEB-INF\lib
@echo Updating TinyMCE Jar..
copy /Y ..\perc-tinymce\target\perc-tinymce-8.0.0-SNAPSHOT.jar target\classes\distribution\jetty\base\webapps\Rhythmyx\WEB-INF\lib
@echo Updating Audit Log Jar..
copy /Y ..\perc-auditlog\target\audit-log-8.0.0-SNAPSHOT.jar target\classes\distribution\jetty\base\webapps\Rhythmyx\WEB-INF\lib

@echo Updating Rest Jar..
copy /Y ..\..\rest\target\rest-8.0.0-SNAPSHOT.jar target\classes\distribution\jetty\base\webapps\Rhythmyx\WEB-INF\lib
copy /Y ..\..\system\target\CMLite-Main-8.0.0-SNAPSHOT.jar target\classes\distribution\jetty\base\webapps\Rhythmyx\WEB-INF\lib
@echo Updating WebUI
cd ..\..\
xcopy /D /E /F /H /R /Y WebUI\target\CMLite-WebUI-8.0.0-SNAPSHOT\*.* modules\perc-distribution-tree\target\classes\distribution\jetty\base\webapps\Rhythmyx\

@echo Starting Jetty...
cd modules\perc-distribution-tree\
start /WAIT cmd /C target\classes\distribution\jetty\StartJetty.bat