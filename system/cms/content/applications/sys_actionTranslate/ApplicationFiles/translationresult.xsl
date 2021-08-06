<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="Content-Type" content="text/html; UTF-8"/>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translationresult@Rhythmyx - Translation Results'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<script language="javascript" src="../sys_resources/js/browser.js">;</script>
				<xsl:variable name="contentids">
					<xsl:for-each select="//Status">
						<xsl:value-of select="@translatedid"/>
						<xsl:if test="position()!=last()">;</xsl:if>
					</xsl:for-each>
				</xsl:variable>
				<script language="javascript">
		function refreshParent()
		{
			if(window.opener &amp;&amp; !window.opener.closed)
			{
				var obj = PSGetApplet(window.opener, "ContentExplorerApplet");
				if (obj != null)
				{
					obj.refresh("Parent",'<xsl:value-of select="$contentids"/>',"");
				}
				else
				{
					window.opener.location.href = window.opener.location.href;
				}
			}
			self.close();
		}
	</script>
			</head>
			<body onload="javascript:self.focus();">
				<table align="center" width="75%" border="0" cellspacing="1" cellpadding="0" class="headercell">
					<tr class="datacell1">
						<td colspan="2" align="center" class="headercellfont">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translationresult@Translation result status'"/>.
                        <xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
							<br/>
						</td>
					</tr>
					<tr class="headercell">
						<td align="left" class="datacell1font" width="25%">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translationresult@Content ID'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
						<td align="left" class="datacell1font">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translationresult@Status'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
					</tr>
					<xsl:for-each select="//Status">
						<tr class="datacell1">
							<td align="left" class="datacell1font">
								<xsl:value-of select="@contentid"/>
							</td>
							<td align="left" class="datacell1font">
								<xsl:value-of select="@status"/>
							</td>
						</tr>
					</xsl:for-each>
					<tr class="datacell1" border="0">
						<td align="left" class="datacell1font" colspan="2">&nbsp;
                                 </td>
					</tr>
					<tr class="datacell1" border="0">
						<td align="center" class="datacell1font" colspan="2">
							<input type="button" name="close" onclick="javascript:refreshParent();">
								<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
								<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
							</input>
						</td>
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
	<psxi18n:lookupkeys>
		<key name="psx.sys_psxRelationshipSupport.translationresult@Rhythmyx - Translation Results">Title for the translation results dialog box.</key>
		<key name="psx.sys_psxRelationshipSupport.translationresult@Translation result status">Heading shown for the translation results.</key>
		<key name="psx.sys_psxRelationshipSupport.translationresult@Content ID">Item content id heading.</key>
		<key name="psx.sys_psxRelationshipSupport.translationresult@Status">Item status heading.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
