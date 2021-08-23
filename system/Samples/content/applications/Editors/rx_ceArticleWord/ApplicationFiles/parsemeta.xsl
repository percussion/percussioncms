<?xml version='1.0' encoding='UTF-8'?>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882" xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
<xsl:import href="../rx_ceArticleWord/contentfilter.xsl" /> 

<xsl:strip-space elements="html:p html:span" /> 
<xsl:output method="xml" /> 

<xsl:template match="/">
<PSXParam>
	<ArticleTitle>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='ArticleDisplayTitle'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='ArticleDisplayTitle']" />
			</xsl:when>
			<xsl:when test="string(//html:body[1]//html:p[@class='MsoTitle'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='MsoTitle']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND A TITLE</xsl:otherwise>
		</xsl:choose>
	</ArticleTitle>
	<ArticleAbstract>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='ArticleAbstract'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='ArticleAbstract']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND AN ABSTRACT</xsl:otherwise>
		</xsl:choose>
	</ArticleAbstract>
	<ArticleAuthor>
		<xsl:choose>
			<xsl:when test="string(//html:body[1]//html:p[@class='ArticleAuthor'])">
				<xsl:apply-templates select="//html:body[1]//html:p[@class='ArticleAuthor']" />
			</xsl:when>
			<xsl:otherwise>COULD NOT FIND AN AUTHOR</xsl:otherwise>
		</xsl:choose>
	</ArticleAuthor>	
</PSXParam>
</xsl:template>

<!-- don't show any nodes that contain deleted text (Word's track changes) -->
<xsl:template match="html:del" />

</xsl:stylesheet>
