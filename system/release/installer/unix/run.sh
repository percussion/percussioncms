
# This script will invoke the JBoss startup script
JAVA_HOME=./JRE
export JAVA_HOME
exec ./AppServer/bin/jboss_run.sh --configuration=rx
