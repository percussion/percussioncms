<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:template match="/">
      <xsl:apply-templates mode="copy"/>
   </xsl:template>
   <xsl:template match="*" mode="copy">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="WorkflowApps" mode="copy" priority="10">
   <xsl:copy-of select="document(url)//WorkflowApps"/>
   </xsl:template>
   <xsl:template match="States" mode="copy" priority="10">
   <xsl:copy-of select="document(url)//States"/>
   </xsl:template>
</xsl:stylesheet>
