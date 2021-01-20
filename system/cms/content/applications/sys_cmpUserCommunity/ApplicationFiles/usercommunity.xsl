<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="page">
		<xsl:choose>
			<xsl:when test="//pagename!=''">
				<xsl:value-of select="//pagename"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'ca_main'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="defactiveitem">
		<xsl:choose>
			<xsl:when test="contains($page,'sys_')">4</xsl:when>
			<xsl:when test="contains($page,'wf_')">3</xsl:when>
			<xsl:when test="contains($page,'pub_')">2</xsl:when>
			<xsl:otherwise>1</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:output method="xml"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>User Community</title>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_cmpUserCommunity.usercommunity@User Community'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
			</head>
			<body>
				<tr>
					<td class="field">
						<xsl:call-template name="getLocaleString">
							<xsl:with-param name="key" select="'psx.sys_cmpUserCommunity.usercommunity@Community'"/>
							<xsl:with-param name="lang" select="$lang"/>
						</xsl:call-template>
						<xsl:text>:</xsl:text>
					</td>
					<td>
						<a href="{usercommunity/url}">
							<xsl:if test="$defactiveitem=1">
								<xsl:attribute name="target">_parent</xsl:attribute>
							</xsl:if>
							<xsl:call-template name="truncatewithoutanchor">
								<xsl:with-param name="texttotruncate" select="usercommunity/community"/>
							</xsl:call-template>
						</a>
					</td>
				</tr>
			</body>
		</html>
	</xsl:template>
	<xsl:template name="truncatewithoutanchor">
		<xsl:param name="texttotruncate"/>
		<xsl:choose>
			<xsl:when test="string-length($texttotruncate)&gt;15">
				<xsl:value-of select="concat(substring($texttotruncate,0,15),'...')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$texttotruncate"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_cmpUserCommunity.usercommunity@User Community">Title for User Community page.</key>
		<key name="psx.sys_cmpUserCommunity.usercommunity@Community">Community text appearing in User Status component.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
