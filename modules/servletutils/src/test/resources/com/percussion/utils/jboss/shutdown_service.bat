@echo off
rem This script will invoke the JBoss shutdown script
set JAVA_HOME=.\JRE
set NOPAUSE=true
taskkill /im phantomjs.exe /f
.\AppServer\bin\jboss_shutdown.bat -s localhost:1099