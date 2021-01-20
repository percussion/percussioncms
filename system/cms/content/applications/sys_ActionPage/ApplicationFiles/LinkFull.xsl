<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" omit-xml-declaration="yes" />

<xsl:variable name="title" select="//@title" />

<!-- This very simple stylesheet just returns the text link from a link element passed -->
<xsl:template match="/link">
   <a><xsl:attribute name="href"><xsl:value-of select='@url'/></xsl:attribute><xsl:value-of select="$title"/></a>        
</xsl:template>

</xsl:stylesheet>
