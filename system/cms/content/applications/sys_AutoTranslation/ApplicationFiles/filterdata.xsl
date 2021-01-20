<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:variable name="unusedctypelocalesdoc" select="document(//@unusedctypelocalesurl)"/>
	<xsl:variable name="ctypeworkflowdoc" select="document(//@contenttypeworkflowsurl)"/>
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="ContentType" mode="copy">
		<xsl:variable name="ctypeid" select="@contenttypeid"/>
		<xsl:if test="$ctypeid = $unusedctypelocalesdoc//ContentType/@contenttypeid">
			<xsl:copy>
				<xsl:apply-templates select="@*" mode="copy"/>
				<Locales>
				<xsl:for-each select="$unusedctypelocalesdoc//ContentType[@contenttypeid=$ctypeid]/Locale">
					<Locale>
						<xsl:attribute name="locale"><xsl:value-of select="@locale"/></xsl:attribute>
						<xsl:attribute name="localename"><xsl:value-of select="@localename"/></xsl:attribute>
					</Locale>
				</xsl:for-each>
				</Locales>
				<xsl:apply-templates mode="copy"/>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	<xsl:template match="Workflow" mode="copy">
		<xsl:variable name="ctypeid" select="../../@contenttypeid"/>
		<xsl:if test="@workflowid = $ctypeworkflowdoc//ContentType[@contenttypeid=$ctypeid]/workflows/workflow/@workflowid">
			<xsl:copy>
				<xsl:apply-templates select="@*" mode="copy"/>
				<xsl:apply-templates mode="copy"/>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
