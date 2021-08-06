<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="lefttopurl" select="concat(//itemurl,'&amp;sys_contentid=',//sys_contentid1,'&amp;sys_revision=',//sys_revision1,'&amp;sys_variantid=',//sys_variantid1,'&amp;itemnumber=',1)"/>
	<xsl:variable name="leftbottomurl" select="concat(//itemurl,'&amp;sys_contentid=',//sys_contentid2,'&amp;sys_revision=',//sys_revision2,'&amp;sys_variantid=',//sys_variantid2,'&amp;itemnumber=',2)"/>
	<xsl:variable name="selectrevurl" select="//selectrevurl"/>
	<xsl:variable name="contentid1" select="//sys_contentid1"/>
	<xsl:variable name="contentid2" select="//sys_contentid2"/>
	<xsl:variable name="revision1" select="//sys_revision1"/>
	<xsl:variable name="revision2" select="//sys_revision2"/>
	<xsl:variable name="variantid1" select="//sys_variantid1"/>
	<xsl:variable name="variantid2" select="//sys_variantid2"/>
	<xsl:variable name="useragent" select="//@useragent"/>
	<xsl:variable name="appletparams">
		<AppletParams>
			<Param name="code" value="com.percussion.tools.help.PSHelpApplet"/>
			<Param name="archive" value="help.jar,jh.jar"/>
			<Param name="codebase" value="../sys_resources/AppletJars/"/>
			<Param name="MAYSCRIPT" value="true"/>
			<Param name="scriptable" value="true"/>
			<Param name="helpset_file" value="../../Docs/Rhythmyx/Business_Users/Content_Explorer_Help.hs"/>
			<Param name="helpid" value="O15253"/>
			<Param name="helpIcon" value="/rx_resources/images/en-us/help_icon.gif"/>
			<Param name="name" value="help"/>
			<Param name="width" value="0"/>
			<Param name="height" value="0"/>
			<Param name="classid" value="{//@classid}"/>
			<Param name="codebaseattr" value="{//@codebase}"/>
			<Param name="TYPE" value="{concat('application/x-java-applet;',//@version_type,'=',//@implementation_version)}"/>
		</AppletParams>
	</xsl:variable>
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_Compare.compare@Rhythmyx - Document Comparison'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link href="../sys_resources/css/templates.css" rel="stylesheet" type="text/css"/>
				<link href="../rx_resources/css/templates.css" rel="stylesheet" type="text/css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<script language="javascript" src="../sys_resources/js/href.js">;</script>
				<script language="javascript">
               function openSelectRevision(url)
               {
                  window.open(url); 
               }
               function openSelectDependent(url)
               {
                  window.open(url); 
               }
               function changeVariant(itemnumber)
               {
                  var rightframeurl = parent.rightframe.location.href;
                  var leftframeurl = location.href;
                  var h = PSHref2Hash(rightframeurl);
                  var h1 = PSHref2Hash(leftframeurl);
                  if(itemnumber==1){
                     h["sys_contentid1"] = h1["sys_contentid1"];
                     h["sys_revision1"] = document.itemdetails1.sys_revision.value;
                     h["sys_variantid1"] = document.itemdetails1.variantlist[document.itemdetails1.variantlist.selectedIndex].value;
                     h["activeitem"] = 1;
                  }
                  else if(itemnumber==2){
                     h["sys_contentid2"] = h1["sys_contentid2"];
                     h["sys_revision2"] = document.itemdetails2.sys_revision.value;                     
                     h["sys_variantid2"] = document.itemdetails2.variantlist[document.itemdetails2.variantlist.selectedIndex].value;
                     h["activeitem"] = 2;
                  }
                  parent.rightframe.location.href = PSHash2Href(h,rightframeurl);
               }
           </script>
			</head>
			<body topmargin="0" leftmargin="0" marginheight="0" marginwidth="0" class="headercell">
				<table border="0" width="225" height="100%" cellspacing="0" cellpadding="0">
					<tr>
						<td width="220" class="headercell" valign="top">
							<table border="0" width="220" cellspacing="0" cellpadding="0">
								<tr class="outerboxcell">
									<td width="225" class="outerboxcellfont" align="center" colspan="2" height="2">
										<img src="../sys_resources/images/spacer.gif" height="2" width="225"/>
									</td>
								</tr>
								<tr class="outerboxcell">
									<td width="225" class="outerboxcellfont" align="center" colspan="2" height="2">
										<xsl:variable name="helpIcon" select="concat('../rx_resources/images/',$lang,'/help_icon.gif')"/>
										            <xsl:variable name="helpAlt">
										               <xsl:call-template name="getLocaleString">
										                  <xsl:with-param name="key" select="'psx.sys_cmpHelp.help.alt@Help'"/>
										                  <xsl:with-param name="lang" select="$lang"/>
										               </xsl:call-template>
										            </xsl:variable>
                                                                                <script language="javaScript1.2" src="../sys_resources/js/browser.js">;</script>
										<script language="JavaScript1.2">	        
											        var appletCaller = new AppletCaller();
											        <xsl:for-each select="$appletparams/AppletParams/Param">
												   <xsl:text>appletCaller.addParam("</xsl:text><xsl:value-of select="@name"/><xsl:text>", "</xsl:text><xsl:value-of select="@value"/><xsl:text>");</xsl:text>					
												</xsl:for-each>													
											        appletCaller.show();
										    
										 
	        	        
	                                                                       </script>
	                                                                       <a href="javascript:void(0);" onclick="_showHelp()"><img align="absmiddle" alt="{$helpAlt}" border="0" src="{$helpIcon}"/></a>
									</td>
								</tr>
								<tr>
									<td width="225" align="center" valign="top">
										<xsl:variable name="itemdetails1" select="document($lefttopurl)//itemdetails"/>
										<xsl:variable name="contentstatusurl1" select="concat(//contentstatusurl,'&amp;sys_contentid=',//sys_contentid1,'&amp;itemnumber=1')"/>
										<xsl:choose>
											<xsl:when test="$itemdetails1//sys_revision!=''">
												<xsl:apply-templates select="document($lefttopurl)//itemdetails">
													<xsl:with-param name="itemnumber" select="'1'"/>
												</xsl:apply-templates>
											</xsl:when>
											<xsl:otherwise>
												<xsl:apply-templates select="document($contentstatusurl1)//itemdetails">
													<xsl:with-param name="itemnumber" select="'1'"/>
												</xsl:apply-templates>
											</xsl:otherwise>
										</xsl:choose>
									</td>
								</tr>
								<tr class="headercell">
									<td width="220" class="headercellfont" align="center" colspan="2" height="10">
                     &nbsp;
                  </td>
								</tr>
								<tr class="outerboxcell">
									<td width="225" class="outerboxcellfont" align="center" colspan="2" height="2">
										<img src="../sys_resources/images/spacer.gif" height="2" width="225"/>
									</td>
								</tr>
								<tr>
									<td width="225" align="center">
										<xsl:variable name="itemdetails2" select="document($leftbottomurl)//itemdetails"/>
										<xsl:variable name="contentstatusurl2" select="concat(//contentstatusurl,'&amp;sys_contentid=',//sys_contentid2,'&amp;itemnumber=2')"/>
										<xsl:choose>
											<xsl:when test="$itemdetails2//sys_revision!=''">
												<xsl:apply-templates select="document($leftbottomurl)//itemdetails">
													<xsl:with-param name="itemnumber" select="'2'"/>
													<xsl:with-param name="dependenturl" select="//dependenturl"/>
												</xsl:apply-templates>
											</xsl:when>
											<xsl:otherwise>
												<xsl:apply-templates select="document($contentstatusurl2)//itemdetails">
													<xsl:with-param name="itemnumber" select="'2'"/>
													<xsl:with-param name="dependenturl" select="//dependenturl"/>
												</xsl:apply-templates>
											</xsl:otherwise>
										</xsl:choose>
									</td>
								</tr>
							</table>
						</td>
						<td width="5" height="100%" class="outerboxcell"/>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="itemdetails">
		<xsl:param name="itemnumber"/>
		<xsl:param name="dependenturl"/>
		<form name="{concat('itemdetails',$itemnumber)}">
			<table width="220" align="center" border="0" cellpadding="0" cellspacing="1" class="outerboxcell">
				<xsl:for-each select="items/item[position()=last()]">
					<tr class="outerboxcell">
						<td align="center" class="outerboxcellfont" height="20" colspan="2">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Item'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
							<xsl:text>:</xsl:text>
							<xsl:value-of select="sys_contentid"/>
						</td>
					</tr>
					<xsl:if test="$itemnumber=2">
						<!--
                     This row need to be uncommented when new dependency viewer is ready.
                  <tr class="headercell">
                     <td width="220" class="headercellfont" align="center" colspan="2">
                        <input type="button" name="selectdependent" value="Select Dependent Item">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Select Dependent Item'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                           <xsl:attribute name="onclick">javascript:openSelectDependent("<xsl:value-of select="$dependenturl"/>");</xsl:attribute>
                        </input>
                     </td>
                  </tr>
                  -->
					</xsl:if>
					<tr class="datacell2">
						<td align="left" class="datacellfontheader" height="20">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Title'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
						<td align="left" class="datacell1font" width="150">
							<xsl:if test="sys_contentid!=''">
								<xsl:value-of select="title"/>
							</xsl:if>&nbsp;
                  </td>
					</tr>
					<tr class="datacell1">
						<td align="left" class="datacellfontheader" height="20">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Rev'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
						<td align="left" class="datacell1font">
							<xsl:value-of select="sys_revision"/>&nbsp;
                     <input type="hidden" name="sys_revision" value="{sys_revision}"/>
						</td>
					</tr>
					<tr class="datacell2">
						<td align="left" class="datacellfontheader" height="20">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Date'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
						<td align="left" class="datacell1font">
							<xsl:if test="sys_revision!=''">
								<xsl:value-of select="date"/>
							</xsl:if>&nbsp;
                  </td>
					</tr>
					<tr class="datacell1">
						<td align="left" class="datacellfontheader" height="20">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Who'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
						<td align="left" class="datacell1font">
							<xsl:value-of select="actor"/>&nbsp;
                  </td>
					</tr>
					<tr class="datacell2">
						<td align="left" class="datacellfontheader" height="20">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_Compare.leftframe@State'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
						<td align="left" class="datacell1font">
							<xsl:if test="state!=''">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="concat('psx.workflow.state@',state)"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</xsl:if>&nbsp;
                  </td>
					</tr>
					<tr class="datacell1">
						<td align="left" class="datacellfontheader" height="20">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Comment'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</td>
						<td align="center" class="datacell1font">
							<xsl:if test="comment!=''">
								<img alt="{comment}" src="../sys_resources/images/singlecomment.gif" width="16" height="16" border="0">
									<xsl:attribute name="OnClick">newWindow=window.open('','HistoryComment','width=500,height=      100,resizable=yes');newWindow.document.write("<xsl:value-of select="comment"/>");setTimeout('newWindow.close()',5000);      </xsl:attribute>
								</img>
							</xsl:if>&nbsp;
      				</td>
					</tr>
				</xsl:for-each>
				<xsl:if test="$itemnumber='1' or $contentid2!=''">
					<xsl:apply-templates select="document(variantlisturl)//VariantList" mode="variantlist">
						<xsl:with-param name="itemnumber" select="$itemnumber"/>
					</xsl:apply-templates>
				</xsl:if>&nbsp;
            <tr class="datacell2">
					<td width="100%" align="center" valign="middle" colspan="2" class="datacell1font" height="30">
						<input type="button" name="change" value="Select Revision">
							<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Select Revision'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
							<xsl:attribute name="onclick">javascript:openSelectRevision("<xsl:choose><xsl:when test="$itemnumber='1' or $contentid2=''"><xsl:value-of select="concat($selectrevurl,'?sys_contentid=',$contentid1,'&amp;itemnumber=',$itemnumber)"/></xsl:when><xsl:otherwise><xsl:value-of select="concat($selectrevurl,'?sys_contentid=',$contentid2,'&amp;itemnumber=',$itemnumber)"/></xsl:otherwise></xsl:choose>");</xsl:attribute>
						</input>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>
	<xsl:template match="VariantList" mode="variantlist">
		<xsl:param name="itemnumber"/>
		<tr class="datacell1">
			<td colspan="2" align="left" class="datacell1font" height="20">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.sys_Compare.leftframe@Select Template'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</td>
		</tr>
		<tr class="datacell2">
			<td colspan="2" align="center" class="datacell1font" height="20">
				<select name="variantlist" onchange="{concat('javascript:changeVariant(',$itemnumber,');')}">
					<xsl:for-each select="Variant">
						<option value="{@variantId}">
							<xsl:choose>
								<xsl:when test="$itemnumber=1">
									<xsl:if test="@variantId=$variantid1">
										<xsl:attribute name="selected">yes</xsl:attribute>
									</xsl:if>
								</xsl:when>
								<xsl:when test="$itemnumber=2">
									<xsl:if test="@variantId=$variantid2">
										<xsl:attribute name="selected">yes</xsl:attribute>
									</xsl:if>
								</xsl:when>
							</xsl:choose>
							<xsl:value-of select="DisplayName"/>
						</option>
					</xsl:for-each>
				</select>
			</td>
		</tr>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_Compare.leftframe@Item">Heading label</key>
		<key name="psx.sys_Compare.leftframe@Select Dependent Item">Button label, when the button clicked it opens depenedent items window</key>
		<key name="psx.sys_Compare.leftframe@Title">Row Label, other columns shows the title.</key>
		<key name="psx.sys_Compare.leftframe@Rev">Row Label, other column shows the revision id.</key>
		<key name="psx.sys_Compare.leftframe@Date">Row Label, other column shows the revision date.</key>
		<key name="psx.sys_Compare.leftframe@Who">Row Label, other column shows the person name who acted on the document.</key>
		<key name="psx.sys_Compare.leftframe@State">Row Label, other column shows the workflow state of the document.</key>
		<key name="psx.sys_Compare.leftframe@Comment">Row Label, other column shows the user comment.</key>
		<key name="psx.sys_Compare.leftframe@Select Template">Template drop down list box label.</key>
		<key name="psx.sys_Compare.leftframe@Select Revision">Button label, when the button clicked it opens select revision window.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
