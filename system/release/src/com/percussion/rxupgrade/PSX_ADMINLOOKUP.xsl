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
	<xsl:template match="row" mode="sortandgetmax">
		<xsl:if test="position()=last()">
			<xsl:value-of select="column[@name='ID']"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="row" mode="findexists">
		<xsl:param name="type"/>
		<xsl:param name="cat"/>
		<xsl:param name="val"/>
		<xsl:choose>
			<xsl:when test="val=''">
				<xsl:if test="(column[@name='TYPE']=$type and column[@name='CATEGORY']=$cat)">
					<xsl:value-of select="'found'"/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="(column[@name='TYPE']=$type and column[@name='CATEGORY']=$cat and column[@name='NAME']=$val)">
					<xsl:value-of select="'found'"/>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="table" mode="copy">
		<xsl:variable name="maxid">
			<xsl:apply-templates select="row" mode="sortandgetmax">
				<xsl:sort select="column[@name='ID']"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:attribute name="onCreateOnly">no</xsl:attribute>
			<xsl:apply-templates mode="copy"/>
			<xsl:variable name="var1">
				<xsl:apply-templates select="row" mode="findexists">
					<xsl:with-param name="type" select="'role'"/>
					<xsl:with-param name="cat" select="'SYS_ATTRIBUTENAME'"/>
					<xsl:with-param name="val" select="'sys_defaultCommunity'"/>
				</xsl:apply-templates>
			</xsl:variable>
			<xsl:if test="$var1!='found'">
				<row>
					<column name="ID"><xsl:value-of select="($maxid + 1)"/></column>
					<column name="TYPE">role</column>
					<column name="CATEGORY">SYS_ATTRIBUTENAME</column>
					<column name="NAME">sys_defaultCommunity</column>
					<column name="LIMITTOLIST">Y</column>
					<column name="CATALOGURL">sys_commSupport/sysdefaultcommunity.xml</column>
				</row>
			</xsl:if>
			<xsl:variable name="var2">
				<xsl:apply-templates select="row" mode="findexists">
					<xsl:with-param name="type" select="'role'"/>
					<xsl:with-param name="cat" select="'SYS_ATTRIBUTENAME'"/>
					<xsl:with-param name="val" select="'sys_defaultHomepageURL'"/>
				</xsl:apply-templates>
			</xsl:variable>
			<xsl:if test="$var2!='found'">
				<row>
					<column name="ID"><xsl:value-of select="($maxid + 2)"/></column>
					<column name="TYPE">role</column>
					<column name="CATEGORY">SYS_ATTRIBUTENAME</column>
					<column name="NAME">sys_defaultHomepageURL</column>
					<column name="LIMITTOLIST"/>
					<column name="CATALOGURL"/>
				</row>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
