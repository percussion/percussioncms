<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xml:output method="xml" encoding="UTF-8"/>
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
	<!-- set yes to index_on_startup property -->
	<xsl:template match="PSXContainerLocator/PSXTableSet" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
			<xsl:choose>
				<xsl:when test="PSXTableRef[@name='CONTENTSTATUS']">
				</xsl:when>
				<xsl:otherwise>
					<PSXTableRef alias="CONTENTSTATUS" name="CONTENTSTATUS"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
    </xsl:template>
</xsl:stylesheet>
