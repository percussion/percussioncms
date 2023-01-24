<?xml version='1.0' encoding='UTF-8'?>



<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="psxi18n"
                extension-element-prefixes="psxi18n">
<xsl:import href="../psx_cerffGenericWord/contentfilter.xsl" /> 

<xsl:strip-space elements="html:p html:span" /> 
<xsl:output method="xml" encoding="UTF-8" />

<xsl:template match="/">
<PSXParam>
	<displaytitle>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='PageDisplayTitle'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='PageDisplayTitle']" />
			</xsl:when>
			<xsl:when test="string(//html:body[1]//html:p[@class='MsoTitle'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='MsoTitle']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND A TITLE</xsl:otherwise>
		</xsl:choose>
	</displaytitle>
	<description>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='PageDescription'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='PageDescription']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND AN ABSTRACT</xsl:otherwise>
		</xsl:choose>
	</description>
	<PageAuthor>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='PageAuthor'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='PageAuthor']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND AN AUTHOR</xsl:otherwise>
		</xsl:choose>
	</PageAuthor>	
</PSXParam>
</xsl:template>

<!-- don't show any nodes that contain deleted text (Word's track changes) -->
<xsl:template match="html:del" />

</xsl:stylesheet>
