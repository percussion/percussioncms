<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "./../../DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "./../../DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "./../../DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Redirect Page</title>
				<script language="javascript">
					function redirectServerActionUrl()
					{
                 			window.location.href = "<xsl:value-of select="//@sys_serveractionurl"/>";
					}
				</script>
			</head>
			<body>
            <xsl:if test="not(//@sys_serveractionurl = '')">
               <xsl:attribute name="onload">javascript:redirectServerActionUrl();</xsl:attribute>
            </xsl:if>
				<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0" class="headercell">
					<tr class="outerboxcell">
						<td align="center" valign="middle" class="outerboxcellfont">
                     <xsl:choose>
                        <xsl:when test="not(//@sys_serveractionurl = '')">
                           <xsl:call-template name="getLocaleString">
                              <xsl:with-param name="key" select="'psx.generic@Your request is being processed'"/>
                              <xsl:with-param name="lang" select="$lang"/>
                           </xsl:call-template>. 
                           <xsl:call-template name="getLocaleString">
                              <xsl:with-param name="key" select="'psx.generic@Please wait a moment'"/>
                              <xsl:with-param name="lang" select="$lang"/>
                           </xsl:call-template>...
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:call-template name="getLocaleString">
                              <xsl:with-param name="key" select="'psx.sys_cxSupport.serveractionurlredirect@The url for the server action is empty. Action execution aborted.'"/>
                              <xsl:with-param name="lang" select="$lang"/>
                           </xsl:call-template>. 
                        </xsl:otherwise>
                     </xsl:choose>
                  </td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cxSupport.serveractionurlredirect@The url for the server action is empty. Action execution aborted.">The message displayed when the server action url is empty.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
