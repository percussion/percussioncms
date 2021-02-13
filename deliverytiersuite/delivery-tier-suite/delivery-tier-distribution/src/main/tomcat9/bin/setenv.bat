SET JAVA_OPTS=%JAVA_OPTS% -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager @JAVA_OPTS@
SET CLASSPATH=%CATALINA_BASE%/log4j2/lib/*;%CATALINA_BASE%/log4j2/conf;%CLASSPATH%



