<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:output method="html" omit-xml-declaration="yes"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:template match="/">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<title>Redirecting to action page...</title>
	            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
	            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
	            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
			</head>
			<body>
            <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0" class="headercell">
               <tr  class="outerboxcell">
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
            <script >
               window.location.href = "../ui/actionpage/panel?sys_contentid=" + <xsl:value-of select="//@contentid"/> + "&amp;sys_folderid=" + '<xsl:value-of select="//@folderid"/>' + "&amp;sys_siteid=" + '<xsl:value-of select="//@siteid"/>'
            </script>
			</body>
		</html>
   </xsl:template>
</xsl:stylesheet>

