<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
<!--	This xsl file is applied on the ResultDocument of sys_casSupport/casSupport resources to add slot name and link urls.
	The slot id is a row value of PSX_RELATIONSHIPPROPERTIES table with a data type of nvarchar and when mapped to RXSLOTTYPE tables 	slotid column with a data type of integer is giving errors on DB2. To add the slot name and linkurl this xsl will be applied on the ResultDocument.
	It gets the slots info and variants info from sys_rcSupport/slotsinfo and sys_rcSupport/variantsifo resources respectively.
-->
	<xsl:variable name="slotinfo" select="document(//slotsinfo)"/>
	<xsl:variable name="variantinfo" select="document(//variantsinfo)//*"/>
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
	<xsl:template match="linkurl" mode="copy">
		<xsl:variable name="slotid" select="@slotid"/>
		<xsl:variable name="variantid" select="@variantid"/>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="slotname"><xsl:value-of select="$slotinfo//item[@id=$slotid]/@name"/></xsl:attribute>
			<xsl:value-of select="$variantinfo/item[@id=$variantid]/@assemblyurlint"/>
			<xsl:text>&amp;</xsl:text>
			<xsl:value-of select="substring-after(.,'?')"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
