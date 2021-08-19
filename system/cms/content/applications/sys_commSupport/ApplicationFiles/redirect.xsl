<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
<xsl:include href="file:sys_resources/stylesheets/redirect.xsl" />
<xsl:template match="/">
   <xsl:apply-templates select="redirect" mode="redirect"/>
</xsl:template>
</xsl:stylesheet>
