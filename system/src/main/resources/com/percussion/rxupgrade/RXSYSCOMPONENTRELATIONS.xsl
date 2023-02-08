<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- main template -->
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
	<xsl:template match="table" mode="copy">
	<xsl:variable name="componentsext" select="document('RXSYSCOMPONENTRELATIONS.xml')/*"/>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="onCreateOnly">no</xsl:attribute>
			<xsl:apply-templates mode="copy"/>
			<xsl:variable name="columns" select="row/column[@name='COMPONENTID']"/>
			<xsl:variable name="columns1" select="row/column[@name='CHILDCOMPONENTID']"/>
			<xsl:apply-templates select="$componentsext/row[not(column[@name='COMPONENTID']=$columns and column[@name='CHILDCOMPONENTID']=$columns1)]" mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
