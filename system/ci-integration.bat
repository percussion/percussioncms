p4 set P4PORT=hera:1666
p4 set P4USER=python
p4 set P4CONFIG=p4.config

@echo off

:dev
echo P4CLIENT=indiana-cmlite-dev-integration> p4.config
p4 copy -b devStreams-to-Integration
p4 opened -c default | findstr /R /N "^" | find /C ":" > temp.txt
set /p var=<temp.txt
del temp.txt
if NOT "%var%" == "0" p4 submit -d "Copying from dev to Integration-stream"

echo P4CLIENT=indiana-cmlite-dev-main-CI>p4.config
del p4.config
echo done!!