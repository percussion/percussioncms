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
   <xsl:variable name="lang" select="//@lang"/>
   <xsl:include href="file:sys_resources/stylesheets/viewpaging_lang.xsl"/>
   <xsl:variable name="this" select="/"/>
   <xsl:variable name="count" select="count(/resultpage/search/item[assignmenttype &gt; 1])"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.0"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_relatedSearch.resultpage@Rhythmyx - Content Editor - Related Content Search'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('/rx_resources/css/',$lang,'/templates.css')}"/>
            <script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
            <script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
            <script>
               function onClickCancel()
               {
                  if(window.opener)
                  {
                     window.opener.name="";
                  }
                  self.close();
               }
               function onClickSearchAgain()
               {
                  window.history.back();
               }
               function delSubmit()
               {
                  form=document.updaterelateditems;
                  len = form.elements.length;
                  var i=0;
                  var nchecked = 0;
                  for(i=0; i&lt;len; i++) 
                  {
                     if(form.elements[i].checked==true)
                     {
                        nchecked=1;
                        break;
                     }
                  } 
                  if(nchecked==0)
                  {
                     alert(LocalizedMessage("select_one_item_before_inserting"));
                     return false;
                  }
                  return true;
               }
               
               function onClickUpdate()
               {
                  if(!window.opener || window.opener.closed)
                  {
                     self.close();
                     return;
                  }
                  if(!delSubmit()) return;
                  
                  window.opener.name="rceditor";
                  document.updaterelateditems.target = "rceditor";
                  var caller = window.opener.location.href;
                  
                  document.updaterelateditems.sys_activeitemid.value = parseParam("sys_activeitemid", caller); 
                  document.updaterelateditems.sys_contentid.value = parseParam("sys_contentid", caller); 
                  document.updaterelateditems.sys_revision.value = parseParam("sys_revision", caller); 
                  document.updaterelateditems.sys_authtype.value = parseParam("sys_authtype", caller); 
                  document.updaterelateditems.sys_context.value = parseParam("sys_context", caller); 
                  document.updaterelateditems.sys_variantid.value = parseParam("sys_variantid", caller); 
                  
                  document.updaterelateditems.httpcaller.value = caller;
                  document.updaterelateditems.action = "../sys_rcSupport/updaterelateditems.html";
                  document.updaterelateditems.submit();
                  setTimeout("self.close()",200);
               }
               
               function parseParam(param, href)
               {
                  var value = "";
                  if(param == null || param=="")
                     return value;
                  var index = href.indexOf(param);
                  if(index == -1)
                     return value;
                  value = href.substring(index+param.length+1);
                  index = value.indexOf("&amp;");
                  if(index == -1)
                     return value;
                  value = value.substring(0, index);
                  return value;
               }
            </script>
         </head>
         <body onload="javascript:self.focus();">
            <form name="updaterelateditems" method="post" action="">
               <input type="hidden" name="sys_command" value="update"/>
               <input type="hidden" name="sys_contentid" value=""/>
               <input type="hidden" name="sys_revision" value=""/>
               <input type="hidden" name="sys_slotid" value="{/*/slotid}"/>
               <input type="hidden" name="sys_variantid" value=""/>
               <input type="hidden" name="sys_context" value=""/>
               <input type="hidden" name="sys_authtype" value=""/>
               <input type="hidden" name="httpcaller" value=""/>
               <input type="hidden" name="sys_activeitemid" value=""/>
               <div align="center">
                  <table width="100%" cellpadding="0" cellspacing="3" border="0">
                     <xsl:choose>
                        <xsl:when test="$count &gt; 0">
                           <xsl:apply-templates select="/*/search[item/assignmenttype &gt; 1]" mode="mode2"/>
                        </xsl:when>
                        <xsl:otherwise>
                           <tr class="datacell1">
                              <td class="datacellnoentriesfound" colspan="4" align="center">
                                 <xsl:call-template name="getLocaleString">
                                    <xsl:with-param name="key" select="'psx.generic@No entries found'"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                 </xsl:call-template>.
                              </td>
                           </tr>
                        </xsl:otherwise>
                     </xsl:choose>
                     <tr class="headercell">
                        <td align="center">
                           <xsl:apply-templates select="/" mode="paging">
                              <xsl:with-param name="lang" select="$lang"/>
                           </xsl:apply-templates>&nbsp;
					      </td>
                     </tr>
                  </table>
               </div>
               <table width="100%" cellpadding="0" cellspacing="3" border="0">
                  <tr>
                     <td align="center">
                        <xsl:if test="$count &gt; 0">
                           <input type="button" name="update" value="Update" onclick="javascript:onClickUpdate()">
                              <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Update'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                           </input>&nbsp;
                        </xsl:if>
                        <input type="button" name="cancel" value="Close" onclick="javascript:onClickCancel()">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>&nbsp;
					         </td>
                  </tr>
               </table>
            </form>
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
   <xsl:template match="search" mode="mode2">
      <table width="100%" cellpadding="4" cellspacing="0" border="0">
         <tr class="headercell">
            <td align="left" class="headercellfont">
               <xsl:if test="string-length(variantid)">
                  <xsl:value-of select="variantname"/>(<xsl:value-of select="variantid"/>)
               </xsl:if>&nbsp;
            </td>
         </tr>
         <tr>
            <td valign="top" class="headercell">
               <table width="100%" border="0" cellspacing="1" cellpadding="0">
                  <tr class="headercell2">
                     <td class="headercell2font" align="center">&nbsp;</td>
                     <td width="60%" class="headercell2font" align="center">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.resultpage@Content Title (ID)'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>&nbsp;&nbsp;&nbsp;
                     </td>
                     <td class="headercell2font" align="center">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_relatedSearch.resultpage@Content Type'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                     </td>
                     <td class="headercell2font" align="center" width="40">&nbsp;</td>
                  </tr>
                  <xsl:if test="string-length(variantid)">
                     <xsl:apply-templates select="item" mode="item"/>
                  </xsl:if>
                  <xsl:if test="not(string-length(variantid))">
                     <tr class="datacell1">
                        <td class="datacellnoentriesfound" colspan="4" align="center">
                           <xsl:call-template name="getLocaleString">
                              <xsl:with-param name="key" select="'psx.generic@No entries found'"/>
                              <xsl:with-param name="lang" select="$lang"/>
                           </xsl:call-template>.
                        </td>
                     </tr>
                  </xsl:if>
               </table>
            </td>
         </tr>
      </table>
   </xsl:template>
   <xsl:template match="item" mode="item">
      <tr class="datacell1">
         <td align="left" class="datacell1font">
            <input type="checkbox" name="conidvarid" value="{conidvarid}"/>
         </td>
         <td align="left" class="datacell1font">
            <xsl:value-of select="contentname"/>(<xsl:value-of select="contentid"/>)</td>
         <td align="left" class="datacell1font">
            <xsl:value-of select="contenttype"/>
         </td>
         <td align="center" class="datacell1font">
            <a>
               <xsl:attribute name="href">javascript:{}</xsl:attribute>
               <xsl:attribute name="onclick">javascript:window.open(&quot;<xsl:value-of select="previewurl"/>&quot;,&quot;preview&quot;, &quot;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=400,height=300,z-lock=1&quot;)</xsl:attribute>
               <img src="/sys_resources/images/preview.gif" alt="Preview" align="top" border="0">
                  <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_relatedSearch.resultpage.alt@Preview'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
               </img>
            </a>
         </td>
      </tr>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_relatedSearch.resultpage@Rhythmyx - Content Editor - Related Content Search">Title for Related Content Search result page.</key>
      <key name="psx.sys_relatedSearch.resultpage@Content Title (ID)">Second column header in related content search result page.</key>
      <key name="psx.sys_relatedSearch.resultpage@Content Type">Third column header in related content search result page.</key>
      <key name="psx.sys_relatedSearch.resultpage.alt@Preview">Alt text for preview image.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
