<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:template match="/">
      <Action name="ROOT" label="ROOT" type="MENU">
         <xsl:apply-templates select="/ActionList/*" mode="copy"/>
      </Action>
   </xsl:template>
   <xsl:template match="*" mode="copy">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates select="*" mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="ActionList" mode="copy" priority="10">
      <Action type="MENU">
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
   <xsl:template match="ActionList[@name='Paste']" mode="copy" priority="20">
      <Action>
         <xsl:attribute name="type">MENU</xsl:attribute>
         <xsl:copy-of select="@*"/>
         <!-- do not copy children of this action. Those are going to be built dynamically -->
		 <xsl:apply-templates select="Props" mode="copy"/>
		 <xsl:apply-templates select="Params" mode="copy"/>
         <xsl:apply-templates select="VisibilityContexts" mode="copy"/>
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
