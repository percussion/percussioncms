@echo off
@echo Building Project
cd ..\..\
start /WAIT /B cmd /c mvn clean install -DskipTests=true -pl :perc-tinymce
cd modules\perc-distribution-tree\
@echo Updating TinyMCE..
copy /Y ..\perc-tinymce\target\perc-tinymce-8.0.0-SNAPSHOT.jar target\classes\distribution\jetty\base\webapps\Rhythmyx\WEB-INF\lib
@echo Starting Jetty...
start /WAIT cmd /C target\classes\distribution\jetty\StartJetty.bat