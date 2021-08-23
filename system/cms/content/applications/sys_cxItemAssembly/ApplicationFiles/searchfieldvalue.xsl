<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
   <xsl:include href="file:rx_resources/stylesheets/rx_DisplayFieldTemplates.xsl"/>
   <!-- main template -->
   <xsl:template match="/">
      <xsl:apply-templates mode="copy"/>
   </xsl:template>
   <xsl:template match="Result" mode="copy">
      <xsl:variable name="cid" select="ResultField[@name='sys_contenttypeid']"/>
      <xsl:variable name="contentid" select="ResultField[@name='sys_contentid']"/>
      <xsl:variable name="revision" select="ResultField[@name='sys_revision']"/>
      <xsl:variable name="variantid" select="ResultField[@name='sys_variantid']"/>
      <xsl:variable name="res" select="."/>
      <Result>
         <xsl:apply-templates select="$res/@*" mode="copy"/>
         <xsl:apply-templates select="$res/*" mode="rc_res_displayfield">
            <xsl:with-param name="sys_contentid" select="$contentid"/>
            <xsl:with-param name="sys_revision" select="$revision"/>
            <xsl:with-param name="sys_variantid" select="$variantid"/>
            <xsl:with-param name="sys_contenttypeid" select="$cid"/>
         </xsl:apply-templates>
      </Result>
   </xsl:template>
   <xsl:template match="@*|*" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="@*|*" mode="rc_res_displayfield">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
