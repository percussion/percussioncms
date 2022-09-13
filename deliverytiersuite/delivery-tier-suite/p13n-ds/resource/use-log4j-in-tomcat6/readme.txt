Use log4j in Tomcat 6
_____________________________________________________________


| The following describes how to configure Tomcat 6 to use log4j instead of its default (non-log4j).
| After unzip ``soln-p13n-ds.zip``, the files mentioned below are in ``../resource/use-log4j-in-tomcat6``.

1. Copy ``log4j.properties`` into ``$CATALINA_HOME/lib``.
   
2. Place the ``log4j jar`` in ``$CATALINA_HOME/lib``. 

3. Replace ``$CATALINA_HOME/bin/tomcat-juli.jar`` with ``tomcat-juli.jar``. 

4. Place ``tomcat-juli-adapters.jar`` in ``$CATALINA_HOME/lib``. 

5. Delete ``$CATALINA_BASE/conf/logging.properties`` to prevent java.util.logging generating zero length log files. 

6. Start Tomcat 


Refer to http://tomcat.apache.org/tomcat-6.0-doc/logging.html#log4j for other related information.
