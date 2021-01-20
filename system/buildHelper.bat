@echo off
FOR /F "TOKENS=1* DELIMS= " %%A IN ('DATE/T') DO SET CDATE=%%B
FOR /F "TOKENS=1,2 eol=/ DELIMS=/ " %%A IN ('DATE/T') DO SET mm=%%B
FOR /F "TOKENS=1,2 DELIMS=/ eol=/" %%A IN ('echo %CDATE%') DO SET dd=%%B
FOR /F "TOKENS=2,3 DELIMS=/ " %%A IN ('echo %CDATE%') DO SET yyyy=%%B

SET configdir=U:\configuration
SET version=3.2
SET date1=%yyyy%%mm%
SET date2=%dd%

if "%1" == "QA" goto qaBuild
if "%1" == "DEV" goto intBuild
if "%1" == "RELEASE" goto relBuild
if "%1" == "copySetup" goto copySetup
goto theEnd

:qaBuild
set folder=%yyyy%%mm%Q%dd%
echo Build type %1
echo Build date %date1%
echo Build count %date2%
call rxbuild.bat CONSOLE QA %date1% %date2%
call rxbuild.bat MANUFACTURE
echo lastBuild=%version%\\%folder%> %configdir%/lastQaBuild.properties
goto copySetup

:intBuild
set folder=%yyyy%%mm%X%dd%
echo Build type %1
echo Build date %date1%
echo Build count %date2%
call C:\apache-ant-1.7.1\bin\ant -buildfile build.xml doSyncToHeadRevision
call rxbuild.bat CONSOLE INTERNAL %date1% %date2%
call rxbuild.bat MANUFACTURE
echo lastBuild=%version%\\%folder%> %configdir%/lastDevBuild.properties
goto copySetup

:relBuild
set folder=%yyyy%%mm%R01_%mm%%dd%%yyyy%
echo Build type %1
echo Build date %date1%
echo Build count 01
call rxbuild.bat CONSOLE RELEASE %date1% 01
call rxbuild.bat MANUFACTURE
echo lastBuild=%version%\\%folder%> %configdir%/lastRelBuild.properties
goto copySetup

:copySetup
xcopy release\setup U:\%version%\%folder% /i /s /y
goto theEnd

:theEnd
echo Done
