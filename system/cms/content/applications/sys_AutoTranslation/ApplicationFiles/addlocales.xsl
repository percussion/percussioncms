<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:variable name="localesdoc" select="document(//@localesurl)"/>
	<xsl:variable name="configdoc" select="document(//@configurl)"/>
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="ContentType" mode="copy">
		<xsl:variable name="ctypeid" select="@contenttypeid"/>
		<xsl:variable name="selectedctypeid" select="../@contenttypeid"/>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:choose>
				<xsl:when test="$configdoc//Config[@contenttypeid=$ctypeid]">
					<xsl:for-each select="$localesdoc//Locale[@locale != $configdoc//Config[@contenttypeid=$ctypeid]/@locale]">
						<Locale>
							<xsl:attribute name="locale"><xsl:value-of select="@locale"/></xsl:attribute>
							<xsl:attribute name="localename"><xsl:value-of select="@localename"/></xsl:attribute>
						</Locale>
					</xsl:for-each>
					<xsl:if test="../@contenttypeid!='' and $ctypeid = ../@contenttypeid">
						<Locale>
							<xsl:attribute name="locale"><xsl:value-of select="../@locale"/></xsl:attribute>
							<xsl:attribute name="localename"><xsl:value-of select="../@localename"/></xsl:attribute>
						</Locale>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="$localesdoc//Locale">
						<Locale>
							<xsl:attribute name="locale"><xsl:value-of select="@locale"/></xsl:attribute>
							<xsl:attribute name="localename"><xsl:value-of select="@localename"/></xsl:attribute>
						</Locale>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="ContentTypes" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:if test="not(@contenttypeid='' or @contenttypeid = ContentType/@contenttypeid)">
				<ContentType>
					<xsl:attribute name="contenttypeid"><xsl:value-of select="@contenttypeid"/></xsl:attribute>
					<xsl:attribute name="contenttypename"><xsl:value-of select="@contenttypename"/></xsl:attribute>
					<Locale>
						<xsl:attribute name="locale"><xsl:value-of select="@locale"/></xsl:attribute>
						<xsl:attribute name="localename"><xsl:value-of select="@localename"/></xsl:attribute>
					</Locale>
				</ContentType>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
