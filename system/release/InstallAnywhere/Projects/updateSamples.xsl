<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:param name="buildId"/>
	<xsl:param name="buildNumber"/>
	<xsl:param name="interfaceVersion"/>
	<xsl:param name="majorVersion"/>
	<xsl:param name="minorVersion"/>
	<xsl:param name="microVersion"/>
	<xsl:param name="versionString"/>
	<xsl:param name="serverName"/>
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
	<xsl:template match="PSXArchiveInfo" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="serverName"><xsl:value-of select="$serverName"/></xsl:attribute>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXFormatVersion" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="buildId"><xsl:value-of select="$buildId"/></xsl:attribute>
			<xsl:attribute name="buildNumber"><xsl:value-of select="$buildNumber"/></xsl:attribute>
			<xsl:attribute name="interfaceVersion"><xsl:value-of select="$interfaceVersion"/></xsl:attribute>
			<xsl:attribute name="majorVersion"><xsl:value-of select="$majorVersion"/></xsl:attribute>
			<xsl:attribute name="minorVersion"><xsl:value-of select="$minorVersion"/></xsl:attribute>
			<xsl:attribute name="microVersion"><xsl:value-of select="$microVersion"/></xsl:attribute>
			<xsl:attribute name="versionString"><xsl:value-of select="$versionString"/></xsl:attribute>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
