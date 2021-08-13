<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:variable name="contentid1" select="//sys_contentid1"/>
   <xsl:variable name="contentid2" select="//sys_contentid2"/>
   <xsl:variable name="revision1" select="//sys_revision1"/>
   <xsl:variable name="revision2" select="//sys_revision2"/>
   <xsl:variable name="siteid" select="//sys_siteid"/>
   <xsl:variable name="variantid1">
      <xsl:choose>
         <xsl:when test="//sys_variantid1!=''">
            <xsl:value-of select="//sys_variantid1"/>
         </xsl:when>
         <xsl:when test="$contentid1!=''">
            <xsl:value-of select="document(//variantlisturl1)//VariantList/Variant/@variantId"/>
         </xsl:when>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="variantid2">
      <xsl:choose>
         <xsl:when test="//sys_variantid2!=''">
            <xsl:value-of select="//sys_variantid2"/>
         </xsl:when>
         <xsl:when test="$contentid2!=''">
            <xsl:value-of select="document(//variantlisturl2)//VariantList/Variant/@variantId"/>
         </xsl:when>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="baseurl1">
      <xsl:choose>
         <xsl:when test="$variantid1 != ''">
            <xsl:value-of select="document(concat(//assemblyurl,'&amp;sys_variantid=',$variantid1))//@current"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="//blankpageurl"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="baseurl2">
      <xsl:choose>
         <xsl:when test="$variantid2 != ''">
            <xsl:value-of select="document(concat(//assemblyurl,'&amp;sys_variantid=',$variantid2))//@current"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="//blankpageurl"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="querysep1">
      <xsl:choose>
         <xsl:when test="contains($baseurl1,'?')">
            <xsl:value-of select="'&amp;'"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="'?'"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="querysep2">
      <xsl:choose>
         <xsl:when test="contains($baseurl2,'?')">
            <xsl:value-of select="'&amp;'"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="'?'"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="assemblyurl1">
      <xsl:choose>
         <xsl:when test="$variantid1 != '' and $contentid1 != '' and $revision1 != ''">
            <xsl:value-of select="concat($baseurl1,$querysep1,'sys_variantid=',$variantid1,'&amp;sys_contentid=',$contentid1,'&amp;sys_revision=',$revision1,'&amp;sys_siteid=',$siteid,'&amp;sys_authtype=0&amp;sys_context=0')"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="//blankpageurl"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="assemblyurl2">
      <xsl:choose>
         <xsl:when test="$variantid2 != '' and $contentid2 != '' and $revision2 != ''">
            <xsl:value-of select="concat($baseurl2,$querysep2,'sys_variantid=',$variantid2,'&amp;sys_contentid=',$contentid2,'&amp;sys_revision=',$revision2,'&amp;sys_siteid=',$siteid,'&amp;sys_authtype=0&amp;sys_context=0')"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="//blankpageurl"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="compareurl">
      <xsl:choose>
         <xsl:when test="$contentid1='' or $variantid1='' or $revision1='' or $contentid2='' or $variantid2='' or $revision2=''">
            <xsl:value-of select="//blankpageurl"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="concat('../sys_compareHandler/ComparePage.html?sys_contentid1=',$contentid1,'&amp;sys_siteid=',$siteid,'&amp;sys_revision1=',$revision1,'&amp;sys_variantid1=',$variantid1,'&amp;sys_contentid2=',$contentid2,'&amp;sys_revision2=',$revision2,'&amp;sys_variantid2=',$variantid2)"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="taburl" select="concat(//taburl,'&amp;sys_contentid1=',$contentid1,'&amp;sys_siteid=',$siteid,'&amp;sys_revision1=',$revision1,'&amp;sys_variantid1=',$variantid1,'&amp;sys_contentid2=',$contentid2,'&amp;sys_revision2=',$revision2,'&amp;sys_variantid2=',$variantid2)"/>
   <xsl:variable name="contentframeurl">
      <xsl:choose>
         <xsl:when test="//activeitem='1'">
            <xsl:value-of select="$assemblyurl1"/>
         </xsl:when>
         <xsl:when test="//activeitem='2'">
            <xsl:value-of select="$assemblyurl2"/>
         </xsl:when>
         <xsl:when test="//activeitem='3'">
            <xsl:value-of select="$compareurl"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="//blankpageurl"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:template match="/">
      <html>
         <head>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_Compare.compare@Rhythmyx - Document Comparison'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link href="../sys_resources/css/templates.css" rel="stylesheet" type="text/css"/>
            <link href="../rx_resources/css/templates.css" rel="stylesheet" type="text/css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
         </head>
         <frameset rows="55,*" border="0">
            <frame name="tabframe" scrolling="auto" src="{$taburl}"/>
            <frame name="contentframe" scrolling="auto">
               <xsl:attribute name="src"><xsl:value-of select="$contentframeurl"/></xsl:attribute>
            </frame>
            <noframes>
               <body>
                  <p>This page uses frames, but your browser doesn't support them.</p>
               </body>
            </noframes>
         </frameset>
      </html>
   </xsl:template>
   <xsl:template name="generateasemblyurl"/>
</xsl:stylesheet>
