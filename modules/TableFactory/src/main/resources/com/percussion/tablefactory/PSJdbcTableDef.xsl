

<!--
   Transforms documents conforming to the sys_DatabasePublisher.dtd to a 
   document conforming to the tabledef.dtd.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!--
      Transform the element 'tabledefset' to 'tables' and copy all attributes
      without changes.
   -->
   <xsl:template match="tabledefset">
      <tables>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </tables>
   </xsl:template>
   <!--
      Transform all elements 'tabledef' to 'table' and copy all attributes
      without changes.
   -->
   <xsl:template match="tabledef">
      <table>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </table>
   </xsl:template>
   <!--
      Transform all elements 'rowdef' to 'row' and copy all attributes
      without changes.
   -->
   <xsl:template match="rowdef">
      <row>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </row>
   </xsl:template>
   <!--
      Transform all elements 'columndef' to 'column' and copy all attributes
      and children without changes.
   -->
   <xsl:template match="columndef">
      <column>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="copy"/>
      </column>
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
      Remove all table data.
   -->
   <xsl:template match="tabledataset"/>
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
