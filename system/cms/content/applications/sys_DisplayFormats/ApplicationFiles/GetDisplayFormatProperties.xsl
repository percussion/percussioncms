<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
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
   <xsl:template match="PSXDFMultiProperty" mode="copy">
      <xsl:variable name="children" select="document(@url)/."/>
      <xsl:if test="count($children/*/*) > 0">
            <xsl:copy-of select="$children"/>
      </xsl:if>

   </xsl:template>
</xsl:stylesheet>
