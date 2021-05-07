<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:include href="file:sys_resources/stylesheets/viewpaging_lang.xsl"/>
   <xsl:variable name="this" select="/"/>
   <xsl:template match="/">
      <xsl:variable name="searchurl" select="document(//searchurl)//url"/>
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.0"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_cmpCaSavedSearches.savedsearchlist@Rhythmyx - Content Editor - Saved Searches'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
            <script src="../sys_resources/js/formValidation.js"/>
         </head>
         <script language="javascript">
            function editSearch(searchquery)
            {
               if(!window.opener || window.opener.closed)
               {
                  self.close();
                  return;
               }
               searchurl = "<xsl:value-of select="$searchurl"/>";
               window.opener.location.href = searchurl.split("?")[0] + "?" + searchquery.split("?")[1] ;
               self.close();
            }
         </script>
         <body onload="javascript:self.focus();">
            <form name="deletesavedsearch" method="post" action="">
               <table width="100%" cellpadding="0" cellspacing="1" border="0" class="headercell">
                  <tr class="headercell">
                     <td align="center" class="outerboxcellfont" colspan="2">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSavedSearches.savedsearchlist@Saved Searches'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                     </td>
                  </tr>
                  <tr class="headercell2">
                     <td class="headercell2font" align="center">&nbsp;</td>
                     <td width="90%" class="headercell2font" align="left">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSavedSearches.savedsearchlist@Search Name'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                     </td>
                  </tr>
                  <xsl:choose>
                     <xsl:when test="count(/*/list)=1 and /*/list/searchid=''">
                        <tr class="datacell1">
                           <td class="datacellnoentriesfound" colspan="2" align="center">
                              <xsl:call-template name="getLocaleString">
                                 <xsl:with-param name="key" select="'psx.generic@No entries found'"/>
                                 <xsl:with-param name="lang" select="$lang"/>
                              </xsl:call-template>.
                           </td>
                        </tr>
                     </xsl:when>
                     <xsl:otherwise>
                        <xsl:apply-templates select="/*/list" mode="mode1"/>
                     </xsl:otherwise>
                  </xsl:choose>
                  <tr class="headercell">
                     <td align="center">
                        <xsl:apply-templates select="/" mode="paging">
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:apply-templates>&nbsp;
					      </td>
                  </tr>
                  <tr class="headercell">
                     <td align="center" colspan="2">
                        <input type="button" value="Close" onclick="window.close()">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>
                     </td>
                  </tr>
               </table>
            </form>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="list" mode="mode1">
      <tr class="datacell1">
         <td valign="top" class="datacell1font" align="center">
            <a href="javascript:delConfirm('{deletelink}')">
               <img src="../sys_resources/images/delete.gif" border="0">
                  <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cmpCaSavedSearches.savedsearchlist.alt@Delete'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
               </img>
            </a>
         </td>
         <td class="datacell1font">
            <a href="javascript:void(0);" onclick="javascript:editSearch('{concat(editlink,'&amp;searchname=',searchname)}')">
               <xsl:value-of select="searchname"/>
            </a>
         </td>
      </tr>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cmpCaSavedSearches.savedsearchlist@Rhythmyx - Content Editor - Saved Searches">Title for the Saved Searches dialog box, opens up when clicked on View Saved Searches button in CA search box.</key>
      <key name="psx.sys_cmpCaSavedSearches.savedsearchlist@Saved Searches">Main header in the Saved Searches dialog box.</key>
      <key name="psx.sys_cmpCaSavedSearches.savedsearchlist@Search Name">Column header in the Saved Searches dialog box.</key>
      <key name="psx.sys_cmpCaSavedSearches.savedsearchlist.alt@Delete">Alt text for delete image in the Saved Searches dialog box.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
