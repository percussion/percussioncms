#!/bin/sh

CURRENT_DIR=`pwd`
cd ..
BASE_INSTALL_DIR=`pwd`
cd $CURRENT_DIR

export PATH=$BASE_INSTALL_DIR/autotest/Ant/bin:$BASE_INSTALL_DIR/JRE/bin:$PATH
export CLASSPATH=
export JAVA_HOME=$BASE_INSTALL_DIR/JRE
export ANT_HOME=$BASE_INSTALL_DIR/autotest/Ant
ANT_CALL="ant --noconfig -lib $ANT_HOME/lib"

$ANT_CALL -Ddest.dir=".." deploy
