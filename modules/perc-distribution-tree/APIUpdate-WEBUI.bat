@echo off
@echo Building Project
cd ..\..\
start /WAIT /B cmd /c mvn clean install -DskipTests=true -pl :CMLite-WebUI
cd modules\perc-distribution-tree\
@echo Updating WEBUI..
cd ..\..\
xcopy /D /E /F /H /R /Y WebUI\target\CMLite-WebUI-8.0.0-SNAPSHOT\*.* modules\perc-distribution-tree\target\classes\distribution\jetty\base\webapps\Rhythmyx\
@echo Starting Jetty...
cd modules\perc-distribution-tree\
start /WAIT cmd /C target\classes\distribution\jetty\StartJetty.bat