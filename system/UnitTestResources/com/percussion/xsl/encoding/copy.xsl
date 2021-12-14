<?xml version='1.0' encoding='UTF-8'?>
<xsl:transform version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:param name="encoding"/>
<xsl:output method="xml" version="1.0" encoding="$encoding" indent="no" omit-xml-declaration="no"/>

<xsl:template match="*">
<!-- if we haven't defined special rules, copy the node and its attributes, and recurse on the template -->
<!-- can't use xsl:copy-of select="." because it recurses itself, without checking for template rules --> 
   <xsl:copy>
      <xsl:for-each select="@*">
         <xsl:sort select="name()"/>
      </xsl:for-each>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates /> 
   </xsl:copy>
</xsl:template>

</xsl:transform>
