<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="itemcount" select="count(//item)"/>
	<xsl:variable name="contentids">
		<xsl:for-each select="//item">
			<xsl:text>&amp;sys_contentid=</xsl:text>
			<xsl:value-of select="sys_contentid"/>
		</xsl:for-each>
	</xsl:variable>
	<xsl:variable name="relateditemlookup" select="document(concat(//relateditemlookupurl,$contentids))"/>
	<xsl:variable name="locales" select="document(//localeurl)"/>
	<xsl:variable name="relatedchilditemlookup" select="document(concat(//relatedchilditemlookupurl,$contentids))"/>
	<xsl:variable name="commonlocales" select="document(concat(//commonlocalesurl,$contentids))"/>
	<xsl:variable name="itemlocale" select="//itemlocale"/>
	<xsl:variable name="this" select="/"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="Content-Type" content="text/html; UTF-8"/>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@Rhythmyx - New Translation Version Properties'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<script src="../sys_psxRelationshipSupport/translateinit.js" language="javascript">;</script>
				<script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
				<script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
				<script language="javascript">
					function alertBeforeTranslate()
					{
						alert(LocalizedMessage("translation_may_take_time","<xsl:value-of select="$lang"/>"));
						document.translate.submit();
					}
				</script>

			</head>
			<body onload="javascript:self.focus();">
				<form name="translate" method="post" action="createtranslations.html">
					<xsl:for-each select="//item">
						<input type="hidden" name="sys_contentid" value="{sys_contentid}"/>
						<input type="hidden" name="sys_revision" value="{sys_revision}"/>
						<input type="hidden" name="ceurl" value="{ceurl}"/>
						<input type="hidden" name="sys_relationshiptype" value="{sys_relationshiptype}"/>
					</xsl:for-each>
					<table align="center" width="75%" border="0" cellspacing="1" cellpadding="0" class="headercell">
						<tr class="headercell">
							<td colspan="2" align="left" class="headercellfont">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@Translation Parameters'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
						</tr>
						<tr class="headercell">
							<td colspan="2" align="left" class="headercellfont">&nbsp;</td>
						</tr>
						<xsl:choose>
							<xsl:when test="$relatedchilditemlookup//item/title!=''">
								<tr class="datacell1" border="0">
									<td align="left" class="datacell1font" colspan="2">&nbsp;
                                 </td>
								</tr>
								<tr class="datacell1">
									<td align="left" class="datacell1font" colspan="2">
										<xsl:if test="$itemcount&gt;1">
											<xsl:call-template name="getLocaleString">
												<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@One or more of the selected items for translation are translated versions of other items'"/>
												<xsl:with-param name="lang" select="$lang"/>
											</xsl:call-template>.&nbsp;
                              </xsl:if>
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@You cannot create a Translation item from another Translation item'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>.&nbsp;
                     </td>
								</tr>
								<xsl:if test="$itemcount=1">
									<tr class="datacell1">
										<td align="left" class="datacell1font" colspan="2">
											<xsl:call-template name="getLocaleString">
												<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@Parent item'"/>
												<xsl:with-param name="lang" select="$lang"/>
											</xsl:call-template>:
                              <xsl:value-of select="$relatedchilditemlookup//item/title"/>(<xsl:value-of select="$relatedchilditemlookup//item/id"/>)
                     </td>
									</tr>
								</xsl:if>
								<tr class="datacell1" border="0">
									<td align="left" class="datacell1font" colspan="2">&nbsp;
                                 </td>
								</tr>
								<xsl:call-template name="displayclosebutton"/>
							</xsl:when>
							<xsl:when test="//checkoutuser!=''">
								<tr class="datacell1" border="0">
									<td align="left" class="datacell1font" colspan="2">&nbsp;
                                 </td>
								</tr>
								<tr class="datacell1">
									<td align="left" class="datacell1font" colspan="2">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@This content item is in the checked out state'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>.&nbsp;
                              <xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@Please check in the content item before creating a Translation item'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>.
                     </td>
								</tr>
								<tr class="datacell1" border="0">
									<td align="left" class="datacell1font" colspan="2">&nbsp;
                                 </td>
								</tr>
								<xsl:call-template name="displayclosebutton"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if test="$itemcount=1">
									<tr class="datacell1">
										<td align="left" class="datacell1font">
											<xsl:call-template name="getLocaleString">
												<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@Content Title(Id)'"/>
												<xsl:with-param name="lang" select="$lang"/>
											</xsl:call-template>
										</td>
										<td align="left" class="datacell1font">
											<xsl:value-of select="//contenttitle"/>(<xsl:value-of select="//sys_contentid"/>)
                     </td>
									</tr>
									<tr class="datacell1">
										<td align="left" valign="middle" class="datacell1font">
											<xsl:call-template name="getLocaleString">
												<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@Previous Translations'"/>
												<xsl:with-param name="lang" select="$lang"/>
											</xsl:call-template>
										</td>
										<td align="left" valign="middle" class="datacell1font">
											<xsl:choose>
												<xsl:when test="count($relateditemlookup//item/langstring[not(.='')]) &gt; 0">
													<xsl:for-each select="$locales//PSXEntry[(Value=$relateditemlookup//item/langstring) and (not(Value=$itemlocale))]">
														<xsl:value-of select="PSXDisplayText"/>
														<xsl:if test="position()!=last()">
															<br/>
														</xsl:if>
													</xsl:for-each>
												</xsl:when>
												<xsl:otherwise>
													<xsl:call-template name="getLocaleString">
														<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@None'"/>
														<xsl:with-param name="lang" select="$lang"/>
													</xsl:call-template>
												</xsl:otherwise>
											</xsl:choose>
										</td>
									</tr>
								</xsl:if>
								<xsl:choose>
									<xsl:when test="count($commonlocales//locale) &lt; 1">
										<tr class="headercell">
											<td colspan="2" align="center" class="datacell1font">
												<xsl:choose>
													<xsl:when test="$itemcount=1">
														<xsl:call-template name="getLocaleString">
															<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@No Translation Locales available'"/>
															<xsl:with-param name="lang" select="$lang"/>
														</xsl:call-template>
													</xsl:when>
													<xsl:otherwise>
														<xsl:call-template name="getLocaleString">
															<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@No Common Translation Locales left'"/>
															<xsl:with-param name="lang" select="$lang"/>
														</xsl:call-template>
													</xsl:otherwise>
												</xsl:choose>
											</td>
										</tr>
										<tr class="datacell1" border="0">
											<td align="left" class="datacell1font" colspan="2">&nbsp;
                                       </td>
										</tr>
										<xsl:call-template name="displayclosebutton"/>
									</xsl:when>
									<xsl:otherwise>
										<tr class="datacell1">
											<td align="left" class="datacell1font">
												<xsl:call-template name="getMnemonicLocaleString">
													<xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@Locale'"/>
						                     <xsl:with-param name="mnemonickey" select="'L'"/>
													<xsl:with-param name="lang" select="$lang"/>
												</xsl:call-template>:

                     </td>
											<td class="datacell1font">
												<select name="sys_lang">
													<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate.mnemonic.Locale@L'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
													<xsl:for-each select="$commonlocales//locale">
														<option value="{@name}">
															<xsl:value-of select="@displaytext"/>
														</option>
													</xsl:for-each>
												</select>
											</td>
										</tr>
										<tr class="datacell1" border="0">
											<td align="left" class="datacell1font" colspan="2">&nbsp;
                                 </td>
										</tr>
										<tr class="datacell1" border="0">
											<td align="center" class="datacell1font" colspan="2">
												<input type="button" name="translate" onclick="javascript:alertBeforeTranslate();">
													<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate.mnemonic.Create@R'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
													<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_psxRelationshipSupport.translate@Create'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
												</input>&nbsp;
                                    <input type="button" name="close" onclick="javascript:window.close();">
													<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
													<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
												</input>
											</td>
										</tr>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
					</table>
				</form>
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
	<xsl:template name="displayclosebutton">
		<tr class="datacell1" border="0">
			<td align="center" class="datacell1font" colspan="2">
				<input type="button" name="close" onclick="javascript:window.close();">
					<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				</input>
			</td>
		</tr>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_psxRelationshipSupport.translate@Rhythmyx - New Translation Version Properties">Title for the create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@Translation Parameters">Header for create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@Create">Button label in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@Content Title(Id)">Content Title label in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@Previous Translations">Previsous translations label in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@None">None shows up when no previous translated items found in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@No Translation Locales available">Message shows up when no locales left for translation.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@Locale">Locale label in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@One or more of the selected items for translation are translated versions of other items">Message appears in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@You cannot create a Translation item from another Translation item">Message appears in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@Parent item">Parent item label in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@One or more content items are in the checked out state">Message appears in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@Please check in the items before creating Translations">Message appears in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate@No Common Translation Locales left">Message appears in create translation pop-up.</key>
		<key name="psx.sys_psxRelationshipSupport.translate.mnemonic.Locale@L">Mnemonic for label &quot;Locale&quot;</key>
		<key name="psx.sys_psxRelationshipSupport.translate.mnemonic.Create@R">Mnemonic for label &quot;Create&quot;</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
