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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:param name="buildId"/>
	<xsl:param name="buildNumber"/>
	<xsl:param name="interfaceVersion"/>
	<xsl:param name="majorVersion"/>
	<xsl:param name="minorVersion"/>
	<xsl:param name="microVersion"/>
	<xsl:param name="versionString"/>
	<xsl:param name="serverName"/>
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
	<xsl:template match="PSXArchiveInfo" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="serverName"><xsl:value-of select="$serverName"/></xsl:attribute>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="PSXFormatVersion" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="buildId"><xsl:value-of select="$buildId"/></xsl:attribute>
			<xsl:attribute name="buildNumber"><xsl:value-of select="$buildNumber"/></xsl:attribute>
			<xsl:attribute name="interfaceVersion"><xsl:value-of select="$interfaceVersion"/></xsl:attribute>
			<xsl:attribute name="majorVersion"><xsl:value-of select="$majorVersion"/></xsl:attribute>
			<xsl:attribute name="minorVersion"><xsl:value-of select="$minorVersion"/></xsl:attribute>
			<xsl:attribute name="microVersion"><xsl:value-of select="$microVersion"/></xsl:attribute>
			<xsl:attribute name="versionString"><xsl:value-of select="$versionString"/></xsl:attribute>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
