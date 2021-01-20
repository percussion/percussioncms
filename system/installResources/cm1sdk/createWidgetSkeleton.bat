@echo off
@setlocal
set disk=%CD:~0,2%
set dir=%CD:~3%
set root=%disk%\%dir%
set JAVA_HOME=%root%\JRE
set ANT_HOME=%root%\ant
set CLASSPATH=
set WIDGET_DISPLAY_NAME=

if (%1) == () goto error
if (%2) == () goto error
if not (%3) == () set WIDGET_DISPLAY_NAME=-Dwidget.display.name=%3
if "%1" == "help" goto usage
if "%1" == "?" goto usage

:run
%ANT_HOME%\bin\ant.bat -buildfile %root%\antfiles\createWidgetSkeleton.xml -Dinstall.target.dir=%1 -Dwidget.name=%2 %WIDGET_DISPLAY_NAME%
goto end

:error
@echo Invalid arguments!! Install directory path and widget name are required.
@echo:

:usage
@echo Usage:
@echo: 
@echo createWidget.bat install_directory_path widget_name [widget display name]
goto end

:end
@endlocal