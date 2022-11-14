<?xml version="1.0" encoding="UTF-8"?>
<!-- This is a generic file for consists of i18n related templates. -->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:output method="html" omit-xml-declaration="yes" encoding="UTF-8" />
   <!-- Template to return localized text for the given keyword in the given language -->
   <xsl:template name="getLocaleString">
      <xsl:param name="key" select="''"/>
      <xsl:param name="lang" select="''"/>
      <xsl:value-of select="psxi18n:getString($key, $lang)"/>
   </xsl:template>
   <!-- Template to return localized string for a given node -->
   <xsl:template match="*" mode="psxi18n">
      <xsl:param name="lang" select="''"/>
      <xsl:value-of select="psxi18n:getString(., $lang)"/>
   </xsl:template>
   <xsl:template name="getFormatString">
      <xsl:param name="formatstring" select="''"/>
      <xsl:param name="params" select="''"/>
      <xsl:param name="lang" select="''"/>
      <xsl:value-of select="psxi18n:formatMessage($formatstring, $params, $lang)"/>
   </xsl:template>
   <!-- Template to return localized string showing a mnemonic underlined -->
   <xsl:template name="getMnemonicLocaleString">
      <xsl:param name="key" select="''"/>
      <xsl:param name="mnemonickey" select="''"/>
      <xsl:param name="lang" select="''"/>
      <xsl:value-of disable-output-escaping="yes"
                    select="psxi18n:getMnemonicString($key, $mnemonickey, $lang)"/>
   </xsl:template>
   <xsl:template name="getMnemonic">
      <xsl:param name="key" select="''"/>
      <xsl:param name="lang" select="''"/>
      <xsl:value-of select="psxi18n:getMnemonic($key, $lang)"/>
   </xsl:template>
   <xsl:template name="getTooltip">
      <xsl:param name="key" select="''"/>
      <xsl:param name="lang" select="''"/>
      <xsl:value-of select="psxi18n:getTooltip($key, $lang)"/>
   </xsl:template>
</xsl:stylesheet>
