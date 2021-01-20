@echo off
mode con codepage select=1252

REM Set classpath to run the JUnit from command line
set CLASSPATH=..\AppServer\server\rx\deploy\rxapp.ear\rxapp.war\WEB-INF\classes\;..\AppServer\server\rx\deploy\rxapp.ear\rxapp.war\WEB-INF\lib\*

java org.junit.runner.JUnitCore com.percussion.sitemanage.service.PSSiteSuckerTest

REM Create variable with current date
set mydate=%date:~4,2%%date:~7,2%%date:~10,4%

REM Check if directory exists, otherwise create it
if not exist Reports mkdir Reports

REM Make copy of the CSV file and rename it setting the current date
copy websiteurls.csv Reports\websiteurls-%mydate%.csv

REM Make copy of the generated HTML report and rename it setting the current date
copy SiteSuckerReport.html Reports\report-%mydate%.html


