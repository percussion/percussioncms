<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:template match="/">
      <xsl:if test="count(/ActionList/*)&lt;1">
         <Action name="ROOT" label="ROOT" type="MENU"/>
      </xsl:if>
      <xsl:apply-templates select="/ActionList/*" mode="copy"/>
   </xsl:template>
   <xsl:template match="*" mode="copy">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates select="*" mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="ActionList" mode="copy" priority="10">
      <Action>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates select="*" mode="copy"/>
      </Action>
   </xsl:template>
   <xsl:template match="Action" mode="copy" priority="10">
      <Action>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates select="*" mode="copy"/>
      </Action>
   </xsl:template>
   <xsl:template match="Params |Props | VisibilityContexts" mode="copy">
      <xsl:variable name="children" select="document(@url)/*/*[@name!='']"/>
      <xsl:if test="count($children)">
         <xsl:copy>
            <!--			<xsl:copy-of select="@*"/> -->
            <xsl:copy-of select="$children"/>
         </xsl:copy>
      </xsl:if>
   </xsl:template>
</xsl:stylesheet>
