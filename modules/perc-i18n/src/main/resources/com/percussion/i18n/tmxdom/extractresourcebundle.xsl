<?xml version="1.0" encoding="UTF-8"?>


<!--
   This stylesheet is used to extract all translation units, but with only a set
   of language specific notes, properties, and variants within each unit.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!-- main template -->
   <xsl:param name="extractlang"/>
   <xsl:variable name="reqlang" select="$extractlang"/>
   <xsl:template match="/">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <!-- copy any attribute or template -->
   <xsl:template match="@*|*" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="@*|*" mode="copy1">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="header" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates select="prop[.=$reqlang]" mode="copy1"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="tu" mode="copy">
    <xsl:choose>
         <xsl:when test=".//node()[@lang=$reqlang]">
          <xsl:copy>
             <xsl:apply-templates select="@*" mode="copy"/>
             <xsl:apply-templates  mode="copy1"/>
          </xsl:copy>
        </xsl:when>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="tuv | note | prop" mode="copy">
      <xsl:choose>
         <xsl:when test="@lang=$reqlang">
            <xsl:copy>
               <xsl:apply-templates select="@*" mode="copy"/>
               <xsl:apply-templates mode="copy"/>
            </xsl:copy>
         </xsl:when>
      </xsl:choose>
   </xsl:template>
</xsl:stylesheet>

