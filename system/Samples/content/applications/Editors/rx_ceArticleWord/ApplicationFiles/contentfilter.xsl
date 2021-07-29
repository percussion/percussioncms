<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="psxi18n"
				xmlns:psxi18n="urn:www.percussion.com/i18n">

<xsl:strip-space elements="html:p html:span" /> 
<xsl:output method="xml" /> 

<!-- don't show any nodes that contain deleted text (Word's track changes) -->
<xsl:template match="html:del" mode="contentfilter"/>

<!-- filter out all <font> tags without class attributes -->
<xsl:template match="font[not(@class)]" >
   <xsl:apply-templates select="child::node()" mode="contentfilter" /> 
</xsl:template> 


<xsl:template match="html:p[@class='ArticleDisplayTitle']" mode="contentfilter" />

<xsl:template match="html:p[@class='ArticleAbstract']" mode="contentfilter" />

<xsl:template match="html:p[@class='ArticleAuthor']" mode="contentfilter" />

<!-- default template, just copy it -->
<xsl:template match="*" mode="contentfilter">
   <xsl:copy><xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="contentfilter" />    		
   </xsl:copy>
</xsl:template>

</xsl:stylesheet>













