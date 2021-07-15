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
        <!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:html="http://www.w3.org/TR/REC-html40" xmlns:urlencoder="java.net.URLEncoder" exclude-result-prefixes="urlencoder">
   <xsl:output method="xml"/>
   <!-- main template -->
   <xsl:template match="/">
      <xsl:apply-templates select="." mode="rxbodyfield"/>
   </xsl:template>
   <!-- template matches on anchor and adds the appropriate attributes -->
   <xsl:template match="html:a[@sys_contentid] | a[@sys_contentid]" mode="rxbodyfield" priority="5">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:attribute name="sys_dependentid"><xsl:value-of select="@sys_contentid"/></xsl:attribute>
         <xsl:attribute name="sys_dependentvariantid"><xsl:value-of select="@sys_variantid"/></xsl:attribute>
         <xsl:attribute name="inlinetype">rxhyperlink</xsl:attribute>
         <xsl:attribute name="rxinlineslot">103</xsl:attribute>
         <xsl:apply-templates mode="rxbodyfield"/>
      </xsl:copy>
   </xsl:template>
   <!-- template matches on image tags and adds the appropriate attributes -->
   <xsl:template match="html:img[@sys_contentid] | img[@sys_contentid]" mode="rxbodyfield" priority="5">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:attribute name="sys_dependentid"><xsl:value-of select="@sys_contentid"/></xsl:attribute>
         <xsl:attribute name="sys_dependentvariantid"><xsl:value-of select="@sys_variantid"/></xsl:attribute>
         <xsl:attribute name="inlinetype">rximage</xsl:attribute>
         <xsl:attribute name="rxinlineslot">104</xsl:attribute>
         <xsl:apply-templates mode="rxbodyfield"/>
      </xsl:copy>
   </xsl:template>
   <!--Template to ignore already converted links -->
   <xsl:template match="*[@rxinlineslot]" mode="rxbodyfield" priority="10">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="rxbodyfield"/>
      </xsl:copy>
   </xsl:template>
   <!-- Generic copy template-->
   <xsl:template match="*|comment()" mode="rxbodyfield">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="rxbodyfield"/>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
