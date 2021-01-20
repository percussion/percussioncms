<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!--
This style is used to sort the TMX resource document just before saving to
the disk. Idea is to make it easier for sombody to locate a required translation
key.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
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
				<xsl:sort select="@xml:lang"/>
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
