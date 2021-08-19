<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:template match="/">
      <xsl:apply-templates select="/*" mode="copy"/>
   </xsl:template>
   <xsl:template match="*" mode="copy">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="PSX_PROPERTIES" mode="copy">
      <xsl:variable name="children" select="document(@url)/."/>
      <xsl:if test="count($children/*/*) > 0">
            <xsl:copy-of select="$children"/>
      </xsl:if>
   </xsl:template>
   <xsl:template match="PSX_FIELDS" mode="copy">
      <xsl:variable name="children" select="document(@url)/."/>
      <xsl:if test="count($children/*/*) > 0">
            <xsl:copy-of select="$children"/>
      </xsl:if>
   </xsl:template>
</xsl:stylesheet>
