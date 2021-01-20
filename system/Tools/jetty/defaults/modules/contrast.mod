## JVM Configuration
## If JVM args are include in an ini file then --exec is needed
## to start a new JVM from start.jar with the extra args.
##
## If you wish to avoid an extra JVM running, place JVM args
## on the normal command line and do not use --exec


[exec]
-Dorg.apache.activemq.SERIALIZABLE_PACKAGES="java.lang,java.util,org.apache.activemq,org.fusesource.hawtbuf,com.thoughtworks.xstream.mapper,com.percussion"
-javaagent:../../bin/contrast-rO0/contrast-rO0.jar
-DrO0.outfile=${jetty.base}/logs/serialize_security.log
-DrO0.lists=../../bin/contrast-rO0/perc-serialize-list.txt
-DrO0.whitelist=false
-DrO0.blacklist=true
-DrO0.reporting=true
