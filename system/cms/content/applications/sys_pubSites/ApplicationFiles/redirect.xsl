<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n">
<xsl:include href="file:sys_resources/stylesheets/redirect.xsl" />
<xsl:template match="/">
   <xsl:apply-templates select="redirect" mode="redirect"/>
</xsl:template>
</xsl:stylesheet>
