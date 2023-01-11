<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
		%HTMLspecial;
		]>
<xsl:stylesheet version="1.1" xmlns="http://www.w3.org/1999/xhtml"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exsl="http://exslt.org/common"
				extension-element-prefixes="exsl"
				xmlns:psxi18n="urn:www.percussion.com/i18n"
				exclude-result-prefixes="psxi18n exsl ">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl" />
	<xsl:output method="html" omit-xml-declaration="yes" encoding="UTF-8" />
	<xsl:variable name="rxroot">
		<xsl:choose>
			<xsl:when test="string-length(//@rxroot) = 0">
				<xsl:text>..</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="//@rxroot" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="height">
		<xsl:choose>
			<xsl:when test="string-length(//@height) = 0">
				<xsl:text>100%</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="//@height" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="width">
		<xsl:choose>
			<xsl:when test="string-length(//@width) = 0">
				<xsl:text>100%</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="//@width" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="editlivedir">
		<xsl:value-of select="concat($rxroot,'../rx_resources/ephox/editlivejava')" />
	</xsl:variable>
	<xsl:variable name="appletparams">
		<AppletParams xmlns="">
			<Param name="CODE" value="com.percussion.cx.PSContentExplorerApplet.class" />
			<Param name="VIEW" value="CX" />
			<xsl:if test="//@debug = 'true'">
				<Param name="DEBUG" value="{//@debug}" />
			</xsl:if>
			<xsl:if test="//@debug = 'false'">
				<Param name="DEBUG" value="{//@debug}" />
			</xsl:if>
			<Param name="RESTRICTSEARCHFIELDSTOUSERCOMMUNITY" value="{//@RestrictSearchFieldsToUserCommunity}" />
			<Param name="CacheSearchableFieldsInApplet" value="{//@CacheSearchableFieldsInApplet}" />
			<Param name="isManagedNavUsed" value="{//@isManagedNavUsed}" />
			<xsl:if test="floor(//@contentid) = //@contentid">
				<Param name="CONTENTID" value="{//@contentid}" />
			</xsl:if>


			<Param name="CODEBASE" value="{concat($rxroot,'/../dce')}" />
			<Param name="OPTIONS_URL" value="{concat($rxroot,'/sys_cxSupport/options.xml')}" />
			<Param name="MENU_URL"
				   value="{concat($rxroot,'/sys_cx/ContentExplorerMenu.html')}" />
			<Param name="NAV_URL" value="{concat($rxroot,'/sys_cx/ContentExplorer.html')}" />
			<Param name="CACHE_ARCHIVE" value="ContentExplorer-@BUILDVERSION@.jar" />
			<Param name="CACHE_OPTION" value="Plugin" />
			<Param name="ARCHIVE" value="ContentExplorer-@BUILDVERSION@.jar" />
			<Param name="helpset_file"
				   value="{concat($rxroot,'/Docs/Business_Users/Content_Explorer_Help.hs')}" />
			<Param name="sys_cxinternalpath" value="{//@cxinternalpath}" />
			<Param name="sys_cxdisplaypath" value="{//@cxdisplaypath}" />
			<Param name="TYPE"
				   value="{concat('application/x-java-applet;',//@version_type,'=',//@implementation_version)}" />
			<Param name="MAYSCRIPT" value="true" />
			<Param name="NAME" value="ContentExplorerApplet" />
			<Param name="ID" value="ContentExplorerApplet" />
			<Param name="WIDTH" value="{concat($width,'')}" />
			<Param name="HEIGHT" value="{concat($height,'')}" />
			<Param name="classid" value="{//@classid}" />
			<Param name="codebaseattr" value="{//@codebase}" />
			<Param name="sys_isSearchEngineAvailable" value="{//@search_engine_available}" />
			<Param name="SHOW_SPLASH" value="true" />
		</AppletParams>
	</xsl:variable>
	<xsl:variable name="lang" select="//@xml:lang" />
	<xsl:variable name="useragent" select="//@useragent" />
	<xsl:template match="/">
		<xsl:choose>
			<xsl:when test="//@portal = 'true'">
				<html lang="{$lang}">
					<head>
						<title></title>
					</head>
					<body>
						<xsl:call-template name="js-functions" />
						<div id="theCxApplet">
							<xsl:call-template name="applet-body" />
						</div>
					</body>
				</html>
			</xsl:when>
			<xsl:otherwise>
				<html xmlns="http://www.w3.org/1999/xhtml">
					<head>
						<title>
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key"
												select="'psx.sys_caSites.casites@Rhythmyx -       Content Explorer'" />
								<xsl:with-param name="lang" select="$lang" />
							</xsl:call-template>
						</title>
						<xsl:call-template name="js-functions" />
						<link rel="stylesheet" type="text/css" href="../sys_resources/css/custom-theme/jquery.ui.all.css"/>
						<style type="text/css">
							* {
								padding: 0px;
								margin: 0px;
							}

							html,body,div {
								height: 100%;
								overflow: hidden;
							}
						</style>
					</head>
					<body onunload="hideCxApplet();">
						<div id="theCxApplet">
							<xsl:call-template name="applet-body" />
						</div>

					</body>
				</html>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="js-functions">

		<script type="text/javascript" src="/Rhythmyx/util/getPSSessionID.jsp">;</script>
		<script type="text/javascript" src="{concat($rxroot,'/sys_resources/js/browser.js')}">;</script>
		<script type="text/javascript" src="{concat($editlivedir,'/editlivejava.js')}">;</script>
		<script src="{concat($rxroot,'/sys_resources/js/jquery/jquery.js')}"></script>
		<script src="{concat($rxroot,'/sys_resources/js/jquery/jquery-ui.js')}"></script>
		<script type="text/javascript" src="{concat($rxroot,'/sys_resources/js/sessioncheck.js')}">;</script>
		<script type="text/javascript" src="{concat($rxroot,'/sys_resources/js/elementEventListener.js')}">;</script>

		<script type="text/javascript">
			<xsl:text disable-output-escaping="yes">
       // &lt;![CDATA[<![CDATA[
           function showWindow(url, target, style)
            {
               win = parent.window.open(url, target, style);
               win.focus();
            }

            function hideCxApplet()
            {
             if(is_win && is_ie)
              {
                 theCxApplet.style.visibility = "hidden";
              }
            }

            function refreshCxApplet(hint, contentids, revisionids)
            {
               var applet = PSGetApplet(window, "ContentExplorerApplet");

               if(applet != null)
               {
                  applet.refresh(hint, contentids, revisionids);
               }

            }
            //CMS-13  Focusing anywhere in applet frame will force applet to gain focus allowing tabbing into applet
            // This does not work on Chrome.
            function startFocus(applet) {
	    		window.onfocus = function() {
		 			var applet = PSGetApplet(window, "ContentExplorerApplet");
		            if(applet != null) {
		                applet.requestFocus();
					}
        		};
	        	window.onclick = function() {
		            var applet = PSGetApplet(window, "ContentExplorerApplet");
		            if(applet != null){
						applet.requestFocus();
		            }
	        	};
			}

            $(document).ready(function() {
            	attachEventListener();
				$.sessionTimeout();
			});
  /*]]>]]&gt;*/
</xsl:text>
		</script>
	</xsl:template>
	<xsl:template name="applet-body">
		<script language="javascript" type="text/javascript">
			var appletCaller = new AppletCaller();
			<xsl:for-each select="exsl:node-set($appletparams)/AppletParams/Param">
				<xsl:text>appletCaller.addParam("</xsl:text><xsl:value-of select="@name" /><xsl:text>", "</xsl:text><xsl:value-of select="@value" /><xsl:text>");</xsl:text>
			</xsl:for-each>
			appletCaller.show();
			startFocus(appletCaller);
		</script>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_mainPage.mainpage@Rhythmyx - Content Administrator">Title for Rhythmyx main page.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>