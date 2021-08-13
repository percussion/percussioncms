<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:template name="generateErrorPage">
		<xsl:variable name="rxroot">
			<xsl:value-of select="PSXLogErrorSet/@rxroot"/>
		</xsl:variable>
		<xsl:variable name="sysimagepath" select="concat($rxroot, '/sys_resources/images/')"/>
		<xsl:variable name="rximagepath" select="concat($rxroot, '/rx_resources/images/en-us/')"/>
		<html>
			<head>
				<title>Rhythmyx - Error</title>
				<link rel="stylesheet" type="text/css" href="{concat($rxroot, '/sys_resources/css/templates.css')}"/>
				<link rel="stylesheet" type="text/css" href="{concat($rxroot, '/rx_resources/css/templates.css')}"/>
			</head>
			<body class="backgroundcolor" topmargin="5" leftmargin="5">
				<table width="100%" height="125" cellpadding="0" cellspacing="0" border="0">
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
													<img src="{concat($sysimagepath,'spacer.gif')}"/>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td height="1" class="backgroundcolor">
							<img src="{concat($sysimagepath,'spacer.gif')}" width="1" height="1" border="0" alt=""/>
						</td>
					</tr>
					<tr class="outerboxcell">
						<td>
							<table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
								<tr class="headercell">
									<td class="headercell2font">Processing Error: <xsl:value-of select="PSXLogErrorSet/@class"/>
									</td>
								</tr>
								<tr>
									<td>
										<table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
											<tr class="headercell">
												<td class="headercell2font">ID</td>
												<td class="headercell2font">Message</td>
											</tr>
											<xsl:for-each select="PSXLogErrorSet/Error">
												<tr class="headercell">
													<td width="10%" class="datacell1font">
														<xsl:value-of select="@id"/>
													</td>
													<td class="headererrorcell">
														<xsl:value-of select="."/>
													</td>
												</tr>
											</xsl:for-each>
										</table>
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
