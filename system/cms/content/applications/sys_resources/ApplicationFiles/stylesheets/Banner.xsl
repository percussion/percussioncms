<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" > 
<xsl:variable name="sysimgpath" select="'../sys_resources/images/'"/>
<xsl:template name="Banner"> 
   <xsl:param name="module" select="''" /> 
   <xsl:param name="userroles" select="'test'" /> 
   <table width="100%" height="125" cellpadding="0" cellspacing="0" border="0">
      <tr> 
         <td height="94"> 
            <table width="100%" height="94" cellpadding="0" cellspacing="0" border="0">
               <tr class="bannerbackground"> 
                  <td width="315" valign="top" align="left"><img src="{concat($sysimgpath, 'rx_banner.gif')}" width="315" height="93" border="0" alt="Rhythmyx Content Manager" /></td>
                  <td width="100%" height="94"><img src="{concat($sysimgpath, 'invis.gif')}" width="1" height="94" border="0" alt="" /></td>
                  <td width="178" align="right" valign="bottom"><img src="{concat($sysimgpath, 'rx_banner_right.gif')}" width="178" height="21" border="0" alt="Percussion Software" /></td>
               </tr>
            </table>
         </td>
      </tr>
      <tr>
         <td height="1" class="backgroundcolor"><img src="{concat($sysimgpath, 'invis.gif')}" width="1" height="1" border="0"  alt="" /></td>
      </tr>
      <tr class="outerboxcell"> 
         <td height="30"> 
            <table width="100%" height="30" cellpadding="0" cellspacing="0" border="0">
               <tr> 
                  <xsl:call-template name="bannerlinkcell">
                     <xsl:with-param name="href" select="'/Rhythmyx/sys_ca/camain.html?sys_componentname=ca_inbox&amp;sys_pagename=ca_inbox&amp;sys_sortparam=title'"/>
                     <xsl:with-param name="modulename" select="'content'"/>
                     <xsl:with-param name="module" select="$module"/>
                  </xsl:call-template>
                  <xsl:choose>
                   <xsl:when test="$userroles='Author'">
                     <xsl:call-template name="bannerlinkcell"/>
                     <xsl:call-template name="bannerlinkcell"/>
                     <xsl:call-template name="bannerlinkcell"/>
                   </xsl:when>
                   <xsl:otherwise>
                     <xsl:call-template name="bannerlinkcell">
                        <xsl:with-param name="href" select="'/Rhythmyx/sys_wfEditor/welcome.html?sys_componentname=wf_all&amp;sys_pagename=wf_all'"/>
                        <xsl:with-param name="modulename" select="'workflow'"/>
                        <xsl:with-param name="module" select="$module"/>
                     </xsl:call-template>
                     <xsl:call-template name="bannerlinkcell">
                        <xsl:with-param name="href" select="'/Rhythmyx/sys_Variants/sa_variants.html?sys_componentname=sys_variants&amp;sys_pagename=sys_variants'"/>
                        <xsl:with-param name="modulename" select="'system'"/>
                        <xsl:with-param name="module" select="$module"/>
                     </xsl:call-template>
                   </xsl:otherwise>
                  </xsl:choose>
               </tr>
            </table>
         </td>
      </tr>
   </table>
</xsl:template> 

<xsl:template name="bannerlinkcell"> 
   <xsl:param name="module"/>
   <xsl:param name="href"/>
   <xsl:param name="modulename" />
   <td width="25%" align="center">
      <xsl:choose>
         <xsl:when test="$href=''">&nbsp;</xsl:when>
         <xsl:otherwise>
            <a>
               <xsl:attribute name="href"><xsl:value-of select="$href"/></xsl:attribute>
               <xsl:variable name="tmp">
                  <xsl:if test="$module=$modulename">
                     <xsl:text>_sel.gif</xsl:text>
                  </xsl:if>
                  <xsl:if test="$module!=$modulename">
                     <xsl:text>.gif</xsl:text>
                  </xsl:if>
               </xsl:variable>
               <img width="87" height="30" border="0">
                  <xsl:attribute name="alt"><xsl:value-of select="$modulename"/></xsl:attribute>
                  <xsl:attribute name="src"><xsl:value-of select="concat($sysimgpath, $modulename, $tmp)"/></xsl:attribute>
               </img>
            </a>
         </xsl:otherwise>
      </xsl:choose>
   </td>
</xsl:template> 
</xsl:stylesheet> 
