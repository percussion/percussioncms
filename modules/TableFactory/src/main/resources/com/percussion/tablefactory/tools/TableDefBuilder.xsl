

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
