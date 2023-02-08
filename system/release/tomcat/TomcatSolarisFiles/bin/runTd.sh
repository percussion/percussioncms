#! /bin/sh

EXECJAVA=java

if [ -r "../../JRE/bin/java" ]; then
  EXECJAVA="../../JRE/bin/java"
else
  echo "JRE does not exist in this location: ..\..\JRE\bin\java.exe"
  exit -1
fi


$EXECJAVA -classpath ../server/rx/deploy/RxServices.war/WEB-INF/lib/rxtablefactory.jar:../server/rx/deploy/RxServices.war/WEB-INF/lib/rxclient.jar:../server/rx/deploy/RxServices.war/WEB-INF/lib/rxutils.jar:../lib/endorsed/xml-apis.jar:../lib/endorsed/xercesImpl.jar:../server/rx/lib/jtds.jar:../server/rx/lib/ojdbc14.jar:../server/rx/lib/db2jcc.jar:../server/rx/lib/db2jcc_license_cu.jar:../server/rx/lib/mysql-connector-java-5.1.6-bin.jar:../server/rx/lib/saxon.jar: com.percussion.tablefactory.tools.PSTDToolDialog




