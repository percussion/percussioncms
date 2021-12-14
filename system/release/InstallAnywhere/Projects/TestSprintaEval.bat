
@echo off

setlocal

if "%1" == "" goto Usage
if "%2" == "" goto Usage


java -classpath %1\%2\release\InstallShield\Projects\sprintaEval\TestSprintaEval.jar;%1\%2\Tools\BCEL\bcel-5.1.jar -DSPRINTA_EVAL_JAR_FILE=%1\%2\jdbc\Sprinta\eval\Sprinta2000.jar run

goto End

:Usage
echo Usage:
echo TestSprintaEval.bat [drive] [root]
echo where [drive] is the drive letter (with colon (:))
echo where [root] is the root directory
goto End


:End
endlocal
