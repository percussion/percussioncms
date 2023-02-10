
# This script will invoke the JBoss shutdown script
JAVA_HOME=./JRE
export JAVA_HOME
./AppServer/bin/jboss_shutdown.sh -s localhost:{{NAMING_PORT}} &
sleep 60
#Shutdown Production Tomcat only if Deployment directory exists
if [ -d Deployment ] ; then
  if [ -f TomcatShutdown.sh ] ; then
  	echo "Issuing Production DTS shutdown command"
    ./TomcatShutdown.sh &
  fi
else
  echo "Production DTS not detected in installation directory...Skipping shutdown command."
fi
#Shutdown Staging Tomcat
if [ -d Staging ] ; then
   echo "Issuing Staging DTS shutdown command"
   cd Staging
   ./TomcatShutdown.sh &
   cd ..
else
   echo "Staging DTS not detected in installation directory...Skipping shutdown command."
fi
sleep 60
if [ -f  ./DatabaseShutdown.sh ] ; then
   ./DatabaseShutdown.sh demo
fi
