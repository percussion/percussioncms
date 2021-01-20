@echo off

set OLDPATH=%path%
set OLDCLASSPATH=%classpath%

if %JAVA_SDK1.4_HOME == "" goto errorMissingJavaSdk14

REM Get the disk from the first two chars in the %CD% environment var,
REM and get the directory from the remainder of %CD% after the first 3 chars.
REM See "help set" in a DOS shell window for info
set SANDBOX_ROOT=.\..\..
set path=%SANDBOX_ROOT%\Tools\Ant\bin;%JAVA_SDK1.4_HOME%\bin;%MKS_DIR%\mksnt
set ANT_HOME=%SANDBOX_ROOT%\Tools\Ant
set JAVA_HOME=%JAVA_SDK1.4_HOME%

set classpath=.
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\xerces\xmlParserAPIs.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\xerces\xercesImpl.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\saxon\saxon.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\mail\mail.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\mail\activation.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\Ant\lib\ant.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\Ant\lib\optional.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\Ant\lib\cpptasks.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\regex\jakarta-oro-2.0.6.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\Ant\lib\p4.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\Ant\lib\psantextensions.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\SWT\swt.jar
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\yguard-1.2\lib\yguard.jar
set classpath=%classpath%;%JAVA_SDK_HOME%\lib\tools.jar
set classpath=%classpath%;Tools\junit\junit-4.11.jar

%ANT_HOME%\bin\ant
goto End

:End
set path=%OLDPATH%
set classpath=%OLDCLASSPATH%
set OLDPATH=
set OLDCLASSPATH=
set SANDBOX_ROOT=
