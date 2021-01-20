<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
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
                  <xsl:with-param name="key" select="'psx.sys_cmpCaSavedSearches.savedsearches@Saved Searches Component'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
         </head>
         <body>
		<table width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
               <tr class="datacell1" border="0">
                  <td valign="top" class="outerboxcellfont">
                     &nbsp;<hr/>
                     <xsl:call-template name="getLocaleString">
                        <xsl:with-param name="key" select="'psx.sys_cmpCaSavedSearches.savedsearches@Saved Searches'"/>
                        <xsl:with-param name="lang" select="$lang"/>
                     </xsl:call-template>
                  </td>
               </tr>
               <tr class="datacell1" border="0">
                  <td valign="top" align="center" class="headercell2font">
                     <form>
                        <input type="button" value="View Saved Searches">
                           <xsl:attribute name="onclick">javascript:window.open(&#34;<xsl:value-of select="/*/searchlisturl"/>&#34;,&#34;savedsearches&#34;,&#34;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=500,height=400,z-lock=1&#34;)</xsl:attribute>
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cmpCaSavedSearches.savedsearches@View Saved Searches'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>
                     </form>
                  </td>
               </tr>
               <tr class="headercell">
                  <td height="100%" colspan="2">&nbsp;</td>
                  <!--   Fill down to the bottom   -->
               </tr>
	  </table>
         </body>
      </html>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cmpCaSavedSearches.savedsearches@Saved Searches Component">Title for the Saved Searches component page.</key>
      <key name="psx.sys_cmpCaSavedSearches.savedsearches@Saved Searches">Saved Searches label in Saved Searches component page.</key>
      <key name="psx.sys_cmpCaSavedSearches.savedsearches@View Saved Searches">View Saved Searches button label in Saved Searches component page.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
