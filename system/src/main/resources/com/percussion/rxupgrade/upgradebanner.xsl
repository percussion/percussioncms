<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<!-- Stylesheet to upgrade 5.7 or lower rx_bannerTemplate.xsl to work with 6.0 which uses JSPs for other components -->
	<xsl:template match="/">
		<xsl:apply-templates select="* | comment()" mode="copy"/>
	</xsl:template>
	<xsl:template match="* | comment()" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="* | comment()" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- enclose the table within a div tag if not already enclosed -->
	<xsl:template match="xsl:template[@name='bannerAndUserStatus']" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="div[@id='RhythmyxBanner']">
					<!-- if already exists just copy the children -->
					<xsl:apply-templates select="* | comment()" mode="copy"/>
				</xsl:when>
				<xsl:otherwise>
					<!-- otherwise, add div tag and then copy the children -->
                    <div id="RhythmyxBanner">
						<xsl:apply-templates select="* | comment()" mode="copy"/>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	<!-- Replace the old table cell with known attribute valuess to  the new one-->
	<xsl:template match="td[@width='205' and @class='outerboxcell' and @valign='middle']" mode="copy" priority="10">
		<td align="right" valign="bottom">
			<xsl:copy-of select="* | comment()"/>
		</td>
	</xsl:template>
</xsl:stylesheet>
