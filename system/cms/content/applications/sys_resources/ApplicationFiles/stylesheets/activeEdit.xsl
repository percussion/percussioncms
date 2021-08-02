<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<!-- $ Id: $ -->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/sys_Templates.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/customControlImports.xsl"/>
	<xsl:import href="file:rx_resources/stylesheets/rx_Templates.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/StatusBar.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/ActionList.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/relatedcontentctrl.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/PreviewBar.xsl"/>
	<xsl:import href="file:sys_resources/stylesheets/ce_globals.xsl"/>
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>
	<xsl:variable name="systemLibrary" select="'file:sys_resources/stylesheets/sys_Templates.xsl'"/>
	<xsl:variable name="systemLibraryDoc" select="document($systemLibrary)"/>
	<xsl:variable name="userLibrary" select="'file:rx_resources/stylesheets/rx_Templates.xsl'"/>
	<xsl:variable name="userLibraryDoc" select="document($userLibrary)"/>
	<xsl:variable name="customControlImportsDoc" select="document('../sys_resources/stylesheets/customControlImports.xsl')"/>
   <xsl:variable name="lang" select="/*/UserStatus/@xml:lang"/>
   <xsl:variable name="EditLiveDynamicName" select="'PSEditLiveDynamic'"/>
	<xsl:template match="/">
		<xsl:apply-templates select="ContentEditor"/>
	</xsl:template>
	<xsl:template match="ContentEditor">
		<xsl:variable name="bannerinclude" select="document(/*/SectionLinkList/SectionLink[@name='bannerincludeurl'])/*/url"/>
		<xsl:variable name="userstatusinclude" select="document(/*/SectionLinkList/SectionLink[@name='userstatusincludeurl'])/*/url"/>
		<xsl:variable name="helpinclude" select="document(/*/SectionLinkList/SectionLink[@name='helpincludeurl'])/*/url"/>
		<xsl:variable name="sysview" select="ItemContent/DisplayField/Control[@paramName='sys_currentview']/Value"/>
		<xsl:variable name="syscontentid" select="Workflow/@contentId"/>
		<xsl:variable name="sysrevision" select="Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>
		<xsl:variable name="syspageid" select="/*/ActionLinkList/ActionLink/Param[@name='sys_pageid']"/>
		<xsl:variable name="hasWebImageFx" select="//ControlNameSet[ControlName='sys_webImageFX']"/>
		<xsl:variable name="hasEditLive" select="//ControlNameSet[ControlName='sys_EditLive']"/>
      <xsl:variable name="hasEditLiveDynamic" select="//ControlNameSet[ControlName='sys_EditLiveDynamic']"/>
		<html>
			<head>
			<script language="javascript" src="../tmx/tmx.jsp?sys_lang={$lang}">;</script>
				<script src="/cm/jslib/profiles/3x/jquery/jquery-3.6.0.js"></script>
				<script src="/cm/jslib/profiles/3x/jquery/jquery-migrate-3.3.2.js"></script>
				<script src="../sys_resources/js/browser.js">;</script>
				<script src="../sys_resources/js/href.js">;</script>
			   <script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
			   <script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
			   <script src="../sys_resources/js/AddFormParameters.js">;</script>
			   <script src="../sys_resources/js/formValidation.js">;</script>
			   <script src="../sys_resources/js/formChangeCheck.js">;</script>
			   <xsl:if test="$hasWebImageFx">
			      <script src="../sys_resources/js/href.js">;</script>
			      <script src="../rx_resources/webimagefx/rx_wifx.js">;</script>
			   </xsl:if>
			   <xsl:variable name="scripttags">
			   	<xsl:apply-templates select="ControlNameSet/ControlName" mode="scriptfiles"/>
			   </xsl:variable>
  			   <xsl:variable name="styletags">
  			   	 <xsl:apply-templates select="ControlNameSet/ControlName" mode="stylefiles"/>
			   </xsl:variable>
			   <xsl:call-template name="createControlScriptTags">
			   	<xsl:with-param name="scripttags" select="$scripttags"/>
			   </xsl:call-template>
			   <xsl:call-template name="createControlStyleTags">
			   	<xsl:with-param name="styletags" select="$styletags"/>
			   </xsl:call-template>
			   <script language="Javascript">
      		  var hasEditLiveControls = false;
        		</script>
            <xsl:if test="$hasEditLive or $hasEditLiveDynamic">
				<script language="Javascript"><![CDATA[
      		  hasEditLiveControls = true;
         		]]></script>
            </xsl:if>
	    <xsl:variable name="escapedTitle">
	       <xsl:call-template name="replace-string">
		  <xsl:with-param name="text" select="Workflow/ContentStatus/Title" />
		  <xsl:with-param name="replace" select='"&#39;"' />
		  <xsl:with-param name="with" select='"\&#39;"' />
	       </xsl:call-template>
	    </xsl:variable>
		<script language="Javascript"><![CDATA[
			var contentEditor = "yes";
			var canSubmit = true;
			if(window.opener != null)
			{
				if(window.opener.ps_CloseMe == true)
				{
					window.opener.ps_CloseMe = false;
					if(window.location.href.indexOf("sys_cacheid=") == -1)
					{
						if(window.opener.ps_updateFlag == true)
						{
                     updateAaParent();
							// Refresh applet's selected view
							var rxApplet = PSGetApplet(window.opener, "ContentExplorerApplet");
							if(rxApplet != null && !is_safari)
							{
								rxApplet.refresh("Selected","]]><xsl:value-of select="$syscontentid"/><![CDATA[","");
							} 					
							else
							{
								refreshCxApplet(window.opener, "Selected","]]><xsl:value-of select="$syscontentid"/><![CDATA[","");
							}
						}
						window.opener.ps_updateFlag = false;
						window.close();
					}
				}
				else
				{
				   updateAaParent();
				}
			}
			
         function updateAaParent()
         {
            if(window.opener.ps_updateFlag && isOpenerActiveAssembly())
            {
               var cid = "]]><xsl:value-of select="$syscontentid"/><![CDATA[";
               if(window.name == "PsAaEditItem")
               {
                  window.opener.ps.aa.controller.updateAllFields(']]><xsl:value-of select="$escapedTitle"/><![CDATA[');
               }
               else if(window.name == "PsAaCreateItem" && cid.length>0)
               {
                  var data = window.opener.ps.aa.controller.newItemData;
                  if(data)
                  {
                  	window.opener.ps.aa.controller.postCreateItem(
                  	data.slotId,data.itemId,data.position,
                  	data.newData,cid);
                        window.opener.ps.aa.controller.newItemData = null;
                  }
               }
               else
               {
                  //it is assumed to be legacy aa and refresh the whole page to get the changes.
                  window.opener.location.href = window.opener.location.href;
               }
               window.opener.ps_updateFlag = false;
            }
         }
			
			function refreshWithHint()
			{
				var url = window.location.href;
				var hint = PSGetParam(url, 'refreshHint');
				if(hint == null || hint == "")
				   hint = "Selected";
				if(window.opener != null)
				{
					var h = PSHref2Hash(url);
					var refreshHint = h["refreshHint"];
					if(refreshHint == null || refreshHint == "")
				           refreshHint = "Selected";
					var rxApplet = PSGetApplet(window.opener, "ContentExplorerApplet");
					if (refreshHint != null && refreshHint != '' && rxApplet != null)
					{
						rxApplet.refresh(refreshHint,"]]><xsl:value-of select="$syscontentid"/><![CDATA[","");
					}
				}
				// Always refresh caller action pane
				reloadOpener(window);            
			}
			function resizeWindow()
			{
				if(isOpenerActiveAssembly())
					self.resizeTo(800, 400);
			}         
	 function psCustomControlIsDirty()
	 {
	    return (false]]><xsl:apply-templates select="/*/ItemContent/DisplayField[@displayType='sys_normal']" mode="psxcontrol-customcontrol-isdirty"/><![CDATA[);
         }
		]]></script>
				<xsl:variable name="thetitle">
					<xsl:choose>
						<xsl:when test="$sysview = 'sys_ItemMeta'">
							<xsl:text>Properties</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="@commandName = 'edit'">
									<xsl:choose>
										<xsl:when test="$syspageid != 0">
											<xsl:text>Edit Child</xsl:text>
										</xsl:when>
										<xsl:otherwise>Edit Content</xsl:otherwise>
									</xsl:choose>
								</xsl:when>
								<xsl:when test="@commandName = 'preview'">
									<xsl:text>View Content</xsl:text>
								</xsl:when>
								<xsl:otherwise>Edit Content</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.contenteditor.activeedit@Rhythmyx'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
               		-
               		<xsl:value-of select="Workflow/ContentStatus/Title"/>
               		-
               <xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="concat('psx.contenteditor.activeedit@',$thetitle)"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<!-- @@REP WITH WEP I18N FUNC@@ -->
			</head>
			<body class="datacell1" topmargin="5" leftmargin="5">
				<!-- provide a hook for controls to get script to run when page is loaded -->
				<xsl:attribute name="onLoad"><xsl:if test="$hasWebImageFx"><xsl:text>wifxLoadImage(); </xsl:text></xsl:if><xsl:apply-templates select="/*/ItemContent" mode="psxcontrol-body-onload"/><xsl:text>setTimeout('ps_getInitialChecksum(document.forms[0])',2000); setTimeout('ps_noCloseOnSave()',2000); refreshWithHint(); resizeWindow();</xsl:text></xsl:attribute>
				<table width="100%" border="0" cellpadding="0" cellspacing="1" class="outerboxcell">
					<tr>
						<td width="100%" height="100%" valign="top">
							<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
								<xsl:if test="(@commandName='edit' or @commandName='preview') and not($syspageid=0 and ItemContent[@newDocument='yes'])">
									<tr>
										<td>
											<xsl:choose>
												<xsl:when test="$sysview='sys_All'">
													<xsl:choose>
														<xsl:when test="$syspageid&gt;0">
															<a>
																<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.activeedit.mnemonic.Return to parent@R'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																<xsl:attribute name="href"><xsl:value-of select="concat(@submitHref,'?sys_contentid=',$syscontentid,'&amp;sys_revision=',$sysrevision,'&amp;sys_pageid=0&amp;sys_command=',@commandName,'&amp;sys_view=sys_All')"/></xsl:attribute>
																<xsl:call-template name="getLocaleString">
																	<xsl:with-param name="key" select="'psx.contenteditor.activeedit@Return to parent'"/>
																	<xsl:with-param name="lang" select="$lang"/>
																</xsl:call-template>
															</a>&nbsp;&nbsp;
                                       </xsl:when>
														<xsl:otherwise>
															<font class="outerboxcellfont">
																<xsl:call-template name="getLocaleString">
																	<xsl:with-param name="key" select="'psx.contenteditor.activeedit@Content'"/>
																	<xsl:with-param name="lang" select="$lang"/>
																</xsl:call-template>
															</font>&nbsp;&nbsp;
                                             <a>
																<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.activeedit.mnemonic.Properties@P'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																<xsl:attribute name="href"><xsl:value-of select="concat(@submitHref,'?sys_contentid=',$syscontentid,'&amp;sys_revision=',$sysrevision,'&amp;sys_pageid=0&amp;sys_command=',@commandName,'&amp;sys_view=sys_ItemMeta')"/></xsl:attribute>
																<xsl:call-template name="getLocaleString">
																	<xsl:with-param name="key" select="'psx.contenteditor.activeedit@Properties'"/>
																	<xsl:with-param name="lang" select="$lang"/>
																</xsl:call-template>
															</a>&nbsp;&nbsp;
                                       </xsl:otherwise>
													</xsl:choose>
												</xsl:when>
												<xsl:when test="$sysview='sys_ItemMeta'">
													<a>
														<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.activeedit.mnemonic.Content@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
														<xsl:attribute name="href"><xsl:value-of select="concat(@submitHref,'?sys_contentid=',$syscontentid,'&amp;sys_revision=',$sysrevision,'&amp;sys_pageid=0&amp;sys_command=',@commandName,'&amp;sys_view=sys_All')"/></xsl:attribute>
														<xsl:call-template name="getLocaleString">
															<xsl:with-param name="key" select="'psx.contenteditor.activeedit@Content'"/>
															<xsl:with-param name="lang" select="$lang"/>
														</xsl:call-template>
													</a>&nbsp;&nbsp;<font class="outerboxcellfont">
														<xsl:call-template name="getLocaleString">
															<xsl:with-param name="key" select="'psx.contenteditor.activeedit@Properties'"/>
															<xsl:with-param name="lang" select="$lang"/>
														</xsl:call-template>
													</font>
												</xsl:when>
												<xsl:otherwise>
													<xsl:if test="$syspageid&gt;0">
														<a>
															<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.activeedit.mnemonic.Return to parent@R'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
															<xsl:attribute name="href"><xsl:value-of select="concat(@submitHref,'?sys_contentid=',$syscontentid,'&amp;sys_revision=',$sysrevision,'&amp;sys_pageid=0&amp;sys_command=',@commandName,'&amp;sys_view=sys_All')"/></xsl:attribute>
															<xsl:call-template name="getLocaleString">
																<xsl:with-param name="key" select="'psx.contenteditor.activeedit@Return to parent'"/>
																<xsl:with-param name="lang" select="$lang"/>
															</xsl:call-template>
														</a>&nbsp;&nbsp;
                                    </xsl:if>
												</xsl:otherwise>
											</xsl:choose>
										</td>
									</tr>
								</xsl:if>
								<xsl:choose>
									<xsl:when test="$sysview='sys_ItemMeta'">
										<xsl:if test="ItemContent[@newDocument='no']">
											<tr align="right" class="datacell1">
												<td class="outerboxcellfont">
                                       &nbsp;
                                    </td>
											</tr>
											<tr>
												<td class="l2">
													<!-- Status Bar Goes Here -->
													<xsl:comment>Start of Status Bar</xsl:comment>
													<xsl:if test="$ContentStatus">
														<xsl:apply-templates select="/ContentEditor/Workflow" mode="statusbar"/>
													</xsl:if>
													<!-- End Status Bar -->
													<xsl:comment>End of Status Bar </xsl:comment>
												</td>
											</tr>
										</xsl:if>
									</xsl:when>
									<xsl:otherwise>
										<xsl:call-template name="sys_GenericPageError"/>
										<tr>
											<td class="headercell2">
												<!-- ********** INSERT CONTENT HERE ********** -->
												<xsl:comment>Start of Content Block</xsl:comment>
												<form id="perc-content-form" method="post" action="{@submitHref}" name="EditForm" encType="multipart/form-data">
													<!-- provide a hook for controls to get script to run when page is submitted -->
													<!-- each template should generate JS that returns a true/false value -->
                                       				<xsl:attribute name="onSubmit"><xsl:text>addFormRedirect(document.forms['EditForm']); ps_setUpdateFlag(); return </xsl:text><xsl:if test="$hasWebImageFx"><xsl:text>wifxHandleSubmit</xsl:text></xsl:if><xsl:text>(_ignoreMultipleSubmit() &amp;&amp; canSubmit &amp;&amp; true</xsl:text><xsl:apply-templates select="/*/ItemContent" mode="psxcontrol-form-onsubmit"/><xsl:text>)</xsl:text></xsl:attribute>
													<table width="100%" border="0" cellspacing="5" cellpadding="0" summary="controls for editing metadata">
														<tr>
															<td colspan="2"/>
														</tr>
														<xsl:apply-templates select="ItemContent"/>
														<tr>
															<td colspan="2">&#160;</td>
														</tr>
														<!-- RELATED CONTENT -->
														<xsl:if test="ItemContent[@newDocument='no'] and ($sysview='sys_All' or $sysview='sys_Content')">
															<xsl:if test="Workflow/@contentId and /ContentEditor/@enableRelatedContent='yes' and $RelatedContent">
																<xsl:choose>
																	<xsl:when test="/ContentEditor/@mode='sys_edit'">
																	   <tr>
																	      <td colspan="2">
   																	      <div id="psRelatedContent">
													                        <table>
     																	            <tr>
            																			<td>
            																				<b>
            																					<span class="controlname">
            																						<xsl:call-template name="getLocaleString">
            																							<xsl:with-param name="key" select="'psx.contenteditor.activeedit@Related Content'"/>
            																							<xsl:with-param name="lang" select="$lang"/>
            																						</xsl:call-template>:
                                                                        </span>
            																				</b>
            																			</td>
                                                                     <td>
               																			<table border="0" width="760">
            																					<xsl:variable name="variants" select="document(SectionLinkList/SectionLink[@name='VariantListURL'])/*"/>
            																					<xsl:choose>
            																						<xsl:when test="count($variants//Variant) = 1 and $variants//Variant/@variantId = ''">
            																							<td class="groupbox" valign="top" align="left" colspan="2">
            																								<div class="datacellnoentriesfound">
            																									<xsl:call-template name="getLocaleString">
            																										<xsl:with-param name="key" select="'psx.contenteditor.activeedit@No Templates registered for this Content Type.'"/>
            																										<xsl:with-param name="lang" select="$lang"/>
            																									</xsl:call-template>
            																								</div>
            																							</td>
            																						</xsl:when>
            																						<xsl:otherwise>
            																							<td valign="top" align="left" colspan="2" class="groupbox">
            																								<div>
            																									<xsl:apply-templates select="$variants" mode="previewbar-edit">
            																										<xsl:with-param name="contentid" select="Workflow/@contentId"/>
            																										<xsl:with-param name="revision" select="Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>
            																									</xsl:apply-templates>
                                                    &nbsp;
                                                    <xsl:apply-templates select="document(SectionLinkList/SectionLink[@name='ContentSlotLookupURL'])/*" mode="relatedcontentctrl">
            																										<xsl:with-param name="relateddoc" select="document(SectionLinkList/SectionLink[@name='RelatedLookupURL'])"/>
            																										<xsl:with-param name="mode" select="@mode"/>
            																										<xsl:with-param name="editable" select="'no'"/>
            																									</xsl:apply-templates>
            																								</div>
            																							</td>
            																						</xsl:otherwise>
            																					</xsl:choose>
            																				</table>
      																			   </td>
   																		      </tr>
   																	      </table> 
   																	      </div>
   																	    </td>
																	   </tr>
																	</xsl:when>
                                                   <xsl:otherwise>
                                                      <tr>
																			<td align="center" colspan="2">
																				<xsl:variable name="cslookupbase" select="SectionLinkList/SectionLink[@name='ContentSlotLookupURL']"/>
																				<xsl:variable name="cslookup" select="concat($cslookupbase,'&amp;filter=true')"/>
																				<xsl:apply-templates select="document($cslookup)/*" mode="relatedcontentctrl">
																					<xsl:with-param name="relateddoc" select="document(SectionLinkList/SectionLink[@name='RelatedLookupURL'])"/>
																					<xsl:with-param name="mode" select="@mode"/>
																					<xsl:with-param name="editable" select="'no'"/>
																				</xsl:apply-templates>
																			</td>
                                                      </tr>
                                                   </xsl:otherwise>
																</xsl:choose>
															</xsl:if>
														</xsl:if>
													<xsl:apply-templates select="ActionLinkList" mode="addformparams"/>
													<input xmlns="" type="hidden" name="httpcaller" value=""/>
													<xsl:if test="not(//Control[@paramName='psredirect'])">
														<input xmlns="" type="hidden" name="psredirect" value=""/>
													</xsl:if>
													<input xmlns="" type="hidden" name="sys_contenttypeid">
														<xsl:attribute name="value"><xsl:value-of select="//ContentEditor/@contentTypeId"/></xsl:attribute>
													</input>
													<tr>
														<td align="center" colspan="2" class="headercell2">
															<xsl:comment>Action List goes here</xsl:comment>
															<xsl:apply-templates select="ActionLinkList" mode="actionlist"/>
						 &nbsp;<input type="button" onClick="ps_closeWithDirtyCheck();" class="nav_body">
																<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
																<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
															</input>
															<xsl:apply-templates select="ActionListFormParams" mode="actionlist"/>
														</td>
													</tr>
													</table>
												</form>
												<form name="inlinelinkssearch" encType="multipart/form-data" target="searchitems" method="post">
													<input xmlns="" type="hidden" name="inlinetext" value=""/>
													<input xmlns="" type="hidden" name="inlineslotid" value=""/>
													<input xmlns="" type="hidden" name="inlinetype" value=""/>
												</form>
												<form name="rxwordlauncher" target="wordlauncherwindow" method="post">
													<input xmlns="" type="hidden" name="wepbodyhtml" value=""/>
												</form>
												<!-- ********** END CONTENT HERE ********** -->
												<xsl:comment>End of Content Block</xsl:comment>
											</td>
										</tr>
									</xsl:otherwise>
								</xsl:choose>
								<tr>
									<td height="100%" class="headercell2">&nbsp;</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			<!--This code changes the update buttons label-->
				<script language="javascript"><![CDATA[
					if(isOpenerActiveAssembly())
					{
						var relcDiv = document.getElementById("psRelatedContent");
						if(relcDiv)
						{
						   relcDiv.style.position = "absolute";
						   relcDiv.style.visibility = "hidden";
						} 
					}
				]]></script>
				
			</body>
		</html>
	</xsl:template>
	<xsl:template match="ItemContent">
		<xsl:comment>ItemContent</xsl:comment>
		<xsl:apply-templates select="DisplayField"/>
	</xsl:template>
	<xsl:template match="DisplayField[@displayType='sys_hidden']">
		<tr>
			<td>
				<xsl:comment>Hidden Control</xsl:comment>
				<xsl:apply-templates select="Control" mode="psxcontrol-hidden"/>
			</td>
		</tr>
	</xsl:template>
	<!-- Treat sys_HiddenInput control also as hidden field-->
	<xsl:template match="DisplayField[Control[@name='sys_HiddenInput']]" priority="10">
		<tr>
			<td>
				<xsl:comment>Hidden Control</xsl:comment>
				<xsl:apply-templates select="Control" mode="psxcontrol-hidden"/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="DisplayField[@displayType='sys_normal']">
		<xsl:comment>Normal Control</xsl:comment>
		<tr>
			<td class="controlname">
				<!-- display an asterisk for required fields -->
				<xsl:if test="Control/@isRequired='yes'">*&#160;</xsl:if>
				<xsl:if test="DisplayLabel!=''">
					<xsl:variable name="keyval">
						<xsl:choose>
							<xsl:when test="DisplayLabel/@sourceType='sys_system'">
								<xsl:value-of select="concat('psx.ce.system.', Control/@paramName, '@', DisplayLabel)"/>
							</xsl:when>
							<xsl:when test="DisplayLabel/@sourceType='sys_shared'">
								<xsl:value-of select="concat('psx.ce.shared.', Control/@paramName, '@', DisplayLabel)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', Control/@paramName, '@', DisplayLabel)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:variable name="mnemonickeyval">
						<xsl:choose>
							<xsl:when test="DisplayLabel/@sourceType='sys_system'">
								<xsl:value-of select="concat('psx.ce.system.', Control/@paramName, '.mnemonic.', DisplayLabel, '@', Control/@accessKey)"/>
							</xsl:when>
							<xsl:when test="DisplayLabel/@sourceType='sys_shared'">
								<xsl:value-of select="concat('psx.ce.shared.', Control/@paramName, '.mnemonic.', DisplayLabel, '@', Control/@accessKey)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', Control/@paramName, '.mnemonic.', DisplayLabel, '@', Control/@accessKey)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<label for="{Control/@paramName}" accesskey="{Control/@accessKey}">
						<xsl:call-template name="getMnemonicLocaleString">
							<xsl:with-param name="key" select="$keyval"/>
							<xsl:with-param name="mnemonickey" select="$mnemonickeyval"/>
							<xsl:with-param name="lang" select="$lang"/>
						</xsl:call-template>
					</label>
				</xsl:if>
			</td>
			<td>
				<xsl:apply-templates select="Control" mode="psxcontrol"/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="DisplayField[@displayType='sys_error'] ">
		<tr>
			<td class="controlnameerror">
				<xsl:if test="DisplayLabel!=''">
					<xsl:variable name="keyval">
						<xsl:choose>
							<xsl:when test="DisplayLabel/@sourceType='sys_system'">
								<xsl:value-of select="concat('psx.ce.system.', Control/@paramName, '@', DisplayLabel)"/>
							</xsl:when>
							<xsl:when test="DisplayLabel/@sourceType='sys_shared'">
								<xsl:value-of select="concat('psx.ce.shared.', Control/@paramName, '@', DisplayLabel)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', Control/@paramName, '@', DisplayLabel)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<label for="{Control/@paramName}" accesskey="{Control/@accessKey}">
						<xsl:call-template name="getLocaleString">
							<xsl:with-param name="key" select="$keyval"/>
							<xsl:with-param name="lang" select="$lang"/>
						</xsl:call-template>
					</label>
				</xsl:if>
			</td>
			<td>
				<xsl:apply-templates select="Control" mode="psxcontrol"/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="DisplayField">
		<tr>
			<td>
				<b>unmatched display field type: '<xsl:copy-of select="@displayType"/>'</b>
				<br id="Rhythmyx"/>
				<xsl:comment>Unmatched display field</xsl:comment>
				<xsl:copy-of select="."/>
			</td>
		</tr>
	</xsl:template>
	<!--
		If it is new document hide the complex children.
	-->
	<xsl:template match="Control[@name='sys_Table' and ../../@newDocument='yes'] " priority="10" mode="psxcontrol"/>
	<xsl:template match="Control" mode="rx_hidden">
		<input type="hidden" name="{@paramName}" value="{Value}"/>
	</xsl:template>
	<xsl:template match="ControlName" mode="scriptfiles">
		<xsl:variable name="ctlname" select="."/>
		<xsl:choose>
			<xsl:when test="$userLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]">
				<xsl:apply-templates select="$userLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='script']" mode="scriptfiles"/>
			</xsl:when>
			<xsl:when test="$systemLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]">
				<xsl:apply-templates select="$systemLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='script']" mode="scriptfiles"/>
			</xsl:when>
         <xsl:otherwise>
            <xsl:for-each select="$customControlImportsDoc//xsl:import">
               <xsl:variable name="customLibrary" select="@href"/>
                  <xsl:choose>
                     <xsl:when test="document($customLibrary)/*/psxctl:ControlMeta[@name=$ctlname]">
                        <xsl:apply-templates select="document($customLibrary)/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='script']" mode="scriptfiles"/>
                     </xsl:when>
                  </xsl:choose>
            </xsl:for-each>
         </xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="psxctl:FileDescriptor" mode="scriptfiles"><xsl:value-of select="psxctl:FileLocation"/>;</xsl:template>
	<xsl:template match="ControlName" mode="stylefiles">
		<xsl:variable name="ctlname" select="."/>
		<xsl:choose>
			<xsl:when test="$systemLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]">
				<xsl:apply-templates select="$systemLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='css']" mode="stylefiles"/>
			</xsl:when>
			<!-- User CSS should be after the System CSS -->
			<xsl:when test="$userLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]">
				<xsl:apply-templates select="$userLibraryDoc/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='css']" mode="stylefiles"/>
			</xsl:when>
         <xsl:otherwise>
            <xsl:for-each select="$customControlImportsDoc/*/xsl:import">
               <xsl:variable name="customLibrary" select="@href"/>
                  <xsl:choose>
                     <xsl:when test="document($customLibrary)/*/psxctl:ControlMeta[@name=$ctlname]">
                        <xsl:apply-templates select="document($customLibrary)/*/psxctl:ControlMeta[@name=$ctlname]/psxctl:AssociatedFileList/psxctl:FileDescriptor[@type='css']" mode="stylefiles"/>
                     </xsl:when>
                  </xsl:choose>
            </xsl:for-each>
         </xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="psxctl:FileDescriptor" mode="stylefiles"><xsl:value-of select="psxctl:FileLocation"/>:<xsl:value-of select="@name"/>;</xsl:template>
   <xsl:template match="ControlNameSet"/>
	<xsl:template match="Workflow"/>
	<xsl:template match="SectionLinkList"/>
	<xsl:template match="ActionLinkList"/>
        
	<!-- Generic template that searches for a supplied string  in the supplied text and then replaces with another supplied string -->
        <!-- Parameters: -->
        <!-- text    - text in which search and replace is needed -->
        <!-- replace - string to search for -->
        <!-- with    - string to replace with -->
        <xsl:template name="replace-string">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        <xsl:variable name="stringText" select="string($text)"/>
        <xsl:choose>
           <xsl:when test="contains($stringText,$replace)">
              <xsl:value-of select="substring-before($stringText,$replace)"/>
              <xsl:value-of select="$with"/>
              <xsl:call-template name="replace-string">
                 <xsl:with-param name="text" select="substring-after($stringText,$replace)"/>
                 <xsl:with-param name="replace" select="$replace"/>
                 <xsl:with-param name="with" select="$with"/>
              </xsl:call-template>
           </xsl:when>
           <xsl:otherwise>
              <xsl:value-of select="$stringText"/>
           </xsl:otherwise>
         </xsl:choose>
        </xsl:template>
	<xsl:template name="createControlScriptTags">
		<xsl:param name="scripttags"/>
		<xsl:if test="not(contains(substring-after($scripttags, ';'),substring-before($scripttags, ';')))">
			<script src="{substring-before($scripttags, ';')}">;</script>
		</xsl:if>
		<xsl:if test="string-length(substring-after($scripttags, ';')) &gt; 1">
			<xsl:call-template name="createControlScriptTags">
				<xsl:with-param name="scripttags" select="substring-after($scripttags, ';')"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<xsl:template name="createControlStyleTags">
		<xsl:param name="styletags"/>
		<xsl:if test="not(contains(substring-after($styletags, ';'),substring-before($styletags, ';')))">
			<xsl:variable name="styletag"><xsl:value-of select="substring-before($styletags, ';')"/></xsl:variable>
			<link rel="stylesheet" href="{substring-before($styletag, ':')}" type="text/css" media="screen" title="{substring-after($styletag, ':')}"/>
		</xsl:if>
		<xsl:if test="string-length(substring-after($styletags, ';')) &gt; 1">
			<xsl:call-template name="createControlStyleTags">
				<xsl:with-param name="styletags" select="substring-after($styletags, ';')"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.contenteditor.activeedit@Rhythmyx">Rhythmyx appears as part of the title.</key>
		<key name="psx.contenteditor.activeedit@Edit Content">Edit Content appears as part of the title when the item is in edit mode.</key>
		<key name="psx.contenteditor.activeedit@View Content">Rhythmyx appears as part of the title when the item is in preview mode.</key>
		<key name="psx.contenteditor.activeedit@Edit Child">Rhythmyx appears as part of the title while editing the child.</key>
		<key name="psx.contenteditor.activeedit@Related Content">Label for related content.</key>
		<key name="psx.contenteditor.activeedit@Return to parent">Link to take the user to parent item when the user is in child table.</key>
		<key name="psx.contenteditor.activeedit@Content">This text appear as bold at top of content editor when editing content and as a link when editing properties.</key>
		<key name="psx.contenteditor.activeedit@Properties">This text appear as bold at top of content editor when editing properties and as a link when editing content.</key>
		<key name="psx.contenteditor.activeedit.mnemonic.Return to parent@R">Mnemonic for label &quot;Return to parent&quot;</key>
		<key name="psx.contenteditor.activeedit.mnemonic.Properties@P">Mnemonic for label &quot;Properties&quot;</key>
		<key name="psx.contenteditor.activeedit.mnemonic.Content@C">Mnemonic for label &quot;Content&quot;</key>
		<key name="psx.contenteditor.activeedit@No Templates registered for this Content Type.">Message appears in related content section when there are no templates registered for this content type.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
