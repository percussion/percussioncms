@echo off
rem ---------------------------------------------------------------------------
rem Script for running Rhythmyx Table Definition Builder.
rem
rem ---------------------------------------------------------------------------

setlocal

set EXECJAVA=java.exe

if exist "..\..\JRE\bin\java.exe" (
set EXECJAVA="..\..\JRE\bin\java.exe"
goto run
)
goto error


:run
%EXECJAVA% -classpath ../server/rx/deploy/RxServices.war/WEB-INF/lib/rxtablefactory.jar;../server/rx/deploy/RxServices.war/WEB-INF/lib/rxclient.jar;../server/rx/deploy/RxServices.war/WEB-INF/lib/rxutils.jar;../lib/endorsed/xml-apis.jar;../lib/endorsed/xercesImpl.jar;../server/rx/lib/jtds.jar;../server/rx/lib/ojdbc14.jar;../server/rx/lib/db2jcc.jar;../server/rx/lib/mysql-connector-java-5.1.6-bin.jar;../server/rx/lib/db2jcc_license_cu.jar;../server/rx/lib/saxon.jar; com.percussion.tablefactory.tools.PSTDToolDialog

:error
echo "JRE does not exist in this location: ..\..\JRE\bin\java.exe"
:end
endlocal

