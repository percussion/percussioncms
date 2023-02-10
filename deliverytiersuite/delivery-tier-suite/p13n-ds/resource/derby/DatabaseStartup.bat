@echo off

%JAVA_HOME%/bin/java.exe  -classpath Repository\lib\derbynet.jar -Dderby.system.home=Repository org.apache.derby.drda.NetworkServerControl start -noSecurityManager -h 0.0.0.0