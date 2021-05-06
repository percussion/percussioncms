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
   <xsl:variable name="searchurl" select="document(//searchurl)//url"/>
   <xsl:variable name="showRelationshiptype"><xsl:value-of select="//Relationships/@showRelationshiptype"/></xsl:variable>
   <!-- begin XSL -->
   <xsl:template match="Item[string-length(@contentid)=0]" mode="childitem" priority="10"/>
   <xsl:template match="Item" mode="childitem" priority="6">
      <xsl:if test="@relationshiptype=$showRelationshiptype">
         <tr class="datacell1">
            <td class="datacell1font">
               <a href="javascript:void(0);">
                  <xsl:attribute name="onclick">javascript:openCA("<xsl:value-of select="concat($searchurl,'&amp;statusid=',@contentid)"/>")</xsl:attribute>
                  <xsl:value-of select="text"/>
               </a>&nbsp;:&nbsp;
            <xsl:if test="@public='y'">
                  <b>
                     <xsl:value-of select="@state"/>
                  </b>
               </xsl:if>
               <xsl:if test="@public!='y'">
                  <xsl:value-of select="@state"/>
               </xsl:if>
               <xsl:if test="@repeat='y'">
                  <xsl:text>&nbsp;*</xsl:text>
               </xsl:if>
            </td>
         </tr>
      </xsl:if>
   </xsl:template>
   <xsl:template match="Item"/>
   <xsl:template match="Workflow[@stateValid='y']" mode="childitem">
      <b>
         <xsl:value-of select="State"/>
      </b>
   </xsl:template>
   <xsl:template match="Workflow" mode="childitem">
      <xsl:value-of select="State"/>
   </xsl:template>
   <!-- end XSL -->
   <xsl:include href="file:sys_resources/stylesheets/UserStatusA.xsl"/>
   <xsl:include href="file:sys_resources/stylesheets/Banner.xsl"/>
   <xsl:variable name="this" select="/"/>
   <xsl:variable name="createcontentlookup" select="/*/createcontentlookupurl"/>
   <xsl:variable name="userstatusinclude" select="/*/userstatusincludeurl"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_ceDependency.Depend@Rhythmyx - Content Editor - Dependency Viewer'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
            <script language="javascript" src="../sys_resources/js/href.js">;</script>
            <script language="javascript">
            function openCA(url)
            {
               if(!window.opener || window.opener.closed)
                  window.open(url);
               else
                  window.opener.location.href=url;
               self.close();
            }

            /**
             * Refreshes the current window with the supplied url for the 
             * provided relationship type.
             * 
             * @param url the page to refresh, not <code>null</code>.
             * @param relationshiptype the relationship type to refresh the 
             *    page for, not <code>null</code>.
             */
            function refresh(url, relationshiptype)
            {
               var h = PSHref2Hash(url.href);
               h["sys_relationshiptype"] = relationshiptype;
                  
               window.location.href = PSHash2Href(h);
            }
         </script>
         </head>
         <body leftmargin="5" rightmargin="5" topmargin="5">
            <xsl:attribute name="sessionid"><xsl:value-of select="*/sessionid"/></xsl:attribute>
            <!--   psx-docalias="userstatusinclude" psx-docref="psx-userstatusincludeurl"   -->
            <!--   psx-docalias="createcontentlookup" psx-docref="psx-createcontentlookupurl"   -->
            <table width="100%" cellpadding="0" cellspacing="1" border="0" class="headercell">
               <xsl:apply-templates select="*" mode="mode10"/>
            </table>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="*">
      <xsl:choose>
         <xsl:when test="text()">
            <xsl:choose>
               <xsl:when test="@no-escaping">
                  <xsl:value-of select="." disable-output-escaping="yes"/>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="."/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise>&nbsp;</xsl:otherwise>
      </xsl:choose>
      <xsl:if test="not(position()=last())">
         <br id="XSpLit"/>
      </xsl:if>
   </xsl:template>
   <xsl:template match="attribute::*">
      <xsl:value-of select="."/>
      <xsl:if test="not(position()=last())">
         <br id="XSpLit"/>
      </xsl:if>
   </xsl:template>
   <xsl:template match="*" mode="mode0">
      <xsl:for-each select=".">
         <tr class="datacell1">
            <td valign="top" align="left" class="outerboxcellfont">
               <br id="XSpLit"/>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_ceDependency.Depend@Parent Items of'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
               :&nbsp;
          <xsl:apply-templates select="title"/>(
          <xsl:apply-templates select="contentid"/>)&nbsp;:&nbsp;
          <xsl:apply-templates select="contenttypename"/>:&nbsp;
          <xsl:apply-templates select="statename"/>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode1">
      <xsl:for-each select=".">
         <tr>
            <td>
               <xsl:attribute name="id"><xsl:value-of select="."/></xsl:attribute>
               <table width="100%" cellpadding="0" cellspacing="0" border="0">
                  <!-- begin XSL -->
                  <xsl:apply-templates select="Item" mode="childitem"/>
                  <!-- end XSL -->
               </table>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode2">
      <xsl:for-each select=".">
         <tr>
            <td valign="top" class="datacell1">
               <table width="100%" cellpadding="0" cellspacing="0" border="0">
                  <xsl:apply-templates select="." mode="mode1"/>
               </table>
               <!--   comment   -->
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="parents" mode="mode3">
      <xsl:for-each select=".">
         <tr class="datacell1">
            <td valign="top" align="center">
               <table width="100%" cellpadding="0" cellspacing="0" border="0">
                  <xsl:apply-templates select="." mode="mode2"/>
               </table>
               <!--   View End   -->
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode4">
      <xsl:for-each select=".">
         <tr class="datacell1">
            <td valign="top" align="left" class="headercellfont">
               <em>&nbsp;* 
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.sys_ceDependency.Depend@indicates branch already expanded elsewhere'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>
               </em>
            </td>
         </tr>
         <xsl:apply-templates select="." mode="mode0"/>
         <xsl:if test="not(parents='')">
            <xsl:apply-templates select="parents" mode="mode3"/>
         </xsl:if>
         <xsl:if test="parents=''">
            <tr class="datacell1">
               <td class="datacellnoentriesfound" colspan="10" align="center">
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.generic@No entries found'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>.
						</td>
            </tr>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode5">
      <xsl:for-each select=".">
         <tr class="datacell1">
            <!--   Repeats once per category   -->
            <td valign="top" align="left" class="outerboxcellfont">
               <br id="XSpLit"/>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_ceDependency.Depend@Child Items of'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>:&nbsp;
               <xsl:apply-templates select="title"/>(
               <xsl:apply-templates select="contentid"/>)&nbsp;:&nbsp;
               <xsl:apply-templates select="contenttypename"/>:&nbsp;
               <xsl:apply-templates select="statename"/>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode6">
      <xsl:for-each select=".">
         <tr>
            <td>
               <xsl:attribute name="id"><xsl:value-of select="."/></xsl:attribute>
               <table width="100%" cellpadding="0" cellspacing="0" border="0">
                  <!-- begin XSL -->
                  <xsl:apply-templates select="Item" mode="childitem"/>
                  <!-- end XSL -->
               </table>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode7">
      <xsl:for-each select=".">
         <tr>
            <td valign="top" class="datacell1">
               <table width="100%" cellpadding="0" cellspacing="0" border="0">
                  <xsl:apply-templates select="." mode="mode6"/>
               </table>
               <!--   comment   -->
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="children" mode="mode8">
      <xsl:for-each select=".">
         <tr class="datacell1">
            <td valign="top" align="center">
               <xsl:attribute name="id"><xsl:value-of select="."/></xsl:attribute>
               <table width="100%" cellpadding="4" cellspacing="0" border="0">
                  <xsl:apply-templates select="." mode="mode7"/>
               </table>
               <!--   View End   -->
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode9">
      <xsl:for-each select=".">
         <xsl:apply-templates select="." mode="mode5"/>
         <xsl:if test="not(children='')">
            <xsl:apply-templates select="children" mode="mode8"/>
         </xsl:if>
         <xsl:if test="children=''">
            <tr class="datacell1">
               <td class="datacellnoentriesfound" colspan="10" align="center">
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.generic@No entries found'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>.
						</td>
            </tr>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>
   <!--
      This template displays a drop down control with all relationship types
      available in the system. By default 'Related Content' is selected. If
      the selection is changed, the window will be refreshed for the new 
      selected relationship type.
   -->
   <xsl:template match="Relationships">
      <select onchange="javascript:refresh(window.location, this.value)" name="sys_relationshiptype" class="datadisplay">
         <xsl:for-each select="Relationship">
            <xsl:variable name="id" select="."/>
            <xsl:variable name="selected" select="./@selected"/>
            <option value="{$id}">
               <xsl:if test="./@selected='yes'">
                  <xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
               </xsl:if>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="concat(psx.relationship.type, $id)"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </option>
         </xsl:for-each>
      </select>
   </xsl:template>
   <xsl:template match="*" mode="mode10">
      <xsl:for-each select=".">
         <tr>
            <xsl:attribute name="id"><xsl:value-of select="revision"/></xsl:attribute>
            <td class="outerboxcell" align="right" valign="top">
               <table width="100%" cellpadding="0" cellspacing="1" border="0" class="headercell">
                  <tr>
                     <td align="left" valign="top">
                        <xsl:apply-templates select="Relationships"/>
                     </td>
                     <td align="right" valign="top">
                        <span class="outerboxcellfont">
                           <xsl:call-template name="getLocaleString">
                              <xsl:with-param name="key" select="'psx.sys_ceDependency.Depend@Dependency Viewer'"/>
                              <xsl:with-param name="lang" select="$lang"/>
                           </xsl:call-template>
                        </span>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
         <tr>
            <td width="100%" height="1">
               <img src="../sys_resources/images/invis.gif" width="1" height="1" border="0"/>
            </td>
         </tr>
         <xsl:apply-templates select="." mode="mode4"/>
         <xsl:apply-templates select="." mode="mode9"/>
         <tr class="datacell1">
            <td height="100%" align="center">
               <input type="button" name="gotoparent" value="Close" language="javascript" onclick="window.close()">
                  <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
               </input>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_ceDependency.Depend@Rhythmyx - Content Editor - Dependency Viewer">Title for Dependency viewer.</key>
      <key name="psx.sys_ceDependency.Depend@Parent Items of">Header for parent items.</key>
      <key name="psx.sys_ceDependency.Depend@indicates branch already expanded elsewhere">Note explaining branch already expanded elsewhere.</key>
      <key name="psx.sys_ceDependency.Depend@Child Items of">Header for child items.</key>
      <key name="psx.sys_ceDependency.Depend@Dependency Viewer">Main header for Dependency Viewer.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
