#!/bin/bash
export dir=`dirname $0`
export pwd=/DevEnv/dev/system
export tpt=/DevEnv/dev/thirdpartytools
export INSTALL_DIR=~/Percussion
mkdir -p $INSTALL_DIR 

# Debug installer with the following
#ANT_OPTS="-Xmx512m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

export ANT_OPTS
export ANT_HOME=$pwd/Tools/Ant
cp -r $pwd/buildResources/executables/linux/* ${INSTALL_DIR}
tar xvzf ${INSTALL_DIR}/JRE.tar.gz -C ~
mkdir -p ${INSTALL_DIR}/rxconfig/Installer
export JAVA_HOME=~/JRE
export CLASSPATH

CLASSPATH=$pwd
CLASSPATH=${CLASSPATH}:$pwd/Tools/Hibernate/antlr-2.7.6.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/cglib/cglib-nodep-2.1_3.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-beanutils-1.7.0.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-betwixt-0.7RC2.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-codec-1.11.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-dbcp-1.2.1.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-io-1.1.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-jexl-1.1.1-patched.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-pool-1.2.jar
CLASSPATH=${CLASSPATH}:$pwd/jdbc/DB2/db2jcc_license_cujar
CLASSPATH=${CLASSPATH}:$pwd/jdbc/DB2/db2jccjar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Hibernate/dom4j-1.6.1.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Hibernate/ejb3-persistence.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Hibernate/hibernate3.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Hibernate/hibernate-annotations.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/JBoss/jboss.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/JSR-170/jcr-1.0.jar
CLASSPATH=${CLASSPATH}:$pwd/jdbc/jtds/jtds.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/log4j/log4j-1.2.14.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/MockRunner/mockrunner-jms.jar
#ojdbc14.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Quartz/quartz-all-2.0.1.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxantinstall.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxbusiness.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxclient.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxdeployer.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxinstall.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxmisctools.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxpublisher.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxserver.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxservices.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxtablefactory.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxtesting.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxutils.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxwebservices.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxworkflow.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/saxon/saxon.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/servlet/servlet.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Spring/spring.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/jdbc/Sprinta/Sprinta2000.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/AspectJ/aspectjrt.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/AspectJ/aspectjweaver.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Stax/stax-api-1.0.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Hibernate/hibernate-commons-annotations.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-httpclient-3.1.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-logging-1.1.1.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Ehcache/backport-util-concurrent.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Ehcache/ehcache-1.4.1.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Ehcache/jsr107cache-1.0.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Spring/spring-test.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/velocity/velocity-1.6.2.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-collections-3.2.2.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-lang-2.4.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Jericho-html/jericho-html.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/JBoss/jboss-j2ee.jar
CLASSPATH=${CLASSPATH}:$pwd/jdbc/derby/derbyclient.jar
CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxff.jar
CLASSPATH=${CLASSPATH}:$tpt/jaxb-2.1.9/lib/jaxb-api.jar
CLASSPATH=${CLASSPATH}:$tpt/guava-r07/guava-r07.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/jsch/jsch-0.1.48.jar
CLASSPATH=${CLASSPATH}:$pwd/jdbc/mysql/mysql-connector-java-5.1.12-bin.jar

#CLASSPATH=${CLASSPATH}:$pwd/Tools/mail/activation.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/regex/jakarta-oro-2.0.6.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/Ant/lib/ant.jar
#CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/Commons/commons-codec-1.11.jar
#CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxff.jar
#CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxtaxonomy.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/Asm/asm-2.2.2.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/Quartz/c3p0-0.9.1.1.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/Quartz/jta-1.1.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/Quartz/slf4j-api-1.6.1.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/Quartz/slf4j-log4j12-1.6.1.jar
#CLASSPATH=${CLASSPATH}:$pwd/build/dist/lib/rxutils.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/xstream/xstream.jar
#CLASSPATH=${CLASSPATH}:$tpt/apache-tika/1.3/tika-core-1.3.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/xerces/xml-apis.jar
#CLASSPATH=${CLASSPATH}:$pwd/Tools/xerces/xercesImpl.jar


${ANT_HOME}/bin/ant -lib $pwd/Tools/Ant/lib -f $pwd/installResources/install.xml -propertyfile $pwd/installResources/install.properties -Dinstall.dir=${INSTALL_DIR} | tee ${INSTALL_DIR}/rxconfig/Installer/ant.log

chmod 777 ${INSTALL_DIR}/bin/RhythmyxDaemon
chmod 777 ${INSTALL_DIR}/*.bin
chmod 777 ${INSTALL_DIR}/*.sh
chmod 777 ${INSTALL_DIR}/AppServer/bin/*.sh

cd ${INSTALL_DIR}/percussion-yajsw/bin
./runConsole.sh