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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="PSXTableLocator ">
		<xsl:copy>
			<xsl:apply-templates select="@*|PSXBackEndCredential|Origin|Database"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="PSXBackEndTable ">
		<xsl:copy>
			<xsl:apply-templates select="@*|alias|table|origin"/>
			<datasource/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="PSXBackEndCredential ">
		<xsl:copy>
			<xsl:apply-templates select="@*|alias|table|origin"/>
			<datasource/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="origin">
		<!-- remove origin -->
	</xsl:template>
	<xsl:template match="Origin">
		<!-- remove Origin -->
	</xsl:template>
	<xsl:template match="Database">
		<!-- remove Database -->
	</xsl:template>
	
	<xsl:template match="*|@*|comment()|processing-instruction()|text()">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
