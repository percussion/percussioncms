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
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:output method="html" omit-xml-declaration="yes" encoding="UTF-8" />
	<xsl:variable name="lang1">
		<xsl:choose>
			<xsl:when test="//@lang"><xsl:value-of select="//@lang"/></xsl:when>
			<xsl:otherwise><xsl:text>en-us</xsl:text></xsl:otherwise> <!-- default value -->
		</xsl:choose>
	</xsl:variable>
	<xsl:template match="redirect" mode="redirect">
		<xsl:param name="lang"/>
		<xsl:variable name="rximagepath">
			<xsl:choose>
				<xsl:when test="$lang and $lang!=''">
					<xsl:value-of select="concat('/rx_resources/images/',$lang,'/')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>/rx_resources/images/en-us/</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<html>
			<head>
				<title>Processing Request</title>
				<link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
                <link rel="stylesheet" type="text/css" href="{concat('/rx_resources/css/',$lang1,'/templates.css')}"/>
				<meta HTTP-EQUIV="Refresh">
					<xsl:attribute name="CONTENT">0.5; URL=<xsl:value-of select="BounceTo"/></xsl:attribute>
				</meta>
				<script>
				    	function bounce()
				    	{
				    	   window.location.href = "<xsl:value-of select="BounceTo"/>";
				    	}
            		</script>
			</head>
			<body class="backgroundcolor" topmargin="0" leftmargin="0" onload="bounce()">
				<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td height="75">
							<table width="100%" height="75" cellpadding="0" cellspacing="0" border="0">
								<tr class="bannerbackground">
									<td width="315" valign="top" align="left">
										<img src="{concat($rximagepath,'banner_longlogo.jpg')}" width="640" height="75" border="0" alt="Rhythmyx Content Manager"/>
									</td>
									<td height="75" align="left" class="tabs" width="100%">
										<table width="100%" border="0" cellspacing="0" cellpadding="0" height="75" background="{concat($rximagepath,'banner_bg_noline.gif')}">
											<tr>
												<td align="left" valign="bottom">
													<img src="/sys_resources/images/spacer.gif"/>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr class="outerboxcell">
						<td height="100%">
							<table width="100%" cellpadding="0" cellspacing="20" border="0">
								<tr class="outerboxcell">
									<td class="outerboxcellfont" align="center" valign="middle">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.generic@Your request is being processed'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>. 
                              <xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.generic@Please wait a moment'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>...
                           </td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
