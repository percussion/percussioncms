<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates select="/*" mode="copy"/>
	</xsl:template>
	<xsl:template match="ActionList" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="*" mode="copy">
            <xsl:sort select="@label"/>
         </xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="Props" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:for-each select="*">
				<Prop>
					<xsl:attribute name="propid">0</xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>
					<xsl:value-of select="."/>
				</Prop>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="Params" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:for-each select="*">
				<Param>
					<xsl:attribute name="paramid">0</xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>
					<xsl:value-of select="."/>
				</Param>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
