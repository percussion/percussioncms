<?xml version="1.0" encoding="UTF-8"?>


<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
		<!ENTITY % w3centities-f PUBLIC
				"-//W3C//ENTITIES Combined Set//EN//XML"
				"http://www.w3.org/2003/entities/2007/w3centities-f.ent"
				>
		%w3centities-f;
		]>
<!--Inline link converter xsl to remove the sys_folderid attributes from the inline elements.-->
<!--
override inlinelinkconverter.xsl with this file, then run the RhythmyxInlineLinkConverter.exe (or *.bin in unix) to remove the sys_folderid attributes fron the inline link in the content. User should also remove the sys_folderid relationship properties from the relationship table, which can be done with a simple SQL statement.
-->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="urlencoder">
	<xsl:output method="xml"/>
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="rxbodyfield"/>
	</xsl:template>
	<!--Template matches on sys_folderid attribute and removes it-->
	<xsl:template match="@*[name()='sys_folderid']" mode="rxbodyfield" priority="100"/>
	<!-- Generic copy template-->
	<!-- This template "eats" the extra <div> tag that we have to add to the HTML -->
	<xsl:template match="@*|*|comment()" mode="rxbodyfield">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="rxbodyfield"/>
			<xsl:apply-templates mode="rxbodyfield"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
