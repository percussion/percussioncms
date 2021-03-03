#!/bin/bash

function usage()
{
	echo Velocity Version Switcher
	echo "switchVelocityVersion.sh [-vc[2.1 or 2.0-patched or 1.6.2] | [-h]]"
	echo Please make sure the Percussion service is not running when running this command.
}

function remove162()
{
	echo Removing Velocity 1.6.2 libraries...
    rm -f ../../rxconfig/Server/velocity-precompile.properties
	rm -f ../../jetty/defaults/lib/perc/velocity-1.6.2.jar
	rm -f ../../jetty/defaults/lib/perc/velocity-tools-view-1.4.jar
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-1.6.2.jar	
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-tools-view-1.4.jar
    rm -f ../../AppServer/server/rx/lib/velocity-1.6.2.jar	
	rm -f ../../AppServer/server/rx/lib/velocity-tools-view-1.4.jar
    rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-1.6.2.jar
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-tools-view-1.4.jar
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/classes/com/percussion/services/assembly/impl/plugin/*.class
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/classes/com/percussion/services/assembly/impl/plugin/*.class
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/classes/com/percussion/services/utils/jexl/*.class
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/classes/com/percussion/services/utils/jexl/*.class
}

function remove21()
{
	echo Removing Velocity 2.1 libraries...
	
	rm -f ../../rxconfig/Server/velocity-precompile.properties
	rm -f ../../jetty/defaults/lib/perc/velocity-engine-core-2.1.jar
	rm -f ../../jetty/defaults/lib/perc/velocity-engine-scripting-2.1.jar
	rm -f ../../jetty/defaults/lib/perc/velocity-tools-generic-3.0.jar
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-engine-core-2.1.jar	
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-engine-scripting-2.1.jar
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-tools-generic-3.0.jar
    rm -f ../../AppServer/server/rx/lib/velocity-engine-core-2.1.jar	
	rm -f ../../AppServer/server/rx/lib/velocity-engine-scripting-2.1.jar
	rm -f ../../AppServer/server/rx/lib/velocity-tools-generic-3.0.jar
    rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-engine-core-2.1.jar
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-engine-scripting-2.1.jar
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-tools-generic-3.0.jar
}

function remove22()
{
	echo Removing Velocity 2.2 libraries...

	rm -f ../../rxconfig/Server/velocity-precompile.properties
	rm -f ../../rxconfig/Server/velocity.properties
	rm -f ../../jetty/defaults/lib/perc/velocity-engine-core-2.2.jar
	rm -f ../../jetty/defaults/lib/perc/velocity-engine-scripting-2.2.jar
	rm -f ../../jetty/defaults/lib/perc/velocity-tools-generic-3.0.jar
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-engine-core-2.2.jar
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-engine-scripting-2.2.jar
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-tools-generic-3.0.jar
    rm -f ../../AppServer/server/rx/lib/velocity-engine-core-2.2.jar
	rm -f ../../AppServer/server/rx/lib/velocity-engine-scripting-2.2.jar
	rm -f ../../AppServer/server/rx/lib/velocity-tools-generic-3.0.jar
    rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-engine-core-2.2.jar
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-engine-scripting-2.2.jar
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-tools-generic-3.0.jar
}


function remove20()
{
    echo Removing Velocity 2.0-patched libraries...
    rm -f ../../rxconfig/Server/velocity-precompile.properties
	rm -f ../../jetty/defaults/lib/perc/velocity-2.0-patched.jar
	rm -f ../../jetty/defaults/lib/perc/velocity-tools-generic-3.0-SNAPSHOT.jar
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-2.0-patched.jar	
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/velocity-tools-generic-3.0-SNAPSHOT.jar
    rm -f ../../AppServer/server/rx/lib/velocity-2.0-patched.jar	
	rm -f ../../AppServer/server/rx/lib/velocity-tools-generic-3.0-SNAPSHOT.jar
    rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-2.0-patched.jar	
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/velocity-tools-generic-3.0-SNAPSHOT.jar
	rm -f ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/classes/com/percussion/services/assembly/impl/plugin/*.class
	rm -f ../../jetty/base/webapps/Rhythmyx/WEB-INF/classes/com/percussion/services/assembly/impl/plugin/*.class
}

function update162()
{
	remove21
	remove20
	remove22

	echo Deploying Velocity 1.6.2 libraries...
	cp -f 1.6.2/*.jar ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/	
    cp -f 1.6.2/*.jar ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/
    cp -f 1.6.2/config/velocity-precompile.properties ../../rxconfig/Server/
    cp -f 1.6.2/config/tools.xml ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/velocity/
	cp -f 1.6.2/config/tools.xml ../../jetty/base/webapps/Rhythmyx/WEB-INF/config/velocity/
	cp -rf 1.6.2/classes/com ../../jetty/base/webapps/Rhythmyx/WEB-INF/classes/
    cp -rf 1.6.2/classes/com ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/classes/

}

function update22()
{
	remove162

	remove20

	remove21

	echo Deploying Velocity 2.2 libraries...
	  cp -f 2.2/*.jar ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/
    cp -f velocity-tools-generic-3.0.jar ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/
    cp -f 2.2/*.jar ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/
   cp -f velocity-tools-generic-3.0.jar ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/
    cp -f 2.2/config/tools.xml ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/velocity/
  	cp -f 2.2/config/tools.xml ../../jetty/base/webapps/Rhythmyx/WEB-INF/config/velocity/
  	cp -f 2.2/config/velocity.properties ../../rxconfig/Server/
  	cp -f 2.2/config/velocity-precompile.properties ../../rxconfig/Server/

}

function update21()
{
	remove162
	
	remove20

	remove22

	echo Deploying Velocity 2.1 libraries...
	cp -f 2.1-release/*.jar ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/	
    cp -f 2.1-release/*.jar ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/
    cp -f 2.1-release/config/velocity-precompile.properties ../../rxconfig/Server/
    cp -f 2.1-release/config/tools.xml ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/velocity/
	cp -f 2.1-release/config/tools.xml ../../jetty/base/webapps/Rhythmyx/WEB-INF/config/velocity/
}

function update20()
{
	remove162
	remove21
	remove22

	echo Deploying Velocity 2.0-patched libraries...
	cp -f 2.0-patched/*.jar ../../jetty/base/webapps/Rhythmyx/WEB-INF/lib/	
    cp -f 2.0-patched/*.jar ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/
    cp -rf 2.0-patched/classes/com ../../jetty/base/webapps/Rhythmyx/WEB-INF/classes/
    cp -rf 2.0-patched/classes/com ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/classes/
    cp -f 2.0-patched/config/velocity-precompile.properties ../../rxconfig/Server/
	cp -f 2.0-patched/config/tools.xml ../../AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/velocity/
	cp -f 2.0-patched/config/tools.xml ../../jetty/base/webapps/Rhythmyx/WEB-INF/config/velocity/
}


while [ "$1" != "" ]; do
    case $1 in
        -v | --version )        shift
                                version=$1
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        * )                     usage
                                exit 1
    esac
    shift
done

echo Selected Velocity Version is $version

if [ "$version" == "2.1" ]; then
        echo Switching to version 2.1...
        update21
elif [ "$version" == "2.0-patched" ]; then
        echo Switching to version 2.0-patched ...
        update20
elif [ "$version" == "1.6.2" ]; then
        echo Switching to version 1.6.2 ...
        update162
elif [ "$version" == "2.2" ]; then
        echo Switching to version 2.2 ...
        update22
else
    	echo "Velocity Version: $version is not currently supported by this tool"
fi
