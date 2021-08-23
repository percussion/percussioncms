<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XML Spy v4.3 U (http://www.xmlspy.com) by matt boucher (home office) -->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="workflowfactions" select="document(//@workflowactionsurl)"/>
	<xsl:variable name="transitionid" select="//transitionid"/>
	<xsl:variable name="actionLink" select="$workflowfactions//ActionLink[Param[@name='sys_transitionid']=$transitionid]"/>
	<xsl:variable name="showAdhoc">
		<xsl:if test="$actionLink/AssignedRoles/Role[@adhocType != '0']">yes</xsl:if>
	</xsl:variable>
	<xsl:variable name="psredirect">
		<xsl:choose>
			<xsl:when test="//fromaa='yes'">../sys_uiSupport/redirect.html</xsl:when>
			<xsl:otherwise>../sys_cxSupport/redirectrefresh.html</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition@Rhythmyx - Workflow'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link href="../sys_resources/css/templates.css" type="text/css" rel="stylesheet"/>
				<link href="../rx_resources/css/templates.css" type="text/css" rel="stylesheet"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<script language="javascript" src="../sys_resources/js/browser.js">;</script>
				<script src="../sys_resources/js/AddFormParameters.js" >;</script>
				<script src="../sys_resources/js/formValidation.js" >;</script>
				<script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
				<script language="javascript" src="../rx_resources/js/globalErrorMessages.js">;</script>
				<script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
				<script language="javascript">
					function postDirectly()
					{
						document.wfcomment.submit();
					}
				</script>
			</head>
			<body class="backgroundcolor" leftmargin="5" topmargin="5">
				<xsl:if test="//commentRequired = 'hide' and $showAdhoc != 'yes'">
					<xsl:attribute name="onload">javascript:postDirectly();</xsl:attribute>
				</xsl:if>
				<form name="wfcomment" action="{concat($workflowfactions//@workflowactionurl,'?sys_contentid=',//contentid,'&amp;sys_command=workflow&amp;WFAction=',$actionLink/Param[@name='WFAction'],'&amp;psredirect=',$psredirect,'%3Fsys_contentid%3D', //contentid,'%26refreshHint%3DSelected')}" method="post">
					<input type="hidden" name="fromRoles">
						<xsl:attribute name="value"><xsl:apply-templates select="$actionLink/AssignedRoles/Role" mode="buildlist"/></xsl:attribute>
					</input>
					<input type="hidden" name="sys_contentid" value="{//contentid}"/>
					<table cellspacing="1" cellpadding="4" border="0" width="250" height="335">
						<tr>
							<td class="outerboxcell" valign="top" align="center">
								<table cellspacing="0" cellpadding="4" width="100%" border="0">
									<tr>
										<td>
											<xsl:choose>
												<xsl:when test="//commentRequired = 'hide'">
													<input type="hidden" name="commenttext" value=""/>
												</xsl:when>
												<xsl:otherwise>
													<table cellspacing="0" cellpadding="0" width="100%" border="0">
														<tr>
															<td class="outerboxcellfont" align="center">
																<label for="commenttext">
																	<xsl:attribute name="accessKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition.mnemonic.Workflow Comments@W'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																	<xsl:call-template name="getMnemonicLocaleString">
																		<xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition@Workflow Comments'"/>
																		<xsl:with-param name="mnemonickey" select="'psx.sys_uiSupport.wfTransition.mnemonic.Workflow Comments@W'"/>
																		<xsl:with-param name="lang" select="$lang"/>
																	</xsl:call-template>
																	<xsl:if test="//commentRequired = 'yes'">
																		<span class="reqfieldfont">*</span>
																	</xsl:if>&#160;&#160;
                                                    <img height="16" alt="Add Comments Below" src="../sys_resources/images/singlecomment.gif" width="16" border="0">
																		<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition.alt@Add Comments Below'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																	</img>
																</label>
															</td>
														</tr>
														<tr>
															<td class="datacell1font" align="center">
																<textarea class="datadisplay" name="commenttext" rows="6" wrap="soft">
																	<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition.mnemonic.Workflow Comments@W'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																	<xsl:attribute name="cols"><xsl:choose><xsl:when test="contains(//UserAgent, 'MSIE')">35</xsl:when><xsl:otherwise>24</xsl:otherwise></xsl:choose></xsl:attribute>
																</textarea>
															</td>
														</tr>
													</table>
													<br/>
												</xsl:otherwise>
											</xsl:choose>
											<xsl:if test="$showAdhoc = 'yes'">
												<table cellspacing="0" cellpadding="0" width="100%" border="0">
													<tr>
														<td class="outerboxcellfont" align="center">
															<label for="sys_wfAdhocUserList">
																<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition.mnemonic.Ad hoc Assignees@A'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																<xsl:call-template name="getMnemonicLocaleString">
																	<xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition@Ad hoc Assignees'"/>
																	<xsl:with-param name="mnemonickey" select="'psx.sys_uiSupport.wfTransition.mnemonic.Ad hoc Assignees@A'"/>
																	<xsl:with-param name="lang" select="$lang"/>
																</xsl:call-template>
															</label>
														</td>
													</tr>
													<tr>
														<td class="datacell1font" align="center">
															<textarea class="datadisplay" name="sys_wfAdhocUserList" rows="6" wrap="soft">
																<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition.mnemonic.Ad hoc Assignees@A'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																<xsl:attribute name="cols"><xsl:choose><xsl:when test="contains(//UserAgent, 'MSIE')">35</xsl:when><xsl:otherwise>24</xsl:otherwise></xsl:choose></xsl:attribute>
															</textarea>
														</td>
													</tr>
													<tr>
														<td align="center">
															<a onclick="javascript:showUserSearch2(document.wfcomment.sys_wfAdhocUserList, 'UserSearchWindow')" href="javascript:void(0)">
																<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition.mnemonic.Search@S'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																<img border="0" alt="Click here to search for users" src="{concat('../rx_resources/images/',$lang,'/people_search.gif')}">
																	<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_uiSupport.wfTransition.alt@Click here to search for users'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																</img>
															</a>
														</td>
													</tr>
												</table>
												<br/>
											</xsl:if>
											<script language="javascript">
											function save_onclick()
											{
												<xsl:if test="//commentRequired = 'yes'">
													if (document.wfcomment.commenttext.value=='')
													{
														alert(LocalizedMessage("workflow_comment_required","<xsl:value-of select="$lang"/>"));
														return false;
													}
												</xsl:if>
												
                                    if (document.wfcomment.commenttext.value.length > 255)
                                    {
                                       alert(LocalizedMessage("workflow_comment_cannot_exceed_255_chars","<xsl:value-of select="$lang"/>"));
                                       return false;
                                    }

                                    document.wfcomment.submit();
												
												if (window.parent != null &amp;&amp;
												    window.parent.refreshurl != null)
												{
												   // Refresh caller if there is one and the url is known
												   window.parent.location.href = window.parent.refreshurl;
												}
   
												return true;
											}
											</script>
										</td>
									</tr>
									<tr>
										<td valign="bottom" align="center">
											<input type="button" value="{//transitionName}" onclick="return save_onclick()"/>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</form>
				<form name="UserSearchForm" action="../sys_ServerUserRoleSearch/rolelist.html?sys_command=GetRoles" method="post" target="UserSearchWindow">
					<input type="hidden" name="fromRoles">
						<xsl:attribute name="value"><xsl:apply-templates select="$actionLink/AssignedRoles/Role" mode="buildlist"/></xsl:attribute>
					</input>
					<input type="hidden" name="sys_contentid" value="{//contentid}"/>
				</form>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="*" mode="buildlist">
		<xsl:value-of select="."/>
		<xsl:text>:</xsl:text>
		<xsl:value-of select="@adhocType"/>
		<xsl:text>;</xsl:text>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_uiSupport.wfTransition@Rhythmyx - Workflow">Title for Workflow comments dialog box opens up when clicked on a workflow action in Active Menu. in </key>
		<key name="psx.sys_uiSupport.wfTransition@Workflow Comments">Label for work flow comments text area.</key>
		<key name="psx.sys_uiSupport.wfTransition.alt@Add Comments Below">Alt text for comments image.</key>
		<key name="psx.sys_uiSupport.wfTransition@Ad hoc Assignees">Label for Ad hoc Assignees text area.</key>
		<key name="psx.sys_uiSupport.wfTransition.alt@Click here to search for users">Alt text for Ad hoc Assignees search image.</key>
		<key name="psx.sys_uiSupport.wfTransition.mnemonic.Workflow Comments@W">Mnemonic for label &quot;Workflow Comments&quot;.</key>
		<key name="psx.sys_uiSupport.wfTransition.mnemonic.Ad hoc Assignees@A">Mnemonic for label &quot;Ad hoc Assignees&quot;.</key>
		<key name="psx.sys_uiSupport.wfTransition.mnemonic.Search@S">Mnemonic for label &quot;Search&quot;.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
