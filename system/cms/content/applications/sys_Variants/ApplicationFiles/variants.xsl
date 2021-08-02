<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:output method="xml"/>
	<xsl:template match="/">
		<xsl:variable name="userroles" select="document(*/userrolesurl)/*/UserStatus"/>
		<xsl:variable name="componentcontext" select="document(*/contexturl)/*/context"/>
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_Variants.variants@Variants Menu'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
			</head>
			<body>
				<table width="100%" cellpadding="1" cellspacing="0" border="0">
					<tr class="navdatacell1">
						<td align="left" class="outerboxcellfont">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_Variants.variants@Variants'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
					</tr>
					<tr class="navdatacell1">
						<xsl:if test="*/pagename='sys_variants' and */sysnavcontentid=''">
							<xsl:attribute name="class"><xsl:value-of select="'navdatacellHighlight'"/></xsl:attribute>
						</xsl:if>
						<td align="left" class="navdatacell1font">
					&nbsp;&nbsp;&nbsp;<a href="../sys_Variants/sa_variants.html?sys_componentname=sys_variants&amp;sys_pagename=sys_variants">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_Variants.variants@All'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</a>
						</td>
					</tr>
					<xsl:apply-templates select="*" mode="mode0"/>
					<tr class="outerboxcell">
						<td align="left" class="outerboxcellfont">&nbsp;</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="*">
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
	<xsl:template match="attribute::*">
		<xsl:value-of select="."/>
		<xsl:if test="not(position()=last())">
			<br id="XSpLit"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="*" mode="mode0">
		<xsl:for-each select="item">
			<tr>
				<!-- begin XSL -->
				<xsl:attribute name="class"><xsl:choose><xsl:when test="/*/pagename='sys_variants' and /*/sysnavcontentid=contentid"><xsl:value-of select="'navdatacellHighlight'"/></xsl:when><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'navdatacell2'"/></xsl:when><xsl:otherwise><xsl:value-of select="'navdatacell1'"/></xsl:otherwise></xsl:choose></xsl:attribute>
				<!-- end XSL -->
				<td align="left">
					<!-- begin XSL -->
					<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'navdatacell2font'"/></xsl:when><xsl:otherwise><xsl:value-of select="'navdatacell1font'"/></xsl:otherwise></xsl:choose></xsl:attribute>
					<!-- end XSL -->
					<xsl:value-of select="'&nbsp;&nbsp;&nbsp;'"/>
					<a>
						<xsl:attribute name="href"><xsl:value-of select="contenturl"/></xsl:attribute>
						<xsl:value-of select="contentname"/>
					</a>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_Variants.variants@Variants Menu">Title for variants menu component comes under System tab.</key>
		<key name="psx.sys_Variants.variants@Variants">Category name for System tab variants menu items.</key>
		<key name="psx.sys_Variants.variants@All">Link text for All variants.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
