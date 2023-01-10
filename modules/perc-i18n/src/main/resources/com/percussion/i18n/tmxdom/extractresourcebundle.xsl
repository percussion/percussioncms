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

<!--
   This stylesheet is used to extract all translation units, but with only a set
   of language specific notes, properties, and variants within each unit.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!-- main template -->
   <xsl:param name="extractlang"/>
   <xsl:variable name="reqlang" select="$extractlang"/>
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
   <xsl:template match="@*|*" mode="copy1">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="header" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates select="prop[.=$reqlang]" mode="copy1"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="tu" mode="copy">
    <xsl:choose>
         <xsl:when test=".//node()[@lang=$reqlang]">
          <xsl:copy>
             <xsl:apply-templates select="@*" mode="copy"/>
             <xsl:apply-templates  mode="copy1"/>
          </xsl:copy>
        </xsl:when>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="tuv | note | prop" mode="copy">
      <xsl:choose>
         <xsl:when test="@lang=$reqlang">
            <xsl:copy>
               <xsl:apply-templates select="@*" mode="copy"/>
               <xsl:apply-templates mode="copy"/>
            </xsl:copy>
         </xsl:when>
      </xsl:choose>
   </xsl:template>
</xsl:stylesheet>
