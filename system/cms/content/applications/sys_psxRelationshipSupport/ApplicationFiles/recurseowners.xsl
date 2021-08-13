<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
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
   <xsl:template match="Owner[@sys_contentid!='']" mode="copy" priority="10">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates select="document(OwnerLink)//Owner" mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="Owner[@sys_contentid='']" mode="copy" priority="10"/>
</xsl:stylesheet>
