#
# Percussion Jetty Logging Module
#   Output Managed by Log4j2
#

[depend]
perc-config
resources

[tags]
logging

[provides]
logging

[lib]
lib/perc-logging/**.jar

[files]
logs/
basehome:modules/perc-logging


[ini]

[exec]
-Dorg.eclipse.jetty.util.log.class?=org.eclipse.jetty.util.log.Slf4jLog


