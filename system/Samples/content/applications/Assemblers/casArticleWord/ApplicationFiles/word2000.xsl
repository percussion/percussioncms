<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;

        ]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
 
  <xsl:strip-space elements="p b i em a u" /> 

  <xsl:template match="body" mode="word2000">      
  <xsl:apply-templates select="descendant::p" mode="word2000" />	
  </xsl:template>
  
  <xsl:template match="p[@class='ArticleTitle']" mode="word2000" >
     <font face="Arial" size="6"><p>
	<xsl:apply-templates mode="word2000" />
     </p></font>
  </xsl:template>

  <xsl:template match="p[@class='ArticleAuthor']" mode="word2000" >
	<p>By: <i><xsl:apply-templates mode="word2000" /></i></p>
  </xsl:template>   
  
  <xsl:template match="p[@class='ArticleBody']" mode="word2000" > 
	<p><xsl:apply-templates mode="word2000" /></p>
  </xsl:template> 
 
  <xsl:template match="a" mode="word2000" >
      <a href="{@href}">
          <xsl:value-of select="." />
	</a>
  </xsl:template>
  
  <xsl:template match="b|em|i| u" mode="word2000">
	<xsl:element name="{name()}"><xsl:value-of select="." /></xsl:element>
  </xsl:template>
 
  <xsl:template match="em" mode="wordc2000">
	<em><xsl:value-of select="." /></em>
  </xsl:template>

  <xsl:template match="i" mode="wordc2000">
	<i><xsl:value-of select="." /></i>
  </xsl:template>

  
  <xsl:template match="p" mode="word2000" >	
  </xsl:template>

  <xsl:template match="*" mode="word2000-inside" >
      <xsl:value-of select="." /> 
  </xsl:template>

  <xsl:template match="*" mode="xxxxword2000-inside">  
     
  <xsl:choose>      
     <xsl:when test="text()">   
       <xsl:choose>     
          <xsl:when test="@no-escaping">
          <xsl:value-of select="." disable-output-escaping="yes"/>
         </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>&nbsp;</xsl:otherwise>
    </xsl:choose>
    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>
  </xsl:template>

<xsl:template match="attribute::*" mode="word2000-inside" >
    <xsl:value-of select="."/>
    <xsl:if test="not(position()=last())">
      <br id="XSpLit"/>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
