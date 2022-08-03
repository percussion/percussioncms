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
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:output method="html" omit-xml-declaration="yes" encoding="UTF-8" />
	<xsl:variable name="lang" select="//@lang"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 4.0"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_welcome.login@Rhythmyx Login Redirect Page'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<meta http-equiv="Content-Type" content="text/html; UTF-8"/>
				<link rel="stylesheet" href="/sys_resources/css/rxcx.css" type="text/css" media="screen"/>
			</head>
			<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
				<xsl:attribute name="onload">document.location.href="<xsl:choose><xsl:when test="*/BounceTo!=''"><xsl:value-of select="*/BounceTo"/></xsl:when><xsl:otherwise><xsl:value-of select="*/caurl"/></xsl:otherwise></xsl:choose>";</xsl:attribute>
				<table class="RxLogin" cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td colspan="2">
							<table cellpadding="0" cellspacing="0" border="0" width="100%">
								<tr>
									<td width="25">
										<img height="25" src="{concat('/rx_resources/images/',$lang,'/','rhythmyx_login_topleft.gif')}" width="25"/>
									</td>
									<td class="rhythmyx_login_topbkgd">
										<img height="25" src="{concat('/rx_resources/images/',$lang,'/','blank-pixel.gif')}" width="25"/>
									</td>
									<td width="25">
										<img height="25" src="{concat('/rx_resources/images/',$lang,'/','rhythmyx_login_topright.gif')}" width="25"/>
									</td>
								</tr>
							</table>
						</td>
						<td class="RightShadow">
							<img src="{concat('/rx_resources/images/',$lang,'/','shadow-topright.gif')}" width="9" height="25"/>
						</td>
					</tr>
					<tr>
						<td colspan="2" class="BannerCell">
							<img height="50" src="{concat('/rx_resources/images/',$lang,'/','rhythmyx_login_banner.jpg')}" width="516">
								<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_welcome.communitylogin.alt@Rhythmyx Content Manager'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
							</img>
						</td>
						<td class="RightShadow">&nbsp;</td>
					</tr>
					<tr class="whiteBKGD" valign="middle">
						<td class="whiteBKGD" colspan="2" align="center">
							<xsl:apply-templates select="*/dummy"/>
							<span class="SelectText">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_welcome.login@Loading'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</span>...
						</td>
						<td class="RightShadow">&nbsp;</td>
					</tr>
					<tr>
						<td colspan="2" class="BottomShadow">&nbsp;</td>
						<td>
							<img src="{concat('/rx_resources/images/',$lang,'/','shadow-bottomright.gif')}" width="9" height="9"/>
						</td>
					</tr>
				</table>
				<div class="copyright">&copy; Copyright Percussion Software @COPYRIGHTYEAR@</div>
			</body>
		</html>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_welcome.login@Rhythmyx Login Redirect Page">The title for login redirect page</key>
		<key name="psx.sys_welcome.login@Loading">Page loading message appears on the page during redirection process</key>
		<key name="psx.sys_welcome.login.alt@Rhythmyx Content Manager">Alt text for Rhythmyx Content Manager image on login redirect page.</key>
		<key name="psx.sys_welcome.login.alt@Welcome to Rhythmyx">Alt text for Welcome to Rhythmyx image on login redirect page.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
