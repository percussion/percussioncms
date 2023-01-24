<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:j2ee="http://java.sun.com/xml/ns/j2ee"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!--
   This stylesheet is used to set the Rhythmyx directory in the "Rhythmyx"
   web application's (installed in the "AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF" directory)
   configuration file "web.xml". This web application contains the Rhythmyx
   Servlet.  If the user has selected to install the server, then he enters the value of
   the Rhythmyx installation directory in the Installshield panel whose bean id is
   "RxNewInstallDestinationPanelBeanId".
   The contents of this stylesheet is resolved during installation prior to
   applying it to the "AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/web.xml" file.
   During resolution "$P(absoluteInstallLocation)" changes to the installation directory
   entered by the user. The stylesheet is then applied to the Xml file,
   thus setting the Rhythmyx directory appropriately.
   
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
	<xsl:template match="init-param[param-name='rxDir']/param-value | context-param[param-name='rxDir']/param-value | j2ee:param[param-name='rxDir']/param-value" mode="copy">
		<param-value>$P(absoluteInstallLocation)</param-value>
	</xsl:template>
</xsl:stylesheet>
