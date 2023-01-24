<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- 
	This stylesheet is used to set the Rhythmyx server port in the Content Connector 
	configuration file "rxconfig/ContentConnector/contentconnector.xml". If the
	user has selected to install Content Connector then the contentconnector.xml file
	is installed in the "rxconfig/ContentConnector" directory. If the user has also
 	selected to install the content connector without the server, then he enters the value of the 
	port in the Installshield panel whose bean id is "PublisherPortPanel". The contents
	of this stylesheet is then resolved. During resolution "$W(installProperties.publisherHTTPPort)"
	changes to the server port entered by the user. The stylesheet is then applied to
	contentconnector.xml, thus setting the Rhythmyx Server port appropritely.
	-->

	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXProperty[@name='Port']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<Value>$W(installProperties.publisherHTTPPort)</Value>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
