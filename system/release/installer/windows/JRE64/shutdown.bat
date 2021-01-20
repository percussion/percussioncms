@echo off
rem This script will invoke the JBoss shutdown script
set JAVA_HOME=..\..\JRE
set NOPAUSE=true
jboss_shutdown.bat -s localhost:{{NAMING_PORT}}