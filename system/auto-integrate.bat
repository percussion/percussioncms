p4 set P4PORT=hera:1666
p4 set P4USER=python
p4 set P4CONFIG=p4.config

@echo off

:dev
echo P4CLIENT=indiana-cmlite-dev-main> p4.config
p4 integ -b streams-to-dev
p4 resolve -at
p4 submit -d "Integrating from streams to dev"

:thirdpartytools
echo P4CLIENT=indiana-thirdpartytools> p4.config
p4 integ -b streams-to-thirdpartytools
p4 resolve -at
p4 opened -c default | findstr /R /N "^" | find /C ":" > temp.txt
set /p var=<temp.txt
del temp.txt
if NOT "%var%" == "0" p4 submit -d "Integrating from streams to thirdpartytools"

:ivyrepo
echo P4CLIENT=indiana-ivyrepo> p4.config
p4 integ -b streams-to-ivyrepo
p4 resolve -at
p4 opened -c default | findstr /R /N "^" | find /C ":" > temp.txt
set /p var=<temp.txt
del temp.txt
if NOT "%var%" == "0" p4 submit -d "Integrating from streams to ivyrepo"

:deliverytier
echo P4CLIENT=indiana-deliverytier-main> p4.config
p4 integ -b streams-to-deliverytier
p4 resolve -at
p4 opened -c default | findstr /R /N "^" | find /C ":" > temp.txt
set /p var=<temp.txt
del temp.txt
if NOT "%var%" == "0" p4 submit -d "Integrating from streams to deliverytier-main"

:end
echo P4CLIENT=indiana-cmlite-dev-main-CI>p4.config
del p4.config
echo done!