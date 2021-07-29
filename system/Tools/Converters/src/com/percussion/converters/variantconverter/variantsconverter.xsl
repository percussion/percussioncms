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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:urlencoder="java.net.URLEncoder"
				exclude-result-prefixes="urlencoder">
	<!-- Converter tool fills the variant map varible with the values from properties file.-->
   <xsl:variable name="variantMap"/>
	<xsl:output method="xml"/>
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="rxbodyfield"/>
	</xsl:template>
	<!--Template to check for the sys_dependentvariantid and convert-->
	<xsl:template match="@sys_dependentvariantid" mode="rxbodyfield" priority="100">
		<xsl:variable name="oldVar" select="."/>
		<xsl:choose>
			<xsl:when test="$oldVar=$variantMap//variant/@old">
				<xsl:attribute name="sys_dependentvariantid"><xsl:value-of select="$variantMap//variant[@old = $oldVar]/@new"/></xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:copy-of select="@*"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--Template to check for the sys_variantid and convert-->
	<xsl:template match="@sys_variantid" mode="rxbodyfield" priority="100">
		<xsl:variable name="oldVar" select="."/>
		<xsl:choose>
			<xsl:when test="$oldVar=$variantMap//variant/@old">
				<xsl:attribute name="sys_variantid"><xsl:value-of select="$variantMap//variant[@old = $oldVar]/@new"/></xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:copy-of select="@*"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Template to match on other attributes to just copy them-->
	<xsl:template match="@*" mode="rxbodyfield">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="node()" mode="rxbodyfield">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="rxbodyfield"/>
			<xsl:apply-templates mode="rxbodyfield"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
