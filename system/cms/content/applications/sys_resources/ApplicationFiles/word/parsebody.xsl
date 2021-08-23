<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882" xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="v o w dt" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:import href="../rx_ceArticleWord/contentfilter.xsl" /> 	
<xsl:strip-space elements="html:p html:span" /> 
<xsl:output method="xml" /> 
<xsl:variable name="styles" select="//html:head/html:style"/>

<xsl:template match="/">
<div class="rxbodyfield">
		<xsl:copy-of select="$styles"/>
		<xsl:apply-templates select="//html:body[1]/*" mode="contentfilter" />
</div>		
</xsl:template>

<!-- don't show any nodes that contain deleted text (Word's track changes) -->
<xsl:template match="html:del" />

</xsl:stylesheet>
