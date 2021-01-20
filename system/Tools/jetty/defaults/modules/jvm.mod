[description]
A noop module that creates an ini template useful for
setting JVM arguments (eg -Xmx )

[ini-template]
## JVM Configuration
## If JVM args are include in an ini file then --exec is needed
## to start a new JVM from start.jar with the extra args.
##
## If you wish to avoid an extra JVM running, place JVM args
## on the normal command line and do not use --exec
--exec

-server
-Xms512m
-Xmx1536m
-XX:+HeapDumpOnOutOfMemoryError

-Xrunjdwp:transport=dt_socket,address=8050,server=y,suspend=n

#-verbose:gc
#-XX:+PrintGCDateStamps
#-XX:+PrintGCTimeStamps
#-XX:+PrintGCDetails
#-XX:+PrintTenuringDistribution
#-XX:+PrintCommandLineFlags

