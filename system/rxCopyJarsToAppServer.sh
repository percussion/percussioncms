#!/bin/bash

rxroot=$1

pwd=`pwd`
export path=$pwd/Tools/Ant/bin:./bin:${path}
set ANT_HOME=$pwd/Tools/Ant
set JAVA_HOME=${JAVA_SDK1_5_HOME}


CLASSPATH=$pwd
CLASSPATH=${CLASSPATH}:$pwd/Tools/saxon/saxon.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/mail/mail.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/mail/activation.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/regex/jakarta-oro-2.0.6.jar
CLASSPATH=${CLASSPATH}:$pwd/Tools/Ant/lib/ant.jar
CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar
export CLASSPATH

ant -lib $pwd/Tools/Ant/lib -f rxCopyJarsToAppServer.xml -DRXROOT=$rxroot
