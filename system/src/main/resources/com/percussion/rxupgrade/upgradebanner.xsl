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
