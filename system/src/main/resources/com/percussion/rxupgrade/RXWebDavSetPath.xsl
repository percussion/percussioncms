<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <!--
   This stylesheet is used to set the absolute path of Webdav configuration 
   file ("RxWebdavConfig.xml")  in the "rxwebdav" web application's (installed 
   in the "AppServer/webapps" directory) configuration file "web.xml". 
   This web application contains the Rhythmyx WebDav Servlet, and is installed 
   along with the server if the user has selected  to install the "Web services".
   
   The contents of this stylesheet is resolved during installation prior to
   applying it to the "AppServer/webapps/rxwebdav/WEB-INF/web.xml" file.
   During resolution "$P(absoluteInstallLocation)" changes to the Rhythmyx
   installation directory. The stylesheet is then applied to the Xml file,
   thus setting the Rhythmyx Server port appropriately.
   
   -->

	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>

	<!-- copy any attribute or template -->
	<xsl:template match="@*|*|comment()" mode="copy">
		<xsl:copy>
       		<xsl:apply-templates select="@*" mode="copy"/>
         		<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="init-param[param-name='RxWebdavConfiguration']/param-value" mode="copy">
       	<param-value>$P(absoluteInstallLocation)/AppServer/webapps/rxwebdav/RxWebdavConfig.xml</param-value>
	</xsl:template>
		
</xsl:stylesheet>
