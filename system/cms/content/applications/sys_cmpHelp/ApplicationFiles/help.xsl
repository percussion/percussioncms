<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
   %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:output method="xml"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <xsl:call-template name="getLocaleString">
               <xsl:with-param name="key" select="'psx.sys_cmpHelp.help@Help'"/>
               <xsl:with-param name="lang" select="$lang"/>
            </xsl:call-template>
         </head>
         <body>
            <table width="100%" cellpadding="1" cellspacing="0" border="0">
               <xsl:apply-templates select="*" mode="mode0"/>
            </table>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="*" mode="mode0">
      <tr class="outerboxcell">
         <td align="center">
            <xsl:variable name="helpId">
              <xsl:choose>
                  <xsl:when test="starts-with(pagename,'pub_')">
                     <xsl:text>O1247</xsl:text>
                  </xsl:when>
                  <xsl:when test="starts-with(pagename, 'sys_') ">
                  <xsl:text>O1259</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>                     
                     <xsl:choose>
                        <xsl:when test="mode='sys_edit'">
                           <xsl:text>O3101</xsl:text>
                        </xsl:when>
                        <xsl:when test="mode='sys_preview'">
                           <xsl:text>O3099</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:text>O3018</xsl:text>
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:otherwise>
               </xsl:choose>    
            </xsl:variable>
            <xsl:variable name="helpIcon" select="concat('../rx_resources/images/',$lang,'/help_icon.gif')"/>
            <xsl:variable name="helpAlt">
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_cmpHelp.help.alt@Help'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </xsl:variable>
	    <script language="javaScript1.2" src="../sys_resources/js/browser.js">;</script>
            <script language="JavaScript1.2">
 	        
 	        var helpSetFile = "../../Docs/Rhythmyx/Business_Users/Content_Explorer_Help.hs";
	        var helpId = "<xsl:value-of select="$helpId"/>";
	        var helpIcon = "<xsl:value-of select="$helpIcon"/>";
	        var helpAlt = "<xsl:value-of select="$helpAlt"/>";
		                    	
            </script>
	     <script language="JavaScript1.2"><![CDATA[<!--   
	        
	        var _codebase = "]]><xsl:value-of select="//@codebase"/><![CDATA[";
	        var _classid = "]]><xsl:value-of select="//@classid"/><![CDATA[";
	        var _type = "]]><xsl:value-of select="concat('application/x-java-applet;',//@version_type,'=',//@implementation_version)"/><![CDATA[";
	        var _pluginpage = "]]><xsl:value-of select="concat('http://java.sun.com/products/plugin/',//@implementation_version,'/plugin-install.html')"/><![CDATA[";
	        var _appletcodebase = "]]><xsl:value-of select="//@appletcodebase"/><![CDATA[";

			  var appletCaller = new AppletCaller();
	        
	        appletCaller.addParam("name", "help");
	        appletCaller.addParam("id", "help");
	        appletCaller.addParam("width", "0");
	        appletCaller.addParam("height", "0");
	        appletCaller.addParam("align", "baseline");
	        appletCaller.addParam("codebase", _appletcodebase);
	        appletCaller.addParam("archive", "help.jar,jh.jar");
	        appletCaller.addParam("code", "com.percussion.tools.help.PSHelpApplet");
	        appletCaller.addParam("MAYSCRIPT", "true");
	        appletCaller.addParam("classid", _classid);
	        appletCaller.addParam("codebaseattr", _codebase);
	        appletCaller.addParam("type", _type);
	        appletCaller.addParam("scriptable", "true");
	        appletCaller.addParam("pluginspage", _pluginpage);
	        appletCaller.addParam("helpset_file", helpSetFile);
	        appletCaller.addParam("helpId", helpId);
	        appletCaller.show();         

           //-->]]></script>

           <a href="javascript:void(0)">
           <xsl:attribute name="onclick">
                 <xsl:choose>
                   <xsl:when test="starts-with(pagename, 'wf_')">
                     <xsl:text>_showWorkflowTabHelp('About_Workflows_in_Rhythmyx.htm');</xsl:text>
                   </xsl:when>
                   <xsl:otherwise>
                     <xsl:text>_showHelp();</xsl:text>
                   </xsl:otherwise>
                 </xsl:choose>
           </xsl:attribute>
           <img align="absmiddle" alt="{$helpAlt}" border="0" src="{$helpIcon}"/></a>
         </td>
      </tr>
      <tr class="outerboxcellfont ">
         <td align="left" class="outerboxcellfont">
            <img border="0" height="2" src="../sys_resources/images/invis.gif"/>
         </td>
      </tr>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cmpHelp.help@Help">Title for the Help component page.</key>
      <key name="psx.sys_cmpHelp.help.alt@Help">Alt text for help image.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
