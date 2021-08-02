<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC="-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="rev" select="//@sys_revision"/>
	<xsl:variable name="contentid" select="//@sys_contentid"/>
	<xsl:variable name="closeWindow" select="//closeWindow"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:template match="/">
		<xsl:variable name="rximagepath">
			<xsl:choose>
				<xsl:when test="$lang and $lang!=''">
					<xsl:text>../rx_resources/images/en-us/</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat('../rx_resources/images/',$lang,'/')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<html>
			<head>
				<title>Processing Request</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<script language="javascript" src="../sys_resources/js/browser.js">;</script>
				<script language="javascript" src="../sys_resources/js/href.js">;</script>
				<script language="javascript">
				    	function bounce()
				    	{
				    		if (window.opener != null)
				    		{
				    			var hint = "<xsl:value-of select='//refreshHint'/>";
				    			var contentids = '<xsl:value-of select="$contentid"/>';
				    			refreshCxApplet(window.opener, hint, contentids, "");
				    			
				    		}
				    		
						var closeWin = 	"<xsl:value-of select='$closeWindow'/>"
						if(closeWin == "no")
						{
							var loc = getReturnURL(window.opener.ps_returnUrl,'<xsl:value-of select="$contentid"/>','<xsl:value-of select="$rev"/>');
							self.location.href = loc; 
						}
						else
						{
							self.close();
						}					     

					}
				<![CDATA[
				
				function getReturnURL(oldHref, contentid, revision)
				{
				   var h = PSHref2Hash(oldHref);
				   var newhash = new Array();
				   newhash["sys_command"] = h["sys_command"];
				   newhash["sys_view"] = h["sys_view"];
				   if(revision.length != 0)
				   {
				   	newhash["sys_revision"] = revision;
				   }
				   else
				   {
				   	newhash["sys_revision"] = '1';
				   }
				   newhash["sys_contentid"] = contentid;
				   
				   return PSHash2Href(newhash, oldHref);
				  
				
				}
				
				]]>					    	
				    	
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
													<img src="../sys_resources/images/spacer.gif"/>
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
