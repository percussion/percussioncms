<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="activeiteminfodoc" select="document(//activeitemurl)"/>
	<xsl:variable name="contentid">
		<xsl:choose>
			<xsl:when test="$activeiteminfodoc//@contentid!=''">
				<xsl:value-of select="$activeiteminfodoc//@contentid"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="//contentid"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="revision">
		<xsl:choose>
			<xsl:when test="$activeiteminfodoc//@contentid!=''">
				<xsl:value-of select="$activeiteminfodoc//@revision"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="//revision"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="appletparams">
		<AppletParams>
			<Param name="CODE" value="com.percussion.cx.PSContentExplorerApplet.class"/>
			<Param name="DEBUG" value="{//@debug}"/>
			<Param name="RESTRICTSEARCHFIELDSTOUSERCOMMUNITY" value="{//@RestrictSearchFieldsToUserCommunity}"/>
			<Param name="CacheSearchableFieldsInApplet" value="{//@CacheSearchableFieldsInApplet}"/>
			<Param name="isManagedNavUsed" value="{//@isManagedNavUsed}"/>
			<Param name="CODEBASE" value="../sys_resources/AppletJars"/>
			<Param name="OPTIONS_URL" value="../sys_cxSupport/options.xml"/>
			<Param name="MENU_URL" value="../sys_cxItemAssembly/ItemAssemblyMenu.html"/>
			<Param name="NAV_URL" value="{concat(//iadataurl,'?sys_contentid=', $contentid, '&amp;sys_revision=', $revision, '&amp;sys_variantid=', //variantid, '&amp;sys_slotname=', //slotname, '&amp;sys_slotid=', //slotid,  '&amp;sys_mode=', //mode)}"/>
			<Param name="CACHE_ARCHIVE" value="rxcx.jar"/>
			<Param name="CACHE_OPTION" value="Plugin"/>
			<Param name="ARCHIVE" value="rxcx.jar"/>
			<Param name="helpset_file" value="../Docs/Rhythmyx/Business_Users/Content_Explorer_Help.hs"/>
			<Param name="sys_cxpath" value="{//@cxpath}"/>
			<Param name="TYPE" value="{concat('application/x-java-applet;',//@version_type,'=',//@implementation_version)}"/>
			<Param name="MAYSCRIPT" value="true"/>
			<Param name="classid" value="{//@classid}"/>
			<Param name="codebaseattr" value="{//@codebase}"/>
			<Param name="NAME" value="ContentExplorerApplet"/>
			<Param name="WIDTH" value="100%"/>
			<Param name="HEIGHT" value="100%"/>
			<Param name="VIEW">
				<xsl:attribute name="value"><xsl:choose><xsl:when test="//mode='Site Centric'">RC</xsl:when><xsl:otherwise>IA</xsl:otherwise></xsl:choose></xsl:attribute>
			</Param>
			<Param name="sys_useSelectiveViewRefresh" value="{//@selective_view_refresh}"/>
			<Param name="sys_isSearchEngineAvailable" value="{//@search_engine_available}"/>
		</AppletParams>
	</xsl:variable>
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
	<xsl:variable name="useragent" select="//@useragent"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key">
							<xsl:choose>
								<xsl:when test="//mode='Site Centric'">psx.sys_cxItemAssembly.itemassembly@Rhythmyx - Related Content Search</xsl:when>
								<xsl:otherwise>psx.sys_cxItemAssembly.itemassembly@Rhythmyx - Active Assembly for Documents</xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<script>
               function showWindow(url, target, style)
               {
                  win = parent.window.open(url, target, style);
                  win.focus();
               }
               function refreshParent()
               {
                  if(window.opener!=null &amp;&amp; !window.opener.closed)
                  {
                     window.opener.location.href = window.opener.location.href;
		     //delay is required
                     setTimeout('self.close()', 333);
                  }
               }
            </script>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
			</head>
			<body marginwidth="0" marginheight="0" leftmargin="0" topmargin="0">
				<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
					<tr class="bannerbackground" height="75">
						<td align="left" valign="top" width="100%">
							<div xmlns:fo="http://www.w3.org/1999/XSL/Format" id="RhythmyxBanner" style="border-bottom:none;">
								<table cellpadding="0" cellspacing="0" class="banner-table" width="100%">
									<tr>
										<td width="220">
											<img height="75" src="../sys_resources/images/blank-pixel.gif" width="220"/>
										</td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<script language="javascript1.2" src="../sys_resources/js/browser.js">;</script>
							<script language="JavaScript1.2">	        
			var appletCaller = new AppletCaller();
			<xsl:for-each select="$appletparams/AppletParams/Param">
									<xsl:text>appletCaller.addParam("</xsl:text>
									<xsl:value-of select="@name"/>
									<xsl:text>", "</xsl:text>
									<xsl:value-of select="@value"/>
									<xsl:text>");</xsl:text>
								</xsl:for-each>	
			if(is_safari)
			{
			   // This is a best guess for the appropriate height as
			   // there does not seem to be a good way to dynamically resize
			   // the applet in Safari. Hopefully Apple will fix the problem with
			   // using percentages so that we can use 100% for both width and
			   // height and let the browser handle resizing. I have logged a bug
			   // with Apple for this issue.
			   var height = 320;
			   var width= 782;
			   appletCaller.addParam("width", width);
			   appletCaller.addParam("height", height);
			}
			appletCaller.show();         				
		      </script>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_cxItemAssembly.itemassembly@Rhythmyx - Active Assembly for Documents">Title for Rhythmyx Active Assembly for Documents page.</key>
		<key name="psx.sys_cxItemAssembly.itemassembly@Rhythmyx - Related Content Search">Title for Rhythmyx Related Content Search page.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
