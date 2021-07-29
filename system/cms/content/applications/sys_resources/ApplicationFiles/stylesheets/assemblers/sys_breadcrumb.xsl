<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<!-- breadcrumb generation -->
	<xsl:template name="output-breadcrumb">
		<xsl:param name="activeiteminfourl"/>
		<xsl:param name="trail"/>
		<xsl:param name="list"/>
		<xsl:variable name="first" select="substring-before($list, ':')"/>
		<xsl:variable name="rest" select="substring-after($list, ':')"/>
		<xsl:if test="$first">
			<span class="PSbreadcrumb">&nbsp;&gt;&nbsp;</span>
			<a class="PSbreadcrumb" href="javascript:void(0)" onclick="PSActivateBreadCrumbItem('{$first}','{$trail}');return false;">
				<xsl:value-of select="document(concat($activeiteminfourl, '&amp;sys_activeitemid=',$first))/activeitem/@title"/>
			</a>
			<xsl:if test="$rest">
				<xsl:call-template name="output-breadcrumb">
					<xsl:with-param name="activeiteminfourl" select="$activeiteminfourl"/>
					<xsl:with-param name="trail" select="$trail"/>
					<xsl:with-param name="list" select="$rest"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
