<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html [
   <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial PUBLIC="-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
   %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:template match="/">
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_rcSupport.createnew@Create Item - Select Content Type and Template'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <meta name="Generator" content="EditPlus"/>
            <meta name="Author"/>
            <meta name="Keywords"/>
            <meta name="Description"/>
            <script language="javascript" ><![CDATA[
         			function variant(id, name)
         			{
         				 this.id = id;
         				 this.name = name;
         			}
         
         			function contenttype(id, name, url, variants)
         			{
         				 this.id = id;
         				 this.name = name;
         				 this.url = url;
         				 this.variants = variants;
         			}
      			]]></script>
            <!-- begin XSL -->
            <xsl:variable name="communityconotenttypes" select="document(//commconlookupurl)/*"/>
            <xsl:element name="script">
               <xsl:attribute name="language">javascript</xsl:attribute>
               <xsl:text>contenttypes = new Array(</xsl:text>
               <xsl:for-each select="//slot/contenttype[@contenttypename=$communityconotenttypes/contenttype/name]">
                  <xsl:text>new contenttype(</xsl:text>
                  <xsl:value-of select="@contenttypeid"/>
                  <xsl:text>, &quot;</xsl:text>
                  <xsl:value-of select="@contenttypename"/>
                  <xsl:text>&quot;, &quot;</xsl:text>
                  <xsl:value-of select="@contenttypeurl"/>
                  <xsl:text>&quot;, new Array(</xsl:text>
                  <xsl:variable name="variant"/>
                  <xsl:for-each select="variant">
                     <xsl:text>new variant(</xsl:text>
                     <xsl:value-of select="@variantid"/>
                     <xsl:text>,&quot;</xsl:text>
                     <xsl:value-of select="@variantname"/>
                     <xsl:text>&quot; )</xsl:text>
                     <xsl:if test="not(position() = last())">
                        <xsl:text>,</xsl:text>
                     </xsl:if>
                  </xsl:for-each>))
                  <xsl:if test="not(position() = last())">
                     <xsl:text>,</xsl:text>
                  </xsl:if>
               </xsl:for-each>
               <xsl:text>);</xsl:text>
            </xsl:element>
            <!-- end XSL -->
            <script language="javascript" ><![CDATA[
               function onFormLoad() 
               {
                   self.resizeTo(600, 401);
                   document.createnewitem.newcontenttypeid.options.length = contenttypes.length;
                   for(i=0; i<contenttypes.length; i++)
                   {
                       document.createnewitem.newcontenttypeid.options[i].value=contenttypes[i].id;
                       document.createnewitem.newcontenttypeid.options[i].text=contenttypes[i].name;
                   }
                   document.createnewitem.newcontenttypeid.options.selectedIndex=0;
               
                   contenttype_onchange();
               
                   self.focus();
               }
               
               function contenttype_onchange() 
               {
                   if(contenttypes.length < 1)
                      return;
                  
                   index = document.createnewitem.newcontenttypeid.options.selectedIndex;
                   variants = contenttypes[index].variants;
                   document.createnewitem.newvariantid.options.length = variants.length;
                   for(i=0; i<variants.length; i++)
                   {
                       document.createnewitem.newvariantid.options[i].value=variants[i].id;
                       document.createnewitem.newvariantid.options[i].text=variants[i].name;
                   }
                   document.createnewitem.newvariantid.options.selectedIndex=0;
               
               }
               
               function submitForm() 
               {
                   if(document.createnewitem.newcontenttypeid.options.length==0)
                   {
                     if(document.createnewitem.logincommunity.value!=0)
                           alert(LocalizedMessage("contenttypes_not_available"));
                     else
                           alert(LocalizedMessage("contenttypes_not_available_through_communities"));
                     return;
                   }
                   document.createnewitem.action = contenttypes[document.createnewitem.newcontenttypeid.options.selectedIndex].url;
                   document.createnewitem.action += addrcparams();
                   document.createnewitem.submit();
               }
               function addrcparams()
               {
                     	var rcparams = "";
               		var caller = window.opener.location.href;
               		var rcnew_activeitemid = parseParam("sys_activeitemid", caller); 
               		var rcnew_contentid = parseParam("sys_contentid", caller); 
               		var rcnew_revision = parseParam("sys_revision", caller); 
               		var rcnew_slotid = document.createnewitem.slotid.value;
               		var rcnew_itemvariantid = document.createnewitem.newvariantid[document.createnewitem.newvariantid.selectedIndex].value; 
               		rcparams += "&rc_createnew=yes";
               		rcparams += "&rcnew_activeitemid=" + rcnew_activeitemid;
               		if(document.createnewitem.rcnew_contentid.value!='')
               			rcparams += "&rcnew_contentid=" + document.createnewitem.rcnew_contentid.value;
               		else
               			rcparams += "&rcnew_contentid=" + rcnew_contentid;
                		if(document.createnewitem.rcnew_revision.value!='')
               			rcparams += "&rcnew_revision=" + document.createnewitem.rcnew_revision.value;
               		else
               			rcparams += "&rcnew_revision=" + rcnew_revision;
              			
               		rcparams += "&rcnew_revision=" + rcnew_revision;
               		rcparams += "&rcnew_slotid=" + rcnew_slotid;
               		rcparams += "&rcnew_itemvariantid=" + rcnew_itemvariantid; 
               		return rcparams;
               }
                  function parseParam(param, href)
                  {
                     var value = "";
                     if(param == null || param=="")
                        return value;
                     index = href.indexOf(param);
               		if(index==-1){
               			return value;
               		}
               		value = href.substring(index+param.length+1);
                     index = value.indexOf("&");
                     if(index == -1)
                        return value;
                     value = value.substring(0, index);
                     return value;
                  }
            ]]></script>
            <script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
            <script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
         </head>
         <body class="headercell" onload="javascript:onFormLoad()">
            <!--   psx-docalias="itemslotvariants" psx-docref="psx-itemslotvariantsurl"   -->
            <form method="post" name="createnewitem">
               <input type="hidden" name="sys_contenttypeid" select=""/>
               <input type="hidden" name="rcnew_contentid" value="{//@rcnew_contentid}"/>
               <input type="hidden" name="rcnew_revision" value="{//@rcnew_revision}"/>
               <input type="hidden" name="httpcaller"/>
               <table width="100%" border="0" cellspacing="1" cellpadding="0" class="headercell" summary="controls for editing metadata">
                  <!--  ItemContent  -->
                  <input type="hidden" name="slotid" value="{//@slotid}"/>
                  <input type="hidden" name="logincommunity" value="{//slot/logincommunity}"/>
                  <tr class="outerboxcell">
                     <td colspan="2" align="center" class="outerboxcellfont">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_rcSupport.createnew@Create Item'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td class="controlname" width="45%">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_rcSupport.createnew@Content Type'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td>
                        <select name="newcontenttypeid" onchange="return contenttype_onchange()"/>
                     </td>
                  </tr>
                  <tr class="datacell2">
                     <td class="controlname">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_rcSupport.createnew@Template'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td>
                        <select name="newvariantid"/>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td align="center" colspan="2">
                        <br/>
                        <input type="button" value="Create" class="nav_body" onclick="javascript:submitForm();">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_rcSupport.createnew@Create'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>&nbsp;&nbsp;
                        <input type="button" value="Close" class="nav_body" onclick="javascript:window.close();">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>
                     </td>
                  </tr>
               </table>
            </form>
         </body>
      </html>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_rcSupport.createnew@Create Item - Select Content Type and Variant">Title for Create Item dialog box opens up when clicked on Create Item menu item in site explorer.</key>
      <key name="psx.sys_rcSupport.createnew@Create Item">Main header for the Create Item dialog box.</key>
      <key name="psx.sys_rcSupport.createnew@Content Type">Label for Content Type drop down list box.</key>
      <key name="psx.sys_rcSupport.createnew@Template">Label for Template drop down list box.</key>
      <key name="psx.sys_rcSupport.createnew@Create">Label for create button.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
