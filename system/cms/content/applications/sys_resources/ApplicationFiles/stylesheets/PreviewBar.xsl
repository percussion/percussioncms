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
   <xsl:template match="VariantList" mode="previewbar">
      <xsl:comment>Start of Preview Bar</xsl:comment>
      <table width="100%" cellpadding="0" cellspacing="0" border="0">
         <tr class="outerboxcell">
            <td align="center" class="outerboxcellfont">
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.contenteditor.previewbar@Preview'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </td>
         </tr>
         <tr>
            <td width="100%" height="1" class="backgroundcolor">
               <img src="../sys_resources/images/invis.gif" width="1" height="1" border="0" alt=""/>
            </td>
         </tr>
         <tr>
            <td class="headercell2">
               <table width="100%" border="0" cellspacing="0" cellpadding="4">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0" class="headercell2">
                           <xsl:for-each select="Variant[position() mod 3 = 1]">
                              <tr class="headercell2">
                                 <xsl:apply-templates select=".|following-sibling::Variant[position() &lt; 3]" mode="columns"/>
                              </tr>
                           </xsl:for-each>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
      <xsl:comment>End of Preview Bar</xsl:comment>
   </xsl:template>
   <xsl:template match="Variant" mode="columns">
      <td align="left" class="headercell2">&#160;&#160;
         <img src="../sys_resources/images/preview.gif" width="20" height="20" border="0" alt="Click on link for preview">
            <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.contenteditor.previewbar.alt@Click on link for preview'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
         </img>
         &#160;
         <a class="previewlinks">
            <xsl:attribute name="target"><xsl:value-of select="'_blank'"/></xsl:attribute>
            <xsl:variable name="url" select="AssemblyUrl"/>
            <xsl:choose>
               <xsl:when test="contains($url, '?')">
                  <xsl:attribute name="href"><xsl:value-of select="$url"/>&amp;sys_contentid=<xsl:value-of select="/ContentEditor/Workflow/@contentId"/>&amp;sys_variantid=<xsl:value-of select="@variantId"/>&amp;sys_revision=<xsl:value-of select="/ContentEditor/Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>&amp;sys_context=0&amp;sys_authtype=0&amp;pssessionid=<xsl:value-of select="/ContentEditor/UserStatus/@sessionId"/></xsl:attribute>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:attribute name="href"><xsl:value-of select="$url"/>?sys_contentid=<xsl:value-of select="/ContentEditor/Workflow/@contentId"/>&amp;sys_variantid=<xsl:value-of select="@variantId"/>&amp;sys_revision=<xsl:value-of select="/ContentEditor/Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_revision']"/>&amp;sys_context=0&amp;sys_authtype=0&amp;pssessionid=<xsl:value-of select="/ContentEditor/UserStatus/@sessionId"/></xsl:attribute>
               </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="DisplayName"/>
         </a>
      </td>
   </xsl:template>
   <xsl:template match="VariantList" mode="previewbar-edit">
      <xsl:comment>Start of Preview Bar</xsl:comment>
      <xsl:param name="contentid"/>
      <xsl:param name="revision"/>
      <table width="100%" cellpadding="0" cellspacing="0" border="0">
         <tr>
            <td  valign="top">
               <table width="100%" border="0" cellspacing="0" cellpadding="0">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <xsl:for-each select="Variant[position() mod 3 = 1]">
                              <tr>
                                 <xsl:apply-templates select=".|following-sibling::Variant[position() &lt; 3]" mode="columns-edit">
                                    <xsl:with-param name="contentid" select="$contentid"/>
                                    <xsl:with-param name="revision" select="$revision"/>
                                 </xsl:apply-templates>
                              </tr>
                           </xsl:for-each>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
      <xsl:comment>End of Preview Bar</xsl:comment>
   </xsl:template>
   <xsl:template match="Variant" mode="columns-edit">
   <xsl:param name="contentid" />
   <xsl:param name="revision" />
   <xsl:variable name="url" select="AssemblyUrl"/>
      <td align="left">
         <a class="previewlinks">
            <xsl:attribute name="target"><xsl:value-of select="'_blank'"/></xsl:attribute>
            <xsl:attribute name="href">
               <xsl:choose>
                  <xsl:when test="contains($url, '?')">            
	             <xsl:value-of select="$url"/>&amp;sys_contentid=<xsl:value-of select="$contentid"/>&amp;sys_variantid=<xsl:value-of select="@variantId"/>&amp;sys_revision=<xsl:value-of select="$revision"/>&amp;sys_context=0&amp;sys_authtype=0<xsl:text>&amp;sys_command=editrc&amp;parentPage=yes</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="$url"/>?sys_contentid=<xsl:value-of select="$contentid"/>&amp;sys_variantid=<xsl:value-of select="@variantId"/>&amp;sys_revision=<xsl:value-of select="$revision"/>&amp;sys_context=0&amp;sys_authtype=0<xsl:text>&amp;sys_command=editrc&amp;parentPage=yes</xsl:text>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:attribute>
            <xsl:value-of select="DisplayName"/>
         </a>
      </td>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.contenteditor.previewbar@Preview">Label for Preview bar in Full Content Editor.</key>
      <key name="psx.contenteditor.previewbar.alt@Click on link for preview">Alt text for preview image in Full COntent Editor.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
