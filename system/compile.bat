REM
REM  Batch file for compiling all E2 components.
REM

@if NOT "%echo%" == "" @echo off
REM Creates a clean version of all Rhythmyx classes on the specified root directory.
REM The drive and CLASSPATH must be set correctly before calling this.
REM usage: compile [root], e.g. compile e2
REM
if "%1" == "" goto Usage

set BUILDVERSION=java com.percussion.buildversion.BuildVersion

REM set the default BUILDNUMBERS file if needed.
if not defined BUILDNUMBERS set BUILDNUMBERS=\%1\VersionControl\build.properties

echo --- compile.bat::e2server depends (this will be slow) -----------------------
REM The table factory depends on classes from these three packages.  So we build
REM them first, then the tablefactory, then the full e2server.
REM We expect to encounter errors at this point, so the output is redirected to nul
cd \%1
make clean
echo BUILDING: com.percussion.security.encryption
make com.percussion.security.encryption > nul
echo BUILDING: com.percussion.util
make com.percussion.util > nul
echo BUILDING: com.percussion.xml
make com.percussion.xml > nul

echo --- compile.bat::installer::Code.class --------------------------------------
REM These classes are needed to build the server. So we need to build this up front.
REM com.percussion.install.Code depends upon com.percussion.util.IPSBrandCodeConstants
cd \%1
if not exist classes md classes
if errorlevel 1 goto Error
javac -d classes src\com\percussion\util\IPSBrandCodeConstants.java
if errorlevel 1 goto Error
cd \%1\release
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
if not exist classes md classes
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\PSSystem.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\CodeException.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\RxInstallerProperties.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\IPSBrandCodeMap.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\PSBrandCodeUtil.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\PSBrandCodeElement.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\PSBrandCodeElementList.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\PSBrandCodeMapVersion.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\PSBrandCodeMap.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\PSBrandCodeData.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\Code.java
if errorlevel 1 goto Error
javac -d classes src\com\percussion\install\PSSystem.java
if errorlevel 1 goto Error
make jar RELEASE_DATE=%RELEASE_DATE%

echo --- compile.bat::buildversion -----------------------------------------------
cd \%1\tools\buildversion
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error

echo --- compile.bat::jarsplitter ------------------------------------------------
cd \%1\tools\jarsplitter
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error

echo --- compile.bat::psjniregistry -----------------------------------------------
cd \%1\psjniregistry
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::tools/tablefactory -----------------------------------------
cd \%1\tools\tablefactory
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::tools/help ------------------------------------------------------
cd \%1\tools\help
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::beans ------------------------------------------------------
cd \%1\beans
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error

echo --- compile.bat::misctools depends (this will be slow) -----------------------
REM The e2server depends on misctools.tools why we build this upfront.
REM We expect to encounter errors at this point, so the output is redirected to nul
cd \%1\misctools
make clean
echo BUILDING: com.percussion.tools
make com.percussion.tools > nul

echo --- compile.bat::HTTPClient ---------------------------------------------------
make HTTPClient
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::i18n depends (this will be slow) -----------------------
REM The e2server depends on i18n.tmxdom and i18n.rxlt why we build this upfront.
REM We expect to encounter errors at this point, so the output is redirected to nul
cd \%1\i18n
make clean
echo BUILDING: com.percussion.i18n.tmxdom
make com.percussion.i18n.tmxdom > nul
cd \%1
echo BUILDING: com.percussion.i18n
make com.percussion.i18n > nul
cd \%1\i18n
echo BUILDING: com.percussion.i18n.rxlt
make com.percussion.i18n.rxlt > nul

echo --- compile.bat::workflow depends (this will be slow) -----------------------
REM classes in com/percussion/relationship/effect package depend on workflow package.
REM We expect to encounter errors at this point, so the output is redirected to nul
cd \%1\workflow
make clean
echo BUILDING: com.percussion.workflow
make com.percussion.workflow > nul

echo --- compile.bat::e2server ---------------------------------------------------
cd \%1
if errorlevel 1 goto Error
REM did this above -- make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error

echo --- compile.bat::serveruicomp ---------------------------------------------------
cd \%1\ServerUIComponents
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::misctools ---------------------------------------------------
cd \%1\misctools
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::agenthandler ---------------------------------------------------
cd \%1\agenthandler
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::tools/simple -----------------------------------------------
cd \%1\tools\simple
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error

echo --- compile.bat::tools/spellcheck -----------------------------------------------
cd \%1\tools\spellcheck
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::release---------------------------------------------------
cd \%1\release
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::htmlconverter ----------------------------------------------
cd \%1\htmlconverter
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error

echo --- compile.bat::e2designer -------------------------------------------------
cd \%1\designer
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error

echo --- compile.bat::deployment client -------------------------------------------------
cd \%1\deploy
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::rxworkflow --------------------------------------------------
cd \%1\workflow
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::rxpublisher -------------------------------------------------
cd \%1\cms\publisher
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::rxdbpublisher -----------------------------------------
cd \%1\cms\dbpublisher
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile portal components ----------------------------------------------------
cd \%1\integration
if errorlevel 1 goto Error

set saveclasspath=%classpath%

set classpath=.;%BUILD_DRIVE%\%1\tools\xerces\xmlParserAPIs.jar
set classpath=%classpath%;%BUILD_DRIVE%\%1\tools\xerces\xercesImpl.jar

call \%1\Tools\ant\bin\ant compile
if errorlevel 1 goto Error

set classpath=%saveclasspath%

echo --- compile.bat::rxservlet ---------------------------------------------------
cd \%1\servlet
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::uploader ----------------------------------------------------
cd \%1\uploader
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::tools/deployment -----------------------------------------------
cd \%1\tools\deployment
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::i18n -----------------------------------------------
cd \%1\i18n
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::loader -----------------------------------------------
cd \%1\loader
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::ContentExplorer -----------------------------------------------
cd \%1\ContentExplorer
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

echo --- compile.bat::tools/InlineLinkConverter -------------------------------------
cd \%1\tools\InlineLinkConverter
if errorlevel 1 goto Error
make clean
if errorlevel 1 goto Error
make COMPALL=1
if errorlevel 1 goto Error
make jar
if errorlevel 1 goto Error

goto End

:Usage
echo Usage:
echo compile [root]
echo where [root] is the root directory to compile from
goto End

:Error
echo *** ERROR: Can not proceed!

:End

