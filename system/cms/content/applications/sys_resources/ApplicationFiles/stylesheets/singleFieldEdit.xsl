<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<!-- $ Id: $ -->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
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
   <xsl:variable name="hasWebImageFx" select="//ControlNameSet[ControlName='sys_webImageFX']"/>
   <xsl:variable name="isEditLive" select="/ContentEditor/ItemContent/DisplayField[@displayType='sys_normal']/Control[@name='sys_EditLive']"/>
   <xsl:variable name="isEditLiveDynamic" select="/ContentEditor/ItemContent/DisplayField[@displayType='sys_normal']/Control[@name='sys_EditLiveDynamic']"/>
   <xsl:variable name="EditLiveDynamicName" select="'PSEditLiveDynamic'"/>
   <xsl:template match="/">
      <xsl:apply-templates select="ContentEditor"/>
   </xsl:template>
   <xsl:template match="ContentEditor">
      <xsl:variable name="bannerinclude" select="document(/*/SectionLinkList/SectionLink[@name='bannerincludeurl'])/*/url"/>
      <xsl:variable name="userstatusinclude" select="document(/*/SectionLinkList/SectionLink[@name='userstatusincludeurl'])/*/url"/>
      <xsl:variable name="helpinclude" select="document(/*/SectionLinkList/SectionLink[@name='helpincludeurl'])/*/url"/>
      <html>
         <head>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.contenteditor.singlefieldedit@Rhythmyx - Edit Field'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
			<script language="javascript" src="../tmx/tmx.jsp?sys_lang={$lang}">;</script>
			<script language="javascript" src="../../cm/jslib/jquery.js">;</script>
            <script src="../sys_resources/js/browser.js">;</script>
            <script src="../sys_resources/js/href.js">;</script>
            <script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
            <script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
            <script src="../sys_resources/js/AddFormParameters.js">;</script>
            <script src="../sys_resources/js/formValidation.js">;</script>
            <script src="../sys_resources/js/formChangeCheck.js">;</script>
            <script language="Javascript"><![CDATA[
      		  var hasEditLiveControls = false;
      		  function updateFieldOnAaPage()
      		  {
         		  if(window.opener.ps_updateFlag && window.opener && !window.opener.closed)
         		  {
            		  window.opener.ps_updateFlag = false;
            		  if(window.opener.ps)
            		     window.opener.ps.aa.controller.fieldEdit.updateField();
                    else //it is assumed to be legacy aa and refresh the whole page to get the changes.
                       window.opener.location.href = window.opener.location.href;
            		  if(window.opener.ps_openFullEditorFlag)
            		  {
                        window.opener.ps_openFullEditorFlag = false;
                        var ceurl = PSHref2Hash(window.location.href);
                        ceurl["sys_view"]="sys_All";
                        window.location.href=PSHash2Href(ceurl);
            		  }
            		  else if(window.opener.ps_CloseMe)
            		  {
            		     window.opener.ps_CloseMe = false;
            		     window.close();
            		  }
         		  }
      		  }
	          function psCustomControlIsDirty()
	          {
                  return (false]]><xsl:apply-templates select="/*/ItemContent/DisplayField[@displayType='sys_normal']" mode="psxcontrol-customcontrol-isdirty"/><![CDATA[); 
               }]]></script>
            <xsl:if test="$isEditLive or $isEditLiveDynamic">
               <script language="Javascript"><![CDATA[
      		  hasEditLiveControls = true;
         		]]></script>
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
            <!-- @@REP WITH WEP I18N FUNC@@ -->
            <xsl:if test="$hasWebImageFx">
               <script src="../sys_resources/js/href.js">;</script>
               <script src="../rx_resources/webimagefx/rx_wifx.js">;</script>
            </xsl:if>
            <script language="javascript">
               var canSubmit = true;
            </script>
         </head>
         <body class="headercell2" topmargin="5" leftmargin="5">
            <!-- provide a hook for controls to get script to run when page is loaded -->
            <xsl:attribute name="onLoad">updateFieldOnAaPage();<xsl:if test="$hasWebImageFx"><xsl:text>wifxLoadImage();</xsl:text></xsl:if><xsl:apply-templates select="/*/ItemContent" mode="psxcontrol-body-onload"/><xsl:text>setTimeout('ps_getInitialChecksum(document.forms[0])',2000);</xsl:text></xsl:attribute>
            <table width="100%" border="0" cellpadding="0" cellspacing="1">
               <tr>
                  <td width="100%" height="100%" valign="top">
                     <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                        <xsl:call-template name="sys_GenericPageError"/>
                        <tr>
                           <td class="headercell2">
                              <!-- ********** INSERT CONTENT HERE ********** -->
                              <xsl:comment>Start of Content Block</xsl:comment>
                              <form method="post" action="{@submitHref}" id="EditForm" name="EditForm" encType="multipart/form-data">
                                 <!-- provide a hook for controls to get script to run when page is submitted -->
                                 <!-- each template should generate JS that returns a true/false value -->
                                 <xsl:attribute name="onsubmit">ps_setUpdateFlag();<xsl:text>addFormRedirect(document.forms['EditForm']); return </xsl:text><xsl:if test="$hasWebImageFx"><xsl:text>wifxHandleSubmit</xsl:text></xsl:if><xsl:text>(_ignoreMultipleSubmit() &amp;&amp; canSubmit &amp;&amp; true</xsl:text><xsl:apply-templates select="/*/ItemContent" mode="psxcontrol-form-onsubmit"/><xsl:text>)</xsl:text></xsl:attribute>
                                 <table width="100%" border="0" cellspacing="5" cellpadding="0" summary="controls for editing metadata">
                                    <tr>
                                       <td height="8"><img src="../sys_resources/images/spacer.gif" height="8"/></td>
                                    </tr>
                                    <xsl:apply-templates select="ItemContent"/>
                                    <tr>
                                       <td height="8"><img src="../sys_resources/images/spacer.gif" height="8"/></td>
                                    </tr>
                                    <tr>
                                       <td align="center" colspan="2" class="headercell2">
                                          <xsl:comment>Action List goes here</xsl:comment>
                                          <div id="psRegularButtons">
                                             <input type="button" value="Content Item..." onClick="ps_openFullEditor();" class="nav_body">
                                             </input>&nbsp;
                                             <xsl:apply-templates select="ActionLinkList" mode="actionlist"/>
                                             &nbsp;<input type="button" onClick="ps_closeWithDirtyCheck();" class="nav_body">
                                                <xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                                                <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                                             </input>
                                          </div>
                                          <div id="psDojoButtons" style="visibility:hidden">
                                             <table align="center" width="380" cellpadding="2" cellspacing="0" border="0">
                                                <tr>
                                                   <td width="35%">&nbsp;</td>
                                                   <td width="10%" align="right">
                                                      <button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.Field.wgtButtonFullEditor">
                                                         Content Item...
                                                      </button>
                                                   </td>
                                                   <td  width="10%" align="right">
                                                      <button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.Field.wgtButtonUpdate">
                                                         Update
                                                      </button>
                                                   </td>
                                                   <td  width="10%" align="left">
                                                      <button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.Field.wgtButtonClose">
                                                         Close
                                                      </button>
                                                   </td>
                                                   <td width="35%">&nbsp;</td>
                                                </tr>
                                             </table>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                                 <xsl:apply-templates select="ActionLinkList" mode="addformparams"/>
                                 <input xmlns="" type="hidden" name="httpcaller" value=""/>
                                 <xsl:if test="not(//Control[@paramName='psredirect'])">
                                    <input xmlns="" type="hidden" name="psredirect" value=""/>
                                 </xsl:if>
                                 <input xmlns="" type="hidden" name="sys_contenttypeid" >
                                    <xsl:attribute name="value"><xsl:value-of select="//ContentEditor/@contentTypeId"/></xsl:attribute>
                                 </input>
                              </form>
                              <form name="inlinelinkssearch" target="searchitems" method="post">
                                 <input xmlns="" type="hidden" name="inlinetext" value=""/>						
                                 <input xmlns="" type="hidden" name="inlineslotid" value=""/>						
                                 <input xmlns="" type="hidden" name="inlinetype" value=""/>						
                              </form>
                              <!-- ********** END CONTENT HERE ********** -->
                              <xsl:comment>End of Content Block</xsl:comment>
                           </td>
                        </tr>
                     </table>
                  </td>
               </tr>
            </table>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="ItemContent">
      <xsl:comment>ItemContent</xsl:comment>
      <xsl:apply-templates select="DisplayField"/>
   </xsl:template>
   <xsl:template match="DisplayField[@displayType='sys_hidden']">
      <xsl:apply-templates select="Control" mode="psxcontrol-hidden"/>
   </xsl:template>
   <xsl:template match="DisplayField[@displayType='sys_normal']">
      <xsl:comment>Normal Control</xsl:comment>
      <tr>
         <td align="center">
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
   <xsl:template match="ControlNameSet"/>
   <xsl:template match="Workflow"/>
   <xsl:template match="SectionLinkList"/>
   <xsl:template match="ActionLinkList"/>
   <psxi18n:lookupkeys>
      <key name="psx.contenteditor.singlefieldedit@Rhythmyx - Edit Field">Title for signle field edit.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
