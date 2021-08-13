<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="rxroot">
	   <xsl:choose>
       <xsl:when test="string-length(//@rxroot) = 0">..</xsl:when>
       <xsl:otherwise><xsl:value-of select="//@rxroot"/></xsl:otherwise>
     </xsl:choose>
  </xsl:variable>	
	<xsl:variable name="height">
	   <xsl:choose>
       <xsl:when test="string-length(//@height) = 0">100%</xsl:when>
       <xsl:otherwise><xsl:value-of select="//@height"/></xsl:otherwise>
     </xsl:choose>
  </xsl:variable>
	<xsl:variable name="width">
	   <xsl:choose>
       <xsl:when test="string-length(//@width) = 0">100%</xsl:when>
       <xsl:otherwise><xsl:value-of select="//@width"/></xsl:otherwise>
     </xsl:choose>
  </xsl:variable>
	<xsl:variable name="appletparams">
		<AppletParams>
			<Param name="CODE" value="com.percussion.cx.PSContentExplorerApplet.class"/>
			<Param name="VIEW" value="CX"/>
			<Param name="DEBUG" value="{//@debug}"/>
			<Param name="RESTRICTSEARCHFIELDSTOUSERCOMMUNITY" value="{//@RestrictSearchFieldsToUserCommunity}"/>
			<Param name="CacheSearchableFieldsInApplet" value="{//@CacheSearchableFieldsInApplet}"/>
			<Param name="isManagedNavUsed" value="{//@isManagedNavUsed}"/>
			<Param name="CONTENTID" value="{//@contentid}"/>
  			<Param name="CODEBASE" value="{concat($rxroot,'/sys_resources/AppletJars')}" />
  			<Param name="OPTIONS_URL" value="{concat($rxroot,'/sys_cxSupport/options.xml')}" />
  			<Param name="MENU_URL" value="{concat($rxroot,'/sys_cx/ContentExplorerMenu.html')}" />
  			<Param name="NAV_URL" value="{concat($rxroot,'/sys_cx/ContentExplorer.html')}" />
			<Param name="CACHE_ARCHIVE" value="rxcx.jar"/>
			<Param name="CACHE_OPTION" value="Plugin"/>
			<Param name="ARCHIVE" value="rxcx.jar"/>
   		        <Param name="helpset_file" value="{concat($rxroot,'/Docs/Rhythmyx/Business_Users/Content_Explorer_Help.hs')}" />
			<Param name="sys_cxinternalpath" value="{//@cxinternalpath}"/>
			<Param name="sys_cxdisplaypath" value="{//@cxdisplaypath}"/>
			<Param name="TYPE" value="{concat('application/x-java-applet;',//@version_type,'=',//@implementation_version)}"/>
			<Param name="MAYSCRIPT" value="true"/>
			<Param name="NAME" value="ContentExplorerApplet"/>
			<Param name="ID" value="ContentExplorerApplet"/>
			<Param name="WIDTH" value="{concat($width,'')}"/>
			<Param name="HEIGHT" value="{concat($height,'')}"/>
			<Param name="classid" value="{//@classid}"/>
			<Param name="codebaseattr" value="{//@codebase}"/>
			<Param name="sys_isSearchEngineAvailable" value="{//@search_engine_available}"/>
			<Param name="SHOW_SPLASH" value="true"/>
		</AppletParams>
	</xsl:variable>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="useragent" select="//@useragent"/>
	<xsl:template match="/">
      <xsl:choose>
         <xsl:when test="//@portal = 'true'">
            <html>
               <head>
                  <title></title>
               </head>
               <body topmargin="0" leftmargin="0" marginheight="0" marginwidth="0">
                  <xsl:call-template name="js-functions"/>  
                  <div id="theCxApplet">                
                     <xsl:call-template name="applet-body" />
                  </div>
               </body>
            </html>
         </xsl:when>
         <xsl:otherwise>
			<html>
				<head>
      				<title>
      					<xsl:call-template name="getLocaleString">
      						<xsl:with-param name="key" select="'psx.sys_caSites.casites@Rhythmyx -       Content Explorer'"/>
      						<xsl:with-param name="lang" select="$lang"/>
      					</xsl:call-template>
      				</title>	
					<xsl:call-template name="js-functions"/>		
				</head>
				<body marginwidth="0" marginheight="0" leftmargin="0" topmargin="0" onunload="hideCxApplet();">
				 	<div id="theCxApplet">
					    <xsl:call-template name="applet-body" />
					</div>
					
      			</body>
      		</html>
  		</xsl:otherwise>
     </xsl:choose>	
	</xsl:template>
   <xsl:template name="js-functions">
		<script language="javascript1.2" src="{concat($rxroot,'/sys_resources/js/browser.js')}">;</script>
			<script><![CDATA[
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
            ]]></script>   
   </xsl:template>
   <xsl:template name="applet-body">
   	 <script language="JavaScript1.2">	        
	        var appletCaller = new AppletCaller();
	        <xsl:for-each select="$appletparams/AppletParams/Param">
		   <xsl:text>appletCaller.addParam("</xsl:text><xsl:value-of select="@name"/><xsl:text>", "</xsl:text><xsl:value-of select="@value"/><xsl:text>");</xsl:text>					
		</xsl:for-each>	
		
	        appletCaller.show();         				
	   </script>
   </xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_mainPage.mainpage@Rhythmyx - Content Administrator">Title for Rhythmyx main page.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
