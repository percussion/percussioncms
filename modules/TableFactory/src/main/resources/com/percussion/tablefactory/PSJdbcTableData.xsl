

<!--
   Transforms documents conforming to the sys_DatabasePublisher.dtd to a 
   document conforming to the tabledata.dtd.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!--
      Remove the table definition.
   -->
   <xsl:template match="tabledefset"/>
   <!--
      Transform the 'tabledataset' element to 'tables' and copy all children.
   -->
   <xsl:template match="tabledataset">
      <tables>
       <xsl:apply-templates select="//childtable" mode="children"/>
         <xsl:apply-templates/>        
      </tables>
   </xsl:template>
   <!--
      Keep all 'table' elements.
   -->
   <xsl:template match="table">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>
   <!--
      Keep all 'row' elements.
   -->
   <xsl:template match="row">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>
   <!--
      Keep all 'column' elements.
   -->
   <xsl:template match="column">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>
   <!--
      Transform all elements 'childtable' to 'table' and copy all attributes
      without changes.
   -->
   <xsl:template match="childtable" mode="children">
      <table>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </table>
   </xsl:template>
   <xsl:template match="childtable">   
   </xsl:template>
   <!--
      Copy any attribute and element.
   -->
   <xsl:template match="@*|*" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
