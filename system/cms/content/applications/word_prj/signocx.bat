SET CERTPATH=N:\certificates\verisign\

REM create cab file

%CERTPATH%cabarc.exe n rxwordocx.cab word_prj.ocx word_prj.inf

REM sign cab file with verisign's certificate

%CERTPATH%signcode.exe -t http://timestamp.verisign.com/scripts/timstamp.dll -spc %CERTPATH%mycredentials.spc -v %CERTPATH%myprivatekey.pvk rxwordocx.cab