<?xml version="1.0" encoding="UTF-8"?>


<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/TR/REC-html40" xmlns:urlencoder="java.net.URLEncoder"
                exclude-result-prefixes="urlencoder">
   <xsl:output method="xml"/>
   <!-- main template -->
   <xsl:template match="/">
      <xsl:apply-templates select="." mode="rxbodyfield"/>
   </xsl:template>
   <!-- template matches on anchor and adds the appropriate attributes -->
   <xsl:template match="html:a[@sys_contentid] | a[@sys_contentid]" mode="rxbodyfield" priority="5">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:attribute name="sys_dependentid"><xsl:value-of select="@sys_contentid"/></xsl:attribute>
         <xsl:attribute name="sys_dependentvariantid"><xsl:value-of select="@sys_variantid"/></xsl:attribute>
         <xsl:attribute name="inlinetype">rxhyperlink</xsl:attribute>
         <xsl:attribute name="rxinlineslot">103</xsl:attribute>
         <xsl:apply-templates mode="rxbodyfield"/>
      </xsl:copy>
   </xsl:template>
   <!-- template matches on image tags and adds the appropriate attributes -->
   <xsl:template match="html:img[@sys_contentid] | img[@sys_contentid]" mode="rxbodyfield" priority="5">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:attribute name="sys_dependentid"><xsl:value-of select="@sys_contentid"/></xsl:attribute>
         <xsl:attribute name="sys_dependentvariantid"><xsl:value-of select="@sys_variantid"/></xsl:attribute>
         <xsl:attribute name="inlinetype">rximage</xsl:attribute>
         <xsl:attribute name="rxinlineslot">104</xsl:attribute>
         <xsl:apply-templates mode="rxbodyfield"/>
      </xsl:copy>
   </xsl:template>
   <!--Template to ignore already converted links -->
   <xsl:template match="*[@rxinlineslot]" mode="rxbodyfield" priority="10">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="rxbodyfield"/>
      </xsl:copy>
   </xsl:template>
   <!-- Generic copy template-->
   <xsl:template match="*|comment()" mode="rxbodyfield">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="rxbodyfield"/>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
