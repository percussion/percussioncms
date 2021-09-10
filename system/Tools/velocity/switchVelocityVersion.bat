@echo off


echo Velocity Version Switcher
echo usage: switchVelocityVersion.bat [2.3, 2.2, 2.1, 2.0-patched OR 1.6.2]
echo Please make sure the Percussion service is not running when running this command.
pause

if "%1" == "2.3" goto update23

if "%1" == "2.2" goto update22

if "%1" == "2.1" goto update21

if "%1" == "2.0-patched" goto update20

if "%1" == "1.6.2" goto update162

echo Unsupported Version "%1" 
goto done

:update23:
echo Removing Velocity 2.0-patched libraries...
	del /Q ..\..\jetty\defaultslib\perc\velocity-2.0-patched.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0-SNAPSHOT.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-2.0-patched.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0-SNAPSHOT.jar

	echo Removing Velocity 1.6.2 libraries...
	del /Q ..\..\jetty\defaultslib\perc\velocity-1.6.2.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-view-1.4.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-1.6.2.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-view-1.4.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\assembly\impl\plugin\*.class
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\utils\jexl\*.class
	del /Q ..\..\rxconfig\Server\velocity-precompile.properties
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\tools.xml

	echo Removing Velocity 2.1 libraries...
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.1.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.1.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.1.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.1.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0.jar

	echo Removing Velocity 2.2 libraries...
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.2.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.2.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.2.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.2.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0.jar

	echo Deploying Velocity 2.3 libraries...
    copy /Y 2.3\*.jar ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    copy /Y 2.3\config\tools.xml ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\
    copy /Y 2.3\config\velocity-precompile.properties ..\..\rxconfig\Server\
    copy /Y 2.3\config\velocity.properties ..\..\rxconfig\Server\

	goto done:


:update22:
echo Removing Velocity 2.0-patched libraries...
	del /Q ..\..\jetty\defaultslib\perc\velocity-2.0-patched.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0-SNAPSHOT.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-2.0-patched.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0-SNAPSHOT.jar

	echo Removing Velocity 1.6.2 libraries...
	del /Q ..\..\jetty\defaultslib\perc\velocity-1.6.2.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-view-1.4.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-1.6.2.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-view-1.4.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\assembly\impl\plugin\*.class
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\utils\jexl\*.class
	del /Q ..\..\rxconfig\Server\velocity-precompile.properties
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\tools.xml

	echo Removing Velocity 2.1 libraries...
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.1.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.1.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.1.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.1.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0.jar

    echo Removing Velocity 2.3 libraries...
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.3.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.3.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.1.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.3.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.3.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.1.jar

	echo Deploying Velocity 2.2 libraries...
    copy /Y 2.2\*.jar ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    copy /Y 2.2\config\tools.xml ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\
    copy /Y 2.2\config\velocity-precompile.properties ..\..\rxconfig\Server\
    copy /Y 2.2\config\velocity.properties ..\..\rxconfig\Server\

	goto done:

:update21:

    echo Removing Velocity 2.3 libraries...
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.3.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.3.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.1.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.3.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.3.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.1.jar

    echo Removing Velocity 2.2 libraries...
    del /Q ..\..\jetty\defaultslib\perc\velocity-engine-core-2.2.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.2.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.2.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.2.jar

	echo Removing Velocity 2.0-patched libraries...
	del /Q ..\..\jetty\defaultslib\perc\velocity-2.0-patched.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0-SNAPSHOT.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-2.0-patched.jar	
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0-SNAPSHOT.jar

	echo Removing Velocity 1.6.2 libraries...
	del /Q ..\..\jetty\defaultslib\perc\velocity-1.6.2.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-view-1.4.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-1.6.2.jar	
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-view-1.4.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\assembly\impl\plugin\*.class
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\utils\jexl\*.class
	del /Q ..\..\rxconfig\Server\velocity-precompile.properties
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\tools.xml
	
	
	echo Deploying Velocity 2.1 libraries...
	copy /Y 2.1-release\*.jar ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\	
    copy /Y 2.1-release\config\tools.xml ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\
	copy /Y 2.1-release\config\velocity-precompile.properties ..\..\rxconfig\Server\
	
	goto done:

:update20:

    echo Removing Velocity 2.3 libraries...
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.3.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.3.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.1.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.3.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.3.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.1.jar

  echo Removing Velocity 2.2 libraries...
    del /Q ..\..\jetty\defaultslib\perc\velocity-engine-core-2.2.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.2.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.2.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.2.jar

	echo Removing Velocity 1.6.2 libraries...
	del /Q ..\..\jetty\defaultslib\perc\velocity-1.6.2.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-view-1.4.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-1.6.2.jar	
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-view-1.4.jar

	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\assembly\impl\plugin\*.class
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\utils\jexl\*.class
	del /Q ..\..\rxconfig\Server\velocity-precompile.properties
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\tools.xml
	
	echo Removing Velocity 2.1 libraries...
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.1.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.1.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.1.jar	
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.1.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0.jar

	echo Deploying Velocity 2.0-patched libraries...	
	copy /Y 2.0-patched\*.jar ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    xcopy /Y /R /S 2.0-patched\classes ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\
	copy /Y 2.0-patched\config\tools.xml ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\
	copy /Y 2.0-patched\config\velocity-precompile.properties ..\..\rxconfig\Server\
	
	goto done:

:update162:

    echo Removing Velocity 2.3 libraries...
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.3.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.3.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.1.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.3.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.3.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.1.jar

  echo Removing Velocity 2.2 libraries...
    del /Q ..\..\jetty\defaultslib\perc\velocity-engine-core-2.2.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.2.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.2.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.2.jar
    del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0.jar
    del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0.jar

	echo Removing Velocity 2.1 libraries...
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-core-2.1.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-engine-scripting-2.1.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-core-2.1.jar	
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-engine-scripting-2.1.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0.jar

    echo Removing Velocity 2.0-patched libraries...
	del /Q ..\..\jetty\defaultslib\perc\velocity-2.0-patched.jar
	del /Q ..\..\jetty\defaults\lib\perc\velocity-tools-generic-3.0-SNAPSHOT.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-2.0-patched.jar	
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\velocity-tools-generic-3.0-SNAPSHOT.jar
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\com\percussion\services\assembly\impl\plugin\*.class
	del /Q ..\..\rxconfig\Server\velocity-precompile.properties
	del /Q ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\tools.xml
	
	echo Deploying Velocity 1.6.2 libraries...	
	copy /Y 1.6.2\*.jar ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\lib\
    xcopy /Y /R /S 1.6.2\classes ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\classes\
	copy /Y 1.6.2\config\tools.xml ..\..\AppServer\server\rx\deploy\rxapp.ear\rxapp.war\WEB-INF\config\velocity\
	copy /Y 1.6.2\config\tools.xml ..\..\jetty\base\webapps\Rhythmyx\WEB-INF\config\velocity\
	copy /Y 1.6.2\config\velocity-precompile.properties ..\..\rxconfig\Server\

	goto done

:done
