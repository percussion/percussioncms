@echo off
SETLOCAL

set OLDPATH=%path%
set OLDCLASSPATH=%classpath%

REM Get the disk from the first two chars in the %CD% environment var,
REM and get the directory from the remainder of %CD% after the first 3 chars.
REM See "help set" in a DOS shell window for information
set disk=%CD:~0,2%
set dir=%CD:~3%
set SANDBOX_ROOT=%disk%\%dir%
set SWT_DLL=swt-win32-2135.dll
set SWT_DLL_PATH=%SystemRoot%\system32\%SWT_DLL%
set LISTENER_ARG=-listener org.apache.tools.ant.listener.Log4jListener
set TMPBATCH=_tmp999.bat
set TMPPATCH=%SANDBOX_ROOT%\patchResources\config\patchFiles.xml
set TMPBACKUP=%SANDBOX_ROOT%\patchResources\config\backupFiles.xml
set TMPINSTALL=%SANDBOX_ROOT%\patchResources\config\installFiles.xml
set TMPUNINSTALL=%SANDBOX_ROOT%\patchResources\config\uninstallFiles.xml
set ANT_HOME=%SANDBOX_ROOT%\Tools\Ant
set MVS_BAT="%ProgramFiles%\Microsoft Visual Studio\VC98\Bin\VCVARS32.BAT"
set ANT_CALL=%SANDBOX_ROOT%\Tools\Ant\bin\ant -lib %SANDBOX_ROOT%\Tools\Ant\target\ %LISTENER_ARG%
set ANT_CALL_DEBUG=%SANDBOX_ROOT%\Tools\Ant\bin\ant -verbose -lib %SANDBOX_ROOT%\Tools\Ant\lib\target %LISTENER_ARG%
set ANT_CALL_SILENT=%SANDBOX_ROOT%\Tools\Ant\bin\ant -quiet -lib %SANDBOX_ROOT%\Tools\Ant\lib\target

REM Set installanywhere source path variables
set IA_PATH_RX_DIR=%SANDBOX_ROOT%

if exist %MVS_BAT% call %MVS_BAT% 
REM TODO: Fix classpath to use new ant Uber jar
set classpath=.
set classpath=%classpath%;%SANDBOX_ROOT%\Tools\Ant\perc-ant-8.0.2-SNAPSHOT.jar
set classpath=%classpath%;%SANDBOX_ROOT%\..\modules\ant-install\target\ant-install-8.0.2-SNAPSHOT.jar

REM Package specific actions
if "%1" == "PKGHELP" goto packageHelp
if "%1" == "PKGLIST" goto packagelist
if "%1" == "PKGBUILD" goto packagebuild
if "%1" == "PKGBUILDALL" goto packagebuildall
if "%1" == "PKGINSTALL" goto packageinstall
if "%1" == "PKGCHECKOUT" goto packagecheckout
if "%1" == "PKGUPDATE" goto packageupdate
if "%1" == "PKGADD" goto packageadd
goto packageHelp


:packageinstall
if "%2" == "" goto errorMissingPackageName
if "%3" == "" goto errorMissingPackageDir
if "%4" == "" goto errorMissingPackageServer
if "%5" == "" goto errorMissingPackagePort
cd installResources
%ANT_CALL% -Dfile=%2 -Dinstall.dir=%3 -Dserver=%4 -Dport=%5 -buildfile install.xml InstallPackage 
cd ..
goto End

:packagebuild
if "%2" == "" goto errorMissingPackageName
%ANT_CALL% -Dfile=%2 -buildfile packages.xml generatePackages
goto End

:packagebuildall 
%ANT_CALL% -buildfile packages.xml preparePackages
goto End

:packagecheckout
echo Checking out %2
if "%2" == "" goto errorMissingPackageName
set PACK=%2.ppkg
set DESCRIPTION=Checking out package %2
if "%3" == "" goto afterloop1
shift
shift
set DESCRIPTION=%1
:loop1
shift
if "%1" == "" goto afterloop1
set DESCRIPTION=%DESCRIPTION% %1
goto loop1
:afterloop1

%ANT_CALL% -DPACKAGENAME=%PACK% -DDESCRIPTION="%DESCRIPTION%" -buildfile packages.xml checkOutPackage
goto End

:packageupdate
if "%2" == "" goto errorMissingPackageName
if "%3" == "" goto errorMissingChangelist
echo Checking in %SANDBOX_ROOT%\Packages\%2
%ANT_CALL% -DPACKAGEPATH=%SANDBOX_ROOT%\Packages\%2 -Dchange.list.number=%3 -buildfile packages.xml updatePackage
goto End

:packageadd
echo Adding %SANDBOX_ROOT%\Packages\%2
if "%2" == "" goto errorMissingPackageName
set PACK=%2
set DESCRIPTION=Adding package %2
if "%3" == "" goto afterloop2
shift
shift
set DESCRIPTION=%1
:loop2
shift
if "%1" == "" goto afterloop2
set DESCRIPTION=%DESCRIPTION% %1
goto loop2
:afterloop2

%ANT_CALL% -DPACKAGEPATH=%SANDBOX_ROOT%\Packages\%PACK% -DDESCRIPTION="%DESCRIPTION%" -buildfile packages.xml addPackage 
goto End

:packagelist
echo ------------------------------------------------------------
%ANT_CALL_SILENT% -buildfile packages.xml packageList
echo ------------------------------------------------------------
goto End

:errorMissingChangelist
echo Error! The change list number must be specified.
goto End

:errorMissingPackageName
echo Error! The name of the package must be specified.
goto End

:errorMissingPackageDir
echo Error! The install directory of a server to use the package install must be specified.
goto End

:errorMissingPackagePort
echo Error! The port of the server to install the package must be specified.
goto End

:errorMissingPackageServer
echo Error! The server name of the server to install the package must be specified.
goto End

:errorMissingJavaSdk15
echo Error! Environment variable JAVA_SDK1.5_HOME must be set and point to a 1.5 SDK
goto End

:errorMissingTarget
echo Error! Target must be specified
goto End

:errorComingSoon
echo.
echo.
echo The selected command is coming soon! 
echo Please use the ant target.
echo.
echo.
goto End

:packageHelp
echo ------------------------------------------------------------
echo.
echo pkgbuild [option] [argumets]
echo Options:
echo.
echo   PKGHELP        Print this message
echo      eg:         pkgbuild PKGHELP
echo.
echo   PKGLIST        List all package names
echo      eg:         pkgbuild PKGLIST
echo.
echo   PKGBUILD       Builds package
echo      eg:         pkgbuild PKGBUILD packagename
echo.
echo   PKGBUILDALL    Builds all packages
echo      eg:         pkgbuild PKGBUILDALL
echo.
echo   PKGINSTALL     Install package on server
echo      eg:         pkgbuild PKGINSTALL packagename install_directory server port
echo                    (directory is used to find a package installer does not 
echo                     have to be directory of the server you are installing to)
echo.
echo   PKGCHECKOUT    Check out package
echo      eg:         pkgbuild PKGCKOUT packagename (optional - any additional text will 
echo                     be added to changelist description)
echo.
echo   PKGUPDATE      Update package source with your updated package
echo                  (package must be checked out first - see PKGCKOUT)
echo                  (Updated package must be placed in (source dir)\system\Packages)
echo      eg:         pkgbuild PKGUPDATE package.ppkg changelistnumber
echo.
echo   PKGADD         Check in new package
echo                  (New package must be placed in (source dir)\system\Packages)
echo      eg:         pkgbuild PKADD package.ppkg (optional - any additional text will 
echo                     be added to changelist description)
echo.
echo ------------------------------------------------------------
goto End

:End
set path=%OLDPATH%
set classpath=%OLDCLASSPATH%
set OLDPATH=
set OLDCLASSPATH=
set SANDBOX_ROOT=
set LISTENER_ARG=
set TMPBATCH=
set TMPPATCH=
set TMPBACKUP=
set TMPINSTALL=
set TMPUNINSTALL=
set dir=
set disk=
set SWT_DLL=
set SWT_DLL_PATH=
set CONSOLE_ARGS=
set MVS_BAT=
set ANT_CALL=
set DESCRIPTION=
set PACK=

ENDLOCAL