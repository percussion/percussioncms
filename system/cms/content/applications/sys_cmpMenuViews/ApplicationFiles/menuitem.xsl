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
	<xsl:variable name="sysimgpath" select="'../sys_resources/images/'"/>
	<xsl:output method="xml"/>
	<xsl:template match="/">
		<xsl:variable name="userroles" select="document(*/userrolesurl)/*"/>
		<xsl:variable name="pagename" select="*/pagename"/>
		<xsl:variable name="componentcontext" select="document(*/contexturl)/*/context"/>
		<html>
			<head>
				<title>Rhythmyx Content Manager - Banner</title>
			</head>
			<body>
				<table valign="top" width="100%" cellpadding="0" cellspacing="0" border="0">
					<xsl:apply-templates select="menuitem">
						<xsl:with-param name="indent"/>
						<xsl:with-param name="pagename">
							<xsl:value-of select="$pagename"/>
						</xsl:with-param>
					</xsl:apply-templates>
				</table>
				<span>&nbsp;</span>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="menuitem">
		<xsl:param name="indent"/>
		<xsl:param name="pagename"/>
		<tr class="navdatacell1">
			<xsl:attribute name="class"><xsl:choose><xsl:when test="$pagename=@name and url"><xsl:value-of select="'navdatacellHighlight'"/></xsl:when><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'navdatacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'navdatacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
			<td>
				<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'navdatacell1font'"/></xsl:when><xsl:otherwise><xsl:value-of select="'navdatacell2font'"/></xsl:otherwise></xsl:choose></xsl:attribute>
				<xsl:choose>
					<xsl:when test="url">
						<xsl:value-of select="$indent"/>
						<a>
							<xsl:attribute name="href"><xsl:value-of select="url"/></xsl:attribute>
							<xsl:value-of select="displaytext"/>
						</a>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="class"><xsl:text>outerboxcellfont</xsl:text></xsl:attribute>
						<xsl:value-of select="$indent"/>
						<xsl:value-of select="displaytext"/>
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
		<xsl:apply-templates select="menuitem">
			<xsl:with-param name="indent">
				<xsl:value-of select="concat($indent, '&nbsp;&nbsp;&nbsp;')"/>
			</xsl:with-param>
			<xsl:with-param name="pagename">
				<xsl:value-of select="$pagename"/>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
</xsl:stylesheet>
