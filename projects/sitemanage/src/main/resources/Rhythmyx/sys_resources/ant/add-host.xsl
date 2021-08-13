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

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="xalan://com.percussion.i18n.PSI18nUtils"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	
    <xsl:param name="perc.site" select="'adam'" />
    
	<xsl:template match="@* | node()">
	   <xsl:copy>
	      <xsl:apply-templates select="@* | node()" />
	   </xsl:copy>
	</xsl:template>
	
	
	<xsl:template match="Engine[@name='Catalina']" priority="100">
        <xsl:copy>
        <xsl:apply-templates select="@*" />
        <xsl:if test="not(Host[@name=$perc.site])">
        <Host name="{$perc.site}" appBase="{concat($perc.site,'apps')}" />
        </xsl:if>
        <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
	</xsl:template>
    
    
</xsl:stylesheet>
