<?xml version="1.0" encoding="UTF-8"?>


<!--
This style is used to sort the TMX resource document just before saving to
the disk. Idea is to make it easier for sombody to locate a required translation
key.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- Copy every other element/attribute as it is -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- Sort the supported languages by their names -->
	<xsl:template match="header" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="prop" mode="copy">
				<xsl:sort select="."/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<!-- Sort the translation units first by section name and then by translation
unit key -->
	<xsl:template match="body" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="tu" mode="copy">
				<xsl:sort select="prop[@type='sectionname']"/>
				<xsl:sort select="@tuid"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<!-- Sort the translation unit variants by its language string -->
	<xsl:template match="tu" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="prop" mode="copy"/>
			<xsl:apply-templates select="note" mode="copy"/>
			<xsl:apply-templates select="tuv" mode="copy">
				<xsl:sort select="@lang"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<!-- Put the the note and prop elements first -->
	<xsl:template match="tuv" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates select="prop" mode="copy"/>
			<xsl:apply-templates select="note" mode="copy"/>
			<xsl:apply-templates select="seg" mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
