<?xml version='1.0' encoding='UTF-8'?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="v o w dt"
                extension-element-prefixes="psxi18n">
<xsl:import href="../psx_cerffGenericWord/contentfilter.xsl" /> 	
<xsl:strip-space elements="html:p html:span" /> 
<xsl:output method="xml" encoding="UTF-8" />
<xsl:variable name="styles" select="//html:head/html:style"/>

<xsl:template match="/">
<div class="rxbodyfield">
		<!-- Uncomment this line if you want to preserve the styles from word. If you preserve the word styles it may conflict with the styles on the assembly page and may render assembly page wrong.-->
      <!--<xsl:copy-of select="$styles"/>-->
		<xsl:apply-templates select="//html:body[1]/*" mode="contentfilter" />
</div>		
</xsl:template>

<!-- don't show any nodes that contain deleted text (Word's track changes) -->
<xsl:template match="html:del" />

</xsl:stylesheet>
