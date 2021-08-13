<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
<xsl:include href="file:sys_resources/stylesheets/redirect.xsl" />
<xsl:template match="/">
   <xsl:apply-templates select="redirect" mode="redirect">
      <xsl:with-param name="lang" select="//@xml:lang"/>
   </xsl:apply-templates>
</xsl:template>
</xsl:stylesheet>
