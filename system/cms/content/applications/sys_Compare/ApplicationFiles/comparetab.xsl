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
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:variable name="defactiveitem">
      <xsl:choose>
         <xsl:when test="//activeitem='3'">3</xsl:when>
         <xsl:when test="//activeitem='2'">2</xsl:when>
         <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:template match="/">
      <html>
         <head>
            <link href="../sys_resources/css/templates.css" rel="stylesheet" type="text/css"/>
            <link href="../rx_resources/css/templates.css" rel="stylesheet" type="text/css"/>
            <link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
            <script language="javascript" src="../sys_resources/js/href.js">;</script>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_Compare.compare@Rhythmyx - Document Comparison'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
            <script language="javascript">
               function changeTab(activeitem){
                  var contenttaburl = window.parent.location.href;
                  var h = PSHref2Hash(contenttaburl);
                  h["activeitem"]=activeitem;
                  window.parent.location.href = PSHash2Href(h,contenttaburl);
               }
            </script>
         </head>
         <body topmargin="0" leftmargin="0" marginheight="0" marginwidth="0" class="datacell1">
            <table border="0" cellspacing="0" cellpadding="0" width="100%" height="46">
               <tr>
                  <td height="5">
                     <img src="../sys_resources/images/spacer.gif" alt="" height="4"/>
                  </td>
               </tr>
               <tr>
                  <td align="right" width="100%" height="26">
                     <table border="0" cellspacing="0" cellpadding="0" width="100%" height="26">
                        <tr>
                           <td width="460" class="outerboxcell" align="center" nowrap="yes" colspan="7">
                              <img src="../sys_resources/images/spacer.gif" width="2" height="2" alt=""/>
                           </td>
                           <td>
                              <img src="../sys_resources/images/spacer.gif" alt="" height="2"/>
                           </td>
                        </tr>
                        <tr>
                           <td width="2" class="outerboxcell" align="center" nowrap="yes">
                              <img src="../sys_resources/images/spacer.gif" width="2" height="26" alt=""/>
                           </td>
                           <td width="150" class="outerboxcell" align="center" nowrap="yes">
                              <xsl:attribute name="class"><xsl:choose><xsl:when test="$defactiveitem='1'">outerboxcell</xsl:when><xsl:otherwise>headercell</xsl:otherwise></xsl:choose></xsl:attribute>
                              <font class="outerboxcellfont">
                                 <xsl:attribute name="class"><xsl:choose><xsl:when test="$defactiveitem='1'">outerboxcellfont</xsl:when><xsl:otherwise>headercellfont</xsl:otherwise></xsl:choose></xsl:attribute>
                                 <a href="javascript:void(0)" onclick="javascript:changeTab(1)">
                                    <xsl:call-template name="getLocaleString">
                                       <xsl:with-param name="key" select="'psx.sys_Compare.comparetab@Item'"/>
                                       <xsl:with-param name="lang" select="$lang"/>
                                    </xsl:call-template>
                                    <xsl:text>:</xsl:text>
                                    <xsl:value-of select="//sys_contentid1"/>&nbsp;&nbsp;                     <xsl:call-template name="getLocaleString">
                                       <xsl:with-param name="key" select="'psx.sys_Compare.comparetab@Rev'"/>
                                       <xsl:with-param name="lang" select="$lang"/>
                                    </xsl:call-template>
                                    <xsl:text>:</xsl:text>
                                    <xsl:value-of select="//sys_revision1"/>
                                 </a>
                              </font>
                           </td>
                           <td width="2" class="outerboxcell" align="center" nowrap="yes">
                              <img src="../sys_resources/images/spacer.gif" width="2" height="26" alt=""/>
                           </td>
                           <td width="150" class="outerboxcell" align="center">
                              <xsl:attribute name="class"><xsl:choose><xsl:when test="$defactiveitem='3'">outerboxcell</xsl:when><xsl:otherwise>headercell</xsl:otherwise></xsl:choose></xsl:attribute>
                              <font class="outerboxcellfont">
                                 <xsl:attribute name="class"><xsl:choose><xsl:when test="$defactiveitem='3'">outerboxcellfont</xsl:when><xsl:otherwise>headercellfont</xsl:otherwise></xsl:choose></xsl:attribute>
                                 <xsl:choose>
                                    <xsl:when test="//sys_contentid1='' or //sys_variantid1='' or //sys_revision1='' or //sys_contentid2='' or //sys_variantid2='' or //sys_revision2=''">
                                       <xsl:call-template name="getLocaleString">
                                          <xsl:with-param name="key" select="'psx.sys_Compare.comparetab@Compared'"/>
                                          <xsl:with-param name="lang" select="$lang"/>
                                       </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                       <a href="javascript:void(0)" onclick="javascript:changeTab(3)">
                                          <xsl:call-template name="getLocaleString">
                                             <xsl:with-param name="key" select="'psx.sys_Compare.comparetab@Compared'"/>
                                             <xsl:with-param name="lang" select="$lang"/>
                                          </xsl:call-template>
                                       </a>
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </font>
                           </td>
                           <td width="2" class="outerboxcell" align="center" nowrap="yes">
                              <img src="../sys_resources/images/spacer.gif" width="2" height="26" alt=""/>
                           </td>
                           <td width="150" class="headercell" align="center" nowrap="yes">
                              <xsl:attribute name="class"><xsl:choose><xsl:when test="$defactiveitem='2'">outerboxcell</xsl:when><xsl:otherwise>headercell</xsl:otherwise></xsl:choose></xsl:attribute>
                              <font class="headercellfont">
                                 <xsl:attribute name="class"><xsl:choose><xsl:when test="$defactiveitem='2'">outerboxcellfont</xsl:when><xsl:otherwise>headercellfont</xsl:otherwise></xsl:choose></xsl:attribute>
                                 <xsl:choose>
                                    <xsl:when test="//sys_contentid2='' or //sys_variantid2='' or //sys_revision2=''">
                                       <xsl:call-template name="getLocaleString">
                                          <xsl:with-param name="key" select="'psx.sys_Compare.comparetab@Item'"/>
                                          <xsl:with-param name="lang" select="$lang"/>
                                       </xsl:call-template>
                                       <xsl:text>:</xsl:text>
                                   <xsl:value-of select="//sys_contentid1"/>&nbsp;&nbsp;                     <xsl:call-template name="getLocaleString">
                                          <xsl:with-param name="key" select="'psx.sys_Compare.comparetab@Rev'"/>
                                          <xsl:with-param name="lang" select="$lang"/>
                                       </xsl:call-template>
                                       <xsl:text>:</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                       <a href="javascript:void(0)" onclick="javascript:changeTab(2)">
                                          <xsl:call-template name="getLocaleString">
                                             <xsl:with-param name="key" select="'psx.sys_Compare.comparetab@Item'"/>
                                             <xsl:with-param name="lang" select="$lang"/>
                                          </xsl:call-template>
                                          <xsl:text>:</xsl:text>
                                          <xsl:value-of select="//sys_contentid2"/>&nbsp;&nbsp;                     <xsl:call-template name="getLocaleString">
                                             <xsl:with-param name="key" select="'psx.sys_Compare.comparetab@Rev'"/>
                                             <xsl:with-param name="lang" select="$lang"/>
                                          </xsl:call-template>
                                          <xsl:text>:</xsl:text>
                                          <xsl:value-of select="//sys_revision2"/>
                                       </a>
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </font>
                           </td>
                           <td width="2" class="outerboxcell" align="center" nowrap="yes">
                              <img src="../sys_resources/images/spacer.gif" width="2" height="26" alt=""/>
                           </td>
                           <td>
                              <img src="../sys_resources/images/spacer.gif" alt="" height="13"/>
                           </td>
                        </tr>
                     </table>
                  </td>
               </tr>
               <tr class="outerboxcell">
                  <td align="right" width="100%" height="20">
                     <img src="../sys_resources/images/spacer.gif" alt="" height="20"/>
                  </td>
               </tr>
            </table>
         </body>
      </html>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_Compare.comparetab@Item">Tablabel format will be like Item:301 Rev:10</key>
      <key name="psx.sys_Compare.comparetab@Rev">Tab label format will be like Item:301 Rev:10</key>
      <key name="psx.sys_Compare.comparetab@Compared">Compare tab Label</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
