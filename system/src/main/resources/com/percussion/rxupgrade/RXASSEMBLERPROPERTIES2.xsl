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
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="onCreateOnly">no</xsl:attribute>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="row" mode="copy">
		<xsl:variable name="newpropid" select="position()"/>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="column" mode="copy">
				<xsl:with-param name="newpropid" select="$newpropid"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="column" mode="copy">
		<xsl:param name="newpropid"/>
		<xsl:choose>
			<xsl:when test="@name='PROPERTYID'">
				<xsl:copy>
					<xsl:apply-templates select="@*" mode="copy"/>
					<xsl:value-of select="$newpropid"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*" mode="copy"/>
					<xsl:apply-templates mode="copy"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
