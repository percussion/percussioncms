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
	<xsl:variable name="componentsext" select="document('RXSYSCOMPONENT.xml')/*"/>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="onCreateOnly">no</xsl:attribute>
			<xsl:apply-templates mode="copy"/>
			<xsl:variable name="columns" select="row/column[@name='COMPONENTID']"/>
			<xsl:apply-templates select="$componentsext/row[not(column[@name='COMPONENTID']=$columns)]" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="row[column[@name='COMPONENTID']='25' and column[@name='NAME']='ca_purge_bystatus' and column[@name='URL']='../sys_ca/camain.html?sys_sortparam=title']" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="column" mode="columncopy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="column" mode="columncopy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:choose>
				<xsl:when test="@name='URL'">
					<xsl:text>../sys_caContentSearch/search.html?sys_sortparam=title</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates mode="copy"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
