<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="PSXTableLocator ">
		<xsl:copy>
			<xsl:apply-templates select="@*|PSXBackEndCredential|Origin|Database"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="PSXBackEndTable ">
		<xsl:copy>
			<xsl:apply-templates select="@*|alias|table|origin"/>
			<datasource/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="PSXBackEndCredential ">
		<xsl:copy>
			<xsl:apply-templates select="@*|alias|table|origin"/>
			<datasource/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="origin">
		<!-- remove origin -->
	</xsl:template>
	<xsl:template match="Origin">
		<!-- remove Origin -->
	</xsl:template>
	<xsl:template match="Database">
		<!-- remove Database -->
	</xsl:template>
	
	<xsl:template match="*|@*|comment()|processing-instruction()|text()">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
