<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<!-- main template -->
	<xsl:variable name="locales" select="document(//localeurl)"/>
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
	<xsl:template match="LoginData" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<Roles>
				<xsl:apply-templates select="../Roles/*" mode="copy"/>
			</Roles>
			<Locales>
				<xsl:for-each select="$locales//PSXEntry">
					<Locale>
						<xsl:attribute name="code"><xsl:value-of select="Value"/></xsl:attribute>
						<xsl:value-of select="PSXDisplayText"/>
					</Locale>
				</xsl:for-each>
			</Locales>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="localeurl" mode="copy"/>
	<xsl:template match="Roles" mode="copy"/>
</xsl:stylesheet>
