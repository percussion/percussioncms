<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:template match="/">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<script language="javascript" src="../sys_resources/js/href.js">;</script>
				<meta name="generator" content="Percussion Rhythmyx"/>
				<title>Redirect page</title>
				<script language="javascript" src="../sys_resources/js/browser.js">;</script>
				<script language="javascript" >
				function refreshParent()
				{
 					window.opener.location.href = window.opener.location.href;
 					self.close(); 				
 				}
				</script>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
			</head>
			<body onload="refreshParent();">
				<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0" class="headercell">
					<tr class="outerboxcell">
						<td align="center" valign="middle" class="outerboxcellfont">
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
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
