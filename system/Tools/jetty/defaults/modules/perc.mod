#
# Percussion core module
#

[optional]
https
ext

[depend]
deploy
http
gzip
plus
jstl
jaas
fcgi
stats
resources
rewrite
servlets
annotations
cdi
perc-config
perc-ds
perc-logging
perc-mq
jvm

[xml]
etc/installation.properties
etc/perc-ssl.xml

[lib]
lib/perc/**.jar
lib/**.jar
lib/jdbc/**.jar
lib/extra/**.jar

[files]
lib/
lib/extra/
lib/perc/
lib/jdbc/
etc/
basehome:etc/login.conf|etc/login.conf
basehome:etc/installation.properties|etc/installation.properties

[ini]
jetty.deploy.monitoredPath=${jetty.base}/webapps
jetty.deploy.defaultsDescriptionPath=${jetty_perc_defaults}/perc-webdefault.xml
jetty_perc_defaults?=${jetty.base}/../defaults
jetty.server.stopTimeout=10000
jetty.server.dumpBeforeStart=true
jetty.webapp.addSystemClasses+=,org.xml.sax.,org.w3c.,org.apache.xmlcommons.Version,org.apache.html.,org.apache.wml.,org.apache.xerces.,org.apache.xml.
[exec]
-Djava.library.path=../../bin
-Djavax.xml.parsers.SAXParserFactory=com.percussion.xml.PSSaxParserFactoryImpl
-Dorg.apache.commons.logging.LogFactory=org.apache.commons.logging.impl.LogFactoryImpl
-Dderby.system.home=../../Repository
-Dderby.drda.startNetworkServer=true
-Djava.net.preferIPv4Stack=true
-Djava.net.preferIPv4Addresses=true


