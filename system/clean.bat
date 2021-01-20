@echo off
if "%1" == "" goto Usage
if "%2" == "" goto Usage
setlocal
set DRIVE=%1
set ROOT=%2


rm -rf %DRIVE%\%ROOT%\agenthandler\classes
rm -rf %DRIVE%\%ROOT%\designer\classes
rm -rf %DRIVE%\%ROOT%\classes
rm -rf %DRIVE%\%ROOT%\beans\classes
rm -rf %DRIVE%\%ROOT%\ServerUIComponents\classes
rm -rf %DRIVE%\%ROOT%\ContentExplorer\classes
rm -rf %DRIVE%\%ROOT%\htmlconverter\classes
rm -rf %DRIVE%\%ROOT%\cms\publisher\classes
rm -rf %DRIVE%\%ROOT%\tools\buildversion\classes
rm -rf %DRIVE%\%ROOT%\psjniregistry\classes
rm -rf %DRIVE%\%ROOT%\tools\jarsplitter\classes
rm -rf %DRIVE%\%ROOT%\tools\simple\classes
rm -rf %DRIVE%\%ROOT%\misctools\classes
rm -rf %DRIVE%\%ROOT%\servlet\classes
rm -rf %DRIVE%\%ROOT%\workflow\classes
rm -rf %DRIVE%\%ROOT%\release\classes
rm -rf %DRIVE%\%ROOT%\tools\tablefactory\classes
rm -rf %DRIVE%\%ROOT%\cms\dbpublisher\classes
rm -rf %DRIVE%\%ROOT%\i18n\classes
rm -rf %DRIVE%\%ROOT%\tools\help\classes
rm -rf %DRIVE%\%ROOT%\loader\classes
rm -rf %DRIVE%\%ROOT%\integration\classes



MKDIR %DRIVE%\%ROOT%\agenthandler\classes
MKDIR %DRIVE%\%ROOT%\designer\classes
MKDIR %DRIVE%\%ROOT%\classes
MKDIR %DRIVE%\%ROOT%\beans\classes
MKDIR %DRIVE%\%ROOT%\ServerUIComponents\classes
MKDIR %DRIVE%\%ROOT%\ContentExplorer\classes
MKDIR %DRIVE%\%ROOT%\htmlconverter\classes
MKDIR %DRIVE%\%ROOT%\cms\publisher\classes
MKDIR %DRIVE%\%ROOT%\tools\buildversion\classes
MKDIR %DRIVE%\%ROOT%\psjniregistry\classes
MKDIR %DRIVE%\%ROOT%\tools\jarsplitter\classes
MKDIR %DRIVE%\%ROOT%\tools\simple\classes
MKDIR %DRIVE%\%ROOT%\misctools\classes
MKDIR %DRIVE%\%ROOT%\servlet\classes
MKDIR %DRIVE%\%ROOT%\workflow\classes
MKDIR %DRIVE%\%ROOT%\release\classes
MKDIR %DRIVE%\%ROOT%\tools\tablefactory\classes
MKDIR %DRIVE%\%ROOT%\cms\dbpublisher\classes
MKDIR %DRIVE%\%ROOT%\i18n\classes
MKDIR %DRIVE%\%ROOT%\tools\help\classes
MKDIR %DRIVE%\%ROOT%\loader\classes
MKDIR %DRIVE%\%ROOT%\integration\classes

goto End

:Usage
echo Usage:
echo setClassPath [drive] [root]
echo where [drive] is the drive letter (with colon (:))
echo where [root] is the root directory
goto End


:End
