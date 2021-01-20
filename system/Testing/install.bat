@rem install the autotester relative to the current directory
setlocal
REM Get the disk from the first two chars in the %CD% environment var,
REM and get the directory from the remainder of %CD% after the first 3 chars.
REM See "help set" in a DOS shell window for info
set disk=%CD:~0,2%
set dir=%CD:~3%
set JAVA_HOME=%disk%\%dir%\..\JRE
set CLASSPATH=
set ANT_HOME=%disk%\%dir%\Ant
set path=%path%;%ANT_HOME%\bin;
set ANT_CALL=ant -lib %disk%\%dir%\Ant\lib

%ANT_CALL% -Ddest.dir=".." deploy
endlocal