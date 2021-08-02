<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:output method="xml"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_cmpCaSaveSearch.casavesearch@Save Searche Component'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
         </head>
         <body>
            <script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
            <script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
            <script language="javaScript"><![CDATA[
				function savesearchfunction(){
					var sname = prompt(LocalizedMessage("save_search_prompt"),"");
					if (sname == null){
						return;
					}
					else if(sname==""){
						alert(LocalizedMessage("search_name") + " " + LocalizedMessage("Field_Required"));
						return;
					}
					else if(sname.indexOf("&")!=-1 || sname.indexOf("'")!=-1 || sname.indexOf("\"")!=-1)
					{
						alert(LocalizedMessage("special_char_alert"));
						return;
					}
					document.savesearch.searchname.value = sname; 
					document.savesearch.searchquery.value = document.location.href;
					document.savesearch.searchid.value = "";
					document.savesearch.submit();
				}
		 ]]></script>
            <xsl:apply-templates select="casavesearch"/>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="casavesearch">
      <form name="savesearch" method="get">
         <xsl:attribute name="action"><xsl:value-of select="savesearchurl"/></xsl:attribute>
         <input type="hidden" name="searchid"/>
         <input type="hidden" name="searchquery"/>
         <input type="hidden" name="searchname" value="{searchname}"/>
         <input type="hidden" name="sys_sortparam" value="title"/>
         <input type="hidden" name="sys_pagename" value="{pagename}"/>
         <input type="hidden" name="sys_componentname" value="{componentname}"/>
         <input type="hidden" name="DBActionType" value="UPDATE"/>
         <input type="hidden" name="username" value="{document(userrolesurl)/UserStatus/UserName}"/>
         <br/>
         <input type="button" value="Save Search" onClick="savesearchfunction();">
            <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cmpCaSaveSearch.casavesearch@Save Search'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
         </input>&nbsp;&nbsp;
  </form>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cmpCaSaveSearch.casavesearch@Save Searche Component">Title for the Save Search Component.</key>
      <key name="psx.sys_cmpCaSaveSearch.casavesearch@Save Search">Save Search button label.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
