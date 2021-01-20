
REM run this program from $RxRoot\WebServices\6.0.0\sample\loader\java directory.  Where the $RxRoot is the Rhythmyx installation directory.

set CLASSPATH=lib/axis-1.4.1.jar;lib/axis-jaxrpc-1.4.1.jar;lib/axis-saaj-1.4.1.jar;lib/jaxrpc.jar;lib/commons-logging-1.0.4.jar;lib/commons-discovery-0.5.jar;lib/wsdl4j-1.6.2.jar;lib/mail.jar;lib/activation.jar;build/classes


..\..\..\..\..\JRE\bin\java.exe -classpath %CLASSPATH% com.percussion.webservices.sample.loader.PSLoader
