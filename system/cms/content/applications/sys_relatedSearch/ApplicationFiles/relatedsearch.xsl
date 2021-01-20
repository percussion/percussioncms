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
   <xsl:variable name="this" select="/"/>
   <xsl:variable name="searchdefcommelem" select="document('../rxconfig/Server/config.xml')/*/BrowserUISettings/SearchSettings/@useCurrentCommunity"/>
   <xsl:variable name="searchdefcomm">
      <xsl:choose>
         <xsl:when test="$searchdefcommelem">
            <xsl:value-of select="$searchdefcommelem"/>
         </xsl:when>
         <xsl:otherwise>yes</xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="contenttypelookup" select="document(/*/contenttypelookupurl)"/>
   <xsl:variable name="CreateStatuslookup" select="/*/statusurl"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Rhythmyx - Content Editor - Related Content Search'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
            <script src="../sys_relatedSearch/searchinit.js" language="javascript">;</script>
            <script src="../sys_resources/js/calPopup.js" language="javascript">;</script>
            <script src="../sys_resources/js/formValidation.js" language="javascript">;</script>
            <script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
            <script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
            <xsl:if test="//logincommunity!=0">
               <script language="javascript" ><![CDATA[
                  function contenttype(id, name)
                  {
                      this.id = id;
                      this.name = name;
                  }
   
                  function community(id, name, contenttypes)
                  {
                      this.id = id;
                      this.name = name;
                      this.contenttypes = contenttypes;
                  }
                  ]]></script>
               <xsl:variable name="communitycontentlookup" select="document(//communitycontentlookupurl)"/>
               <!-- begin XSL -->
               <xsl:element name="script">
                  <xsl:attribute name="language">javascript</xsl:attribute>
                  <xsl:text>communities = new Array(</xsl:text>
                  <xsl:for-each select="$communitycontentlookup//community">
                     <xsl:text>new community(</xsl:text>
                     <xsl:value-of select="id"/>
                     <xsl:text>, &quot;</xsl:text>
                     <xsl:value-of select="name"/>
                     <xsl:text>&quot;, new Array(</xsl:text>
                     <xsl:for-each select="contenttype[displayname=$contenttypelookup//slotlookup/item/display]">
                        <xsl:text>new contenttype(</xsl:text>
                        <xsl:value-of select="id"/>
                        <xsl:text>,&quot;</xsl:text>
                        <xsl:value-of select="displayname"/>
                        <xsl:text>&quot;)</xsl:text>
                        <xsl:if test="not(position() = last())">
                           <xsl:text>,</xsl:text>
                        </xsl:if>
                     </xsl:for-each>))
               <xsl:if test="not(position() = last())">
                        <xsl:text>,</xsl:text>
                     </xsl:if>
                  </xsl:for-each>
                  <xsl:text>);</xsl:text>
                  <xsl:text>allcontenttypes = new Array(</xsl:text>
                  <xsl:for-each select="$contenttypelookup//slotlookup/item">
                     <xsl:text>&quot;</xsl:text>
                     <xsl:value-of select="display"/>
                     <xsl:text>&quot;</xsl:text>
                     <xsl:if test="not(position() = last())">
                        <xsl:text>,</xsl:text>
                     </xsl:if>
                  </xsl:for-each>
                  <xsl:text>);</xsl:text>
                  <xsl:text>allcontentids = new Array(</xsl:text>
                  <xsl:for-each select="$contenttypelookup//slotlookup/item">
                     <xsl:text>&quot;</xsl:text>
                     <xsl:value-of select="id"/>
                     <xsl:text>&quot;</xsl:text>
                     <xsl:if test="not(position() = last())">
                        <xsl:text>,</xsl:text>
                     </xsl:if>
                  </xsl:for-each>
                  <xsl:text>);</xsl:text>
               </xsl:element>
               <!-- end XSL -->
            </xsl:if>
            <script id="clientEventHandlersJS" language="javascript" ><![CDATA[
            function Search_onclick() 
            {
               if(!dateValidate(document.updatesearch.sys_enddate.value)){
                  return false;
               }
               if(!dateValidate(document.updatesearch.sys_startdate.value)){
                  return false;
               }
            
               document.updatesearch.sys_searchcontenttitle.value="";
               document.updatesearch.sys_searchauthor.value="";
               if (document.updatesearch.sys_contenttitle.value.length > 0)
               {
                  document.updatesearch.sys_searchcontenttitle.value = "%" + document.updatesearch.sys_contenttitle.value + "%";
               }
            
               if (document.updatesearch.sys_author.value.length > 0)
               {
                  document.updatesearch.sys_searchauthor.value = "%" + document.updatesearch.sys_author.value + "%";
               }
               if(document.updatesearch.sys_casesensitive.checked){
                  document.updatesearch.action="resultpage_dbsensitive.html"
               }
               else {
                  document.updatesearch.action="resultpage.html"
               }
                  
               document.updatesearch.submit();
            }
            
            function Reset_onclick() {
            document.updatesearch.sys_contenttitle.value=""
            document.updatesearch.sys_author.value=""
            document.updatesearch.sys_contenttype.value=""
            document.updatesearch.sys_enddate.value=""
            document.updatesearch.sys_startdate.value=""
            document.updatesearch.sys_contentid.value=""
            document.updatesearch.sys_status.value=""
            }
         ]]></script>
         </head>
         <body bgcolor="0xffffff" onload="javascript:self.focus();">
            <!--   psx-docalias="contenttypelookup" psx-docref="psx-contenttypelookupurl"   -->
            <form name="updatesearch" method="post">
               <input type="hidden" name="logincommunity" value="{//logincommunity}"/>
               <input type="hidden" name="searchdefcomm" value="{$searchdefcomm}"/>
               <input type="hidden" name="sys_slotid" value="{*/slotid}"/>
               <input type="hidden" name="sys_revision" value="{*/revision}"/>
               <table align="center" width="75%" border="0" cellspacing="1" cellpadding="0" class="headercell">
                  <tr class="outerboxcell">
                     <td colspan="2" align="center" class="outerboxcellfont">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Search Related Items for'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                     </td>
                  </tr>
                  <tr class="outerboxcell">
                     <td colspan="2" align="center" class="outerboxcellfont">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Slot (ID)'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:&nbsp;
                        <xsl:apply-templates select="*/slotname"/>(<xsl:apply-templates select="*/slotid"/>)
                     </td>
                  </tr>
                  <tr class="headercell">
                     <td colspan="2" align="left" class="outerboxcellfont">
                        <br id="XSpLit"/>
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Search Parameters'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                     </td>
                  </tr>
                  <tr class="datacell2">
                     <td align="left" class="datacell1font">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Title'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1">
                        <input type="hidden" name="sys_searchcontenttitle" value="{*/contenttitle}"/>
                        <input size="20" type="text" name="sys_contenttitle" class="datadisplay" value="{*/contenttitle}"/>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td align="left" class="datacell1font">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@ID'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1font">
                        <input size="15" type="text" name="sys_contentid">
                        </input>
                     </td>
                  </tr>
                  <xsl:choose>
                     <xsl:when test="//logincommunity!=0">
                        <tr class="datacell1" border="0">
                           <td align="left" class="datacell1font">
                              <xsl:call-template name="getLocaleString">
                                 <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Community'"/>
                                 <xsl:with-param name="lang" select="$lang"/>
                              </xsl:call-template>:
                           </td>
                           <td align="left" class="datacell1font">
                              <select name="sys_communityid" onchange="return community_onchange()">
                                 <option>--- Select from list ---</option>
                                 <option>1</option>
                                 <option>2</option>
                                 <option>3</option>
                                 <option>4</option>
                                 <option>5</option>
                              </select>
                           </td>
                        </tr>
                        <tr class="datacell1" border="0">
                           <td align="left" class="datacell1font">
                              <xsl:call-template name="getLocaleString">
                                 <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Content Type'"/>
                                 <xsl:with-param name="lang" select="$lang"/>
                              </xsl:call-template>:
                           </td>
                           <td align="left" class="datacell1font">
                              <select name="sys_contenttype">
                                 <option>--- Select from list ---</option>
                                 <option>1</option>
                                 <option>2</option>
                                 <option>3</option>
                                 <option>4</option>
                                 <option>5</option>
                              </select>
                           </td>
                        </tr>
                     </xsl:when>
                     <xsl:otherwise>
                        <tr class="datacell1">
                           <td class="datacell1" align="left">
                              <xsl:call-template name="getLocaleString">
                                 <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Content Type'"/>
                                 <xsl:with-param name="lang" select="$lang"/>
                              </xsl:call-template>:
                           </td>
                           <td class="datacell1font">
                              <select name="sys_contenttype" class="datadisplay">
                                 <option value=""/>
                                 <xsl:apply-templates select="$contenttypelookup" mode="contenttypelookup"/>
                              </select>
                           </td>
                        </tr>
                     </xsl:otherwise>
                  </xsl:choose>
                  <xsl:apply-templates select="document($CreateStatuslookup)/*" mode="mode3">
                     <xsl:with-param name="stype" select="/*/status"/>
                  </xsl:apply-templates>
                  <tr class="datacell2">
                     <td align="left" class="datacell1font">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Author'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1">
                        <input type="hidden" name="sys_searchauthor" value="{*/author}"/>
                        <input size="20" type="text" name="sys_author" class="datadisplay" value="{*/author}"/>
                     </td>
                  </tr>
                  <tr class="datacell2">
                     <td align="left" class="datacell1font">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Start Date Before'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1">
                        <input type="text" name="sys_enddate" size="10" class="datadisplay" value="{*/enddate}"/>&nbsp;
		          <a href="javascript:doNothing()" onclick="showCalendar(document.updatesearch.sys_enddate);">
                           <img border="0" height="20" width="20" src="../sys_resources/images/cal.gif">
                              <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch.alt@Calendar Pop-up'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                           </img>
                        </a>
                     </td>
                  </tr>
                  <tr class="datacell2">
                     <td align="left" class="datacell1font">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Start Date After'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1">
                        <input type="text" name="sys_startdate" size="10" class="datadisplay" value="{*/startdate}"/>&nbsp;
					            <a href="javascript:doNothing()" onclick="showCalendar(document.updatesearch.sys_startdate);">
                           <img border="0" height="20" width="20" src="../sys_resources/images/cal.gif">
                              <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch.alt@Calendar Pop-up'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                           </img>
                        </a>
                     </td>
                  </tr>
                  <tr class="datacell2">
                     <td align="left" class="datacell1font">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Case Sensitive'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1">
                        <input type="checkbox" name="sys_casesensitive"/>
                     </td>
                  </tr>
                  <tr class="datacell2">
                     <td colspan="2" align="center">
                        <br id="XSpLit"/>
                        <input type="button" name="Search" value="Search" class="nav_body" onclick="return Search_onclick();">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Search'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>&nbsp;
                        <input type="button" name="Reset" value="Reset" class="nav_body" language="javascript" onclick="return Reset_onclick()">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Reset'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>&nbsp;
                        <input type="button" value="Close" class="nav_body" onclick="javascript:window.close();">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>
                        <br id="XSpLit"/>&nbsp;
                      </td>
                  </tr>
               </table>
            </form>
            <xsl:if test="//logincommunity!=0">
               <script language="javascript">
	               javascript:onFormLoad();
               </script>
            </xsl:if>
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
   <xsl:template match="*" mode="contenttypelookup">
      <xsl:for-each select="item">
         <option>
            <xsl:variable name="value">
               <xsl:value-of select="id"/>
            </xsl:variable>
            <xsl:if test="$this/relatedsearch/selcontenttype=$value">
               <xsl:attribute name="selected"/>
            </xsl:if>
            <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
            <xsl:apply-templates select="display"/>
         </option>
      </xsl:for-each>
   </xsl:template>
   <xsl:template match="*" mode="mode3">
      <xsl:param name="stype"/>
      <tr class="datacell1">
         <td class="datacell1font" align="left">
            <xsl:call-template name="getLocaleString">
               <xsl:with-param name="key" select="'psx.sys_relatedSearch.relatedsearch@Status'"/>
               <xsl:with-param name="lang" select="$lang"/>
            </xsl:call-template>:
         </td>
         <td class="datacell1font">
            <select name="sys_status">
               <option value="">
                  <xsl:if test="$stype=''">
                     <xsl:attribute name="selected"/>
                  </xsl:if>
               </option>
               <xsl:for-each select="item">
                  <option>
                     <xsl:attribute name="value"><xsl:value-of select="name"/></xsl:attribute>
                     <xsl:if test="$stype=name">
                        <xsl:attribute name="selected"/>
                     </xsl:if>
                     <xsl:value-of select="name"/>
                  </option>
               </xsl:for-each>
            </select>
         </td>
      </tr>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_relatedSearch.relatedsearch@Rhythmyx - Content Editor - Related Content Search">Title for the Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Search Related Items for">Main header text for Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Search Parameters">Header for the related content search box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Slot (ID)">Sub header text for Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Title">Title label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@ID">ID label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Community">Community label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Content Type">Content Type label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Status">Status label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Author">Author label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Start Date Before">Start Date Before label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Start Date After">Start Date After label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch.alt@Calendar Pop-up">Alt text for calender image in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Search">Search button label in Related Content Search dialog box.</key>
      <key name="psx.sys_relatedSearch.relatedsearch@Reset">Reset button label in Related Content Search dialog box.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
