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
