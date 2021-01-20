@if "%echo%" == "" @echo off
REM perform some sanity checks
echo Checking for files in master jar that aren't in any child jar...
java com.percussion.dev.jar.JarMiss master_obfu.jar e2.jar rxclient.jar rhythmyx.jar percbeans.jar htmlconverter.jar rxinstaller.jar

echo Checking for duplicate classes across all jars...
java com.percussion.dev.jar.JarLap e2.jar rxclient.jar rhythmyx.jar percbeans.jar htmlconverter.jar rxinstaller.jar
