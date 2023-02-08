
# This script will invoke the JBoss shutdown script
JAVA_HOME=./JRE
export JAVA_HOME
exec ./AppServer/bin/jboss_shutdown.sh -s localhost:1099
