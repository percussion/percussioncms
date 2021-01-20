#!/bin/bash

function packageinstall()
{
if [ "$2" == "" ]; then
	errorMissingPackageName
	exit
fi

if [ "$3" == "" ]; then
	errorMissingPackageDir
	exit
fi

if [ "$4" == "" ]; then
	errorMissingPackageServer
	exit
fi

if [ "$5" == "" ]; then
 	errorMissingPackagePort
	exit
fi
cd installResources
$ANT_CALL -Dfile=$2 -Dinstall.dir=$3 -Dserver=$4 -Dport=$5 -buildfile install.xml InstallPackage
cd ..
}


function packagebuild()
{
if [ "$2" == "" ]; then
	errorMissingPackageName
	exit
fi

$ANT_CALL -Dfile=$2 -buildfile packages.xml generatePackages
exit
}

function packagebuildall()
{
$ANT_CALL -buildfile packages.xml preparePackages
}

function packagecheckout()
{
echo Checking out "$2"
if [ "$2" == "" ]; then
 	errorMissingPackageName
	exit
fi

PACK=$2.ppkg
DESCRIPTION="Checking out package $2"

$ANT_CALL -DPACKAGENAME=$PACK -DDESCRIPTION="$DESCRIPTION" -buildfile packages.xml checkOutPackage
exit
}

function packageupdate(){

if [ "$2" == "" ]; then
	errorMissingPackageName
	exit
fi

if [ "$3" == "" ]; then
	errorMissingChangelist
	exit
fi
echo Checking in $SANDBOX_ROOT/Packages/$2
$ANT_CALL -DPACKAGEPATH=$SANDBOX_ROOT/Packages/$2 -Dchange.list.number=$3 -buildfile packages.xml updatePackage
exit
}


function packageadd()
{
echo Adding $SANDBOX_ROOT/Packages/$2
if [ "$2" == "" ]; then
	errorMissingPackageName
	exit
fi

set PACK=$2
set DESCRIPTION="Adding package $2"

$ANT_CALL -DPACKAGEPATH=$SANDBOX_ROOT/Packages/$PACK -DDESCRIPTION="$DESCRIPTION" -buildfile packages.xml addPackage
exit
}

function packagelist()
{
echo ------------------------------------------------------------
$ANT_CALL_SILENT -buildfile packages.xml packageList
echo ------------------------------------------------------------
exit
}

function errorMissingChangelist()
{
echo Error! The change list number must be specified.
}

function errorMissingPackageName(){
echo Error! The name of the package must be specified.
}

function errorMissingPackageDir(){
	echo Error! The install directory of a server to use the package install must be specified.
}

function errorMissingPackagePort()
{
echo Error! The port of the server to install the package must be specified.
}

function errorMissingPackageServer(){
echo Error! The server name of the server to install the package must be specified.
}

function errorMissingJavaSdk15(){
echo Error! Environment variable JAVA_SDK1.5_HOME must be set and point to a 1.5 SDK
}

function errorMissingTarget(){
echo Error! Target must be specified
}

function errorComingSoon(){
echo
echo
echo The selected command is coming soon!
echo Please use the ant target.
echo
echo
}

function packagehelp () {

echo ------------------------------------------------------------
echo
echo "pkgbuild [option] [argumets]"
echo "Options:"
echo
echo   "PKGHELP        Print this message"
echo   "   eg:         pkgbuild PKGHELP"
echo
echo   "PKGLIST        List all package names"
echo   "   eg:         pkgbuild PKGLIST"
echo
echo  " PKGBUILD       Builds package"
echo   "   eg:         pkgbuild PKGBUILD packagename"
echo
echo   "PKGBUILDALL    Builds all packages"
echo   "   eg:         pkgbuild PKGBUILDALL"
echo
echo   "PKGINSTALL     Install package on server"
echo   "   eg:         pkgbuild PKGINSTALL packagename install_directory server port"
echo   "                 (directory is used to find a package installer does not "
echo   "                  have to be directory of the server you are installing to)"
echo
echo   "PKGCHECKOUT    Check out package"
echo   "   eg:         pkgbuild PKGCKOUT packagename (optional - any additional text will"
echo   "                  be added to changelist description)"
echo
echo   "PKGUPDATE      Update package source with your updated package"
echo   "               (package must be checked out first - see PKGCKOUT)"
echo   "               (Updated package must be placed in (source dir)/system/Packages)"
echo   "   eg:         pkgbuild PKGUPDATE package.ppkg changelistnumber"
echo
echo   "PKGADD         Check in new package"
echo   "               (New package must be placed in (source dir)\system\Packages)"
echo   "   eg:         pkgbuild PKADD package.ppkg (optional - any additional text will"
echo   "                  be added to changelist description)"
echo
echo ------------------------------------------------------------
exit
}

OLDPATH=$PATH
OLDCLASSPATH=$CLASSPATH
SANDBOX_ROOT=.
LISTENER_ARG="-listener org.apache.tools.ant.listener.Log4jListener"

ANT_HOME="Tools/Ant"
ANT_CALL="Tools/Ant/bin/ant -Djava.io.tmpdir=/tmp -lib Tools/Ant/target/ $LISTENER_ARG"
ANT_CALL_DEBUG="Tools/Ant/bin/ant -verbose -lib Tools/Ant/lib/target $LISTENER_ARG"
ANT_CALL_SILENT="Tools/Ant/bin/ant -quiet -lib Tools/Ant/lib/target"

# Set installanywhere source path variables
IA_PATH_RX_DIR=.
CLASSPATH=.
CLASSPATH="$CLASSPATH:Tools/Ant/perc-ant-8.0.0-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:../modules/ant-install/target/ant-install-8.0.0-SNAPSHOT.jar"

# Package specific actions
if [ "$1" == "PKGHELP" ]; then
packagehelp $1 $2 $3 $4 $5
fi

if [ "$1" == "PKGLIST" ]; then
   packagelist $1 $2 $3 $4 $5
fi

if [ "$1" == "PKGBUILD" ]; then
   packagebuild $1 $2 $3 $4 $5
fi

if [ "$1" == "PKGBUILDALL" ]; then
   packagebuildall $1 $2 $3 $4 $5
fi

if [ "$1" == "PKGINSTALL" ]; then
    packageinstall $1 $2 $3 $4 $5
fi

if [ "$1" == "PKGCHECKOUT" ]; then
     packagecheckout $1 $2 $3 $4 $5
fi

if [ "$1" == "PKGUPDATE" ]; then
     packageupdate $1 $2 $3 $4 $5
fi

if [ "$1" == "PKGADD" ]; then
     packageadd $1 $2 $3 $4 $5
else
     packagehelp $1 $2 $3 $4 $5
fi


