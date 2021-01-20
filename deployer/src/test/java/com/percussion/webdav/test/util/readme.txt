
How to setup and run the test:

[0] Make sure single sign on works for RhythmyxServlet

[1] create directories:
   
    $tomcat\webapps\testutil
    $tomcat\webapps\testutil\WEB-INF
    $tomcat\webapps\testutil\WEB-INF\classes\com\percussion\webdav\test\util

[2] copy web.xml to  $tomcat/webapps/testutil/WEB-INF

[3] copy RxWebdavConfig.xml to  $tomcat/webapps/testutil

[4] copy PSServletRequesterTest.class TO

    $tomcat\webapps\testutil\WEB-INF\classes\com\percussion\webdav\test\util

[5] Add the following line into $tomcat\conf\server.xml, right above "<!-- Rhythmyx Servlet Context -->"

    <Context path="/testutil" docBase="testutil" debug="0" crossContext="true"/>

[6] Start tomcat, invoke the servlet by: http://host:port/testutil

