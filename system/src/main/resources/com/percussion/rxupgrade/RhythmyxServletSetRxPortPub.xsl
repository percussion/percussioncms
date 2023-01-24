<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <!--
   This stylesheet is used to set the Rhythmyx server port in the "Rhythmyx"
   web application's (installed in the "AppServer/webapps" directory)
   configuration file "web.xml". This web application contains the Rhythmyx
   Servlet, and is installed along with the server if the user has selected
   to install the "Web services".
   If the user has selected to install the server, then he enters the value of
   the Rhythmyx server port in the Installshield panel whose bean id is
   "RxPortPanelBean".
   The contents of this stylesheet is resolved during installation prior to
   applying it to the "AppServer/webapps/Rhythmyx/WEB-INF/web.xml" file.
   During resolution "$W(installProperties.publisherHTTPPort)" changes to the server
   port entered by the user. The stylesheet is then applied to the Xml file,
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
	
	<xsl:template match="init-param[param-name='Port']/param-value" mode="copy">
       	<param-value>$W(installProperties.publisherHTTPPort)</param-value>
	</xsl:template>
		
</xsl:stylesheet>
