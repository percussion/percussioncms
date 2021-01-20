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
   Transforms documents conforming to the tabledef.dtd to a 
   document conforming to the sys_Tabledef.dtd.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!--
      Transform the element 'tables' to 'tabledefset' and copy all attributes
      without changes.
   -->
   <xsl:template match="/">
      <tabledefset>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </tabledefset>
   </xsl:template>
   <!--
      Transform all elements 'table' to 'tabledef' and copy all attributes
      without changes.
   -->
   <xsl:template match="table">
      <tabledef>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </tabledef>
   </xsl:template>
   <!--
      Transform all elements 'row' to 'rowdef' and copy all attributes
      without changes.
   -->
   <xsl:template match="row">
      <rowdef>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </rowdef>
   </xsl:template>
   <!--
      Transform all elements 'column' to 'columndef' and copy all attributes
      and children without changes.
   -->
   <xsl:template match="column">
      <columndef>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="copy"/>
      </columndef>
   </xsl:template>
   <!--
      Copy all 'primarykey' elements and attributes.
   -->
   <xsl:template match="primarykey">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <!--
      Copy all 'foreignkey' elements and attributes.
   -->
   <xsl:template match="foreignkey">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <!--
      Copy all 'updatekey' elements and attributes.
   -->
   <xsl:template match="updatekey">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <!--
      Copy all 'indexdefinitions' elements and attributes.
   -->
   <xsl:template match="indexdefinitions">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <!--
      Copy any attribute and element recursive.
   -->
   <xsl:template match="@*|*" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
