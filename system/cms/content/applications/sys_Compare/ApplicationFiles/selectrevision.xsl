<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:output method="xml"/>
   <xsl:variable name="contentstatus" select="document(//contentstatusurl)"/>
   <xsl:template match="/">
      <html>
         <head>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_Compare.selectrevision@Rhythmyx - Select Revision for Document Comparison'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <link href="../sys_resources/css/templates.css" rel="stylesheet" type="text/css"/>
            <link href="../rx_resources/css/templates.css" rel="stylesheet" type="text/css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
            <script language="javascript" src="../sys_resources/js/href.js">;</script>
            <script language="javascript">
               function selectRevision(revid){
                  var inumber = document.selectrevision.itemnumber.value;
                  parenturl = window.opener.location.href;
                  var rightframeurl = window.opener.parent.rightframe.location.href;
                  var h = PSHref2Hash(parenturl);
                  var h1 = PSHref2Hash(rightframeurl);
                  h["sys_revision" + inumber ] = revid;
                  if(inumber == 2){
                     if(h["sys_contentid2"] == ''){
                        h["sys_contentid2"]= h["sys_contentid1"];
                     }
                     if(h1["sys_contentid2"] == ''){
                        h1["sys_contentid2"]= h1["sys_contentid1"];
                     }
                  }
                  h["sys_variantid1"] = window.opener.document.itemdetails1.variantlist[window.opener.document.itemdetails1.variantlist.selectedIndex].value;
                  if(window.opener.document.itemdetails2.variantlist!=null)
                  {
                     h["sys_variantid2"] = window.opener.document.itemdetails2.variantlist   [window.opener.document.itemdetails2.variantlist.selectedIndex].value;
                  }
                  h1["sys_revision" + inumber] = revid;
                  h1["activeitem"] = inumber;
                  window.opener.location.href = PSHash2Href(h,parenturl);
                  window.opener.parent.rightframe.location.href = PSHash2Href(h1,rightframeurl);
                  self.close();
               }
            </script>
         </head>
         <body leftmargin="0" topmargin="0">
            <xsl:apply-templates select="selectrevison"/>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="selectrevison">
      <form name="selectrevision">
         <input type="hidden" name="itemnumber" value="{itemnumber}"/>
         <table width="100%" align="center" border="0" cellpadding="0" cellspacing="1" class="outerboxcell">
            <tr class="outerboxcell">
               <td align="center" class="outerboxcellfont" height="20" width="100%" colspan="6">
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.sys_Compare.selectrevision@Select a revision for comparison'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>
               </td>
            </tr>
            <tr class="headercell">
               <td align="left" class="headercellfont" height="20" width="5%">&nbsp;</td>
               <td align="left" class="headercellfont" width="10%">
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.sys_Compare.selectrevision@Rev'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>
               </td>
               <td align="left" class="headercellfont" width="30%">
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.sys_Compare.selectrevision@Date'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>
               </td>
               <td align="left" class="headercellfont" width="30%">
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.sys_Compare.selectrevision@Who'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>
               </td>
               <td align="left" class="headercellfont" width="20%">
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.sys_Compare.selectrevision@State'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>
               </td>
               <td align="left" class="headercellfont" width="5%">
                  <xsl:call-template name="getLocaleString">
                     <xsl:with-param name="key" select="'psx.sys_Compare.selectrevision@Comment'"/>
                     <xsl:with-param name="lang" select="$lang"/>
                  </xsl:call-template>
               </td>
            </tr>
            <xsl:for-each select="items/item">
               <xsl:if test="not(revid=following-sibling::item/revid) and revid!=''">
                  <tr class="datacell2">
                     <td align="center" class="datacell1font">
                        <a href="javascript:void(0);">
                           <xsl:attribute name="onclick">javascript:selectRevision(<xsl:value-of select="revid"/>)</xsl:attribute>
                           <img src="../sys_resources/images/insert.gif" border="0" alt="{comment}"/>
                        </a>
                     </td>
                     <td align="left" class="datacell1font">
                        <xsl:value-of select="revid"/>&nbsp;
                     </td>
                     <td align="left" class="datacell1font">
                        <xsl:value-of select="date"/>&nbsp;
                     </td>
                     <td align="left" class="datacell1font">
                        <xsl:value-of select="actor"/>&nbsp;
                     </td>
                     <td align="left" class="datacell1font">
                     <xsl:call-template name="getLocaleString">
                        <xsl:with-param name="key" select="concat('psx.workflow.state@',state)"/>
                        <xsl:with-param name="lang" select="$lang"/>
                     </xsl:call-template>&nbsp;
                     </td>
                     <td align="center" class="datacell1font">
                        <xsl:if test="comment!=''">
                           <img alt="{comment}" src="../sys_resources/images/singlecomment.gif" width="16" height="16" border="0">
                              <xsl:attribute name="OnClick">newWindow=window.open('','HistoryComment','width=500,height=      100,resizable=yes');newWindow.document.write("<xsl:value-of select="comment"/>");setTimeout('newWindow.close()',5000);      </xsl:attribute>
                           </img>
                        </xsl:if>&nbsp;
      				</td>
                  </tr>
               </xsl:if>
            </xsl:for-each>
            <xsl:if test="$contentstatus//items/item/checkoutuser=$contentstatus//items/item/loginuser and not($contentstatus//items/item/sys_revision=items/item/revid)">
               <xsl:apply-templates select="$contentstatus//items/item" mode="item"/>
            </xsl:if>
            <tr class="datacell2">
               <td align="center" colspan="6" class="datacell1font" height="20">
                  <input type="button" name="close" value="Close" onclick="javascript:window.close();">
                     <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                  </input>
               </td>
            </tr>
         </table>
      </form>
   </xsl:template>
   <xsl:template match="item" mode="item">
      <tr class="datacell2">
         <td align="center" class="datacell1font">
            <a href="javascript:void(0);">
               <xsl:attribute name="onclick">javascript:selectRevision(<xsl:value-of select="sys_revision"/>)</xsl:attribute>
               <img src="../sys_resources/images/insert.gif" border="0" alt="Select Revision for Comparison">
                  <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_Compare.selectrevision.alt@Select Revision for Comparison'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
               </img>
            </a>
         </td>
         <td align="left" class="datacell1font">
            <xsl:value-of select="sys_revision"/>
         </td>
         <td align="left" class="datacell1font">
            <xsl:value-of select="date"/>
         </td>
         <td align="left" class="datacell1font">
            <xsl:value-of select="actor"/>
         </td>
         <td align="left" class="datacell1font">
            <xsl:value-of select="state"/>
         </td>
         <td align="center" class="datacell1font">
            &nbsp;
         </td>
      </tr>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_Compare.selectrevision@Rhythmyx - Select Revision for Document Comparison">Title for select revision popup window</key>
      <key name="psx.sys_Compare.selectrevision@Select a revision for comparison">Header for select revision window</key>
      <key name="psx.sys_Compare.selectrevision.alt@Select Revision for Comparison">Alt text for the image for selecting revision.</key>
      <key name="psx.sys_Compare.selectrevision@Title">Column header for title.</key>
      <key name="psx.sys_Compare.selectrevision@Rev">Column header for revision id.</key>
      <key name="psx.sys_Compare.selectrevision@Date">Column header for revision date.</key>
      <key name="psx.sys_Compare.selectrevision@Who">Column header for showing who acted on the document.</key>
      <key name="psx.sys_Compare.selectrevision@State">Column header for the workflow state of the document.</key>
      <key name="psx.sys_Compare.selectrevision@Comment">Column header for the user comment.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
