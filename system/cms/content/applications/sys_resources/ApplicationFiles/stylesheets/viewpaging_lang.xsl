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
   <xsl:template match="*" mode="paging">
      <table>
         <tr>
            <td>
               <xsl:apply-templates select="PSXPrevPage" mode="paging"/>
               <xsl:apply-templates select="PSXIndexPage" mode="paging"/>
               <xsl:apply-templates select="PSXNextPage" mode="paging"/>
            </td>
         </tr>
      </table>
   </xsl:template>
   <xsl:template match="PSXPrevPage" mode="paging">
      <a>
         <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
         <xsl:call-template name="getLocaleString">
            <xsl:with-param name="key" select="'psx.sys_resources.viewpaging_lang@Prev'"/>
            <xsl:with-param name="lang" select="$lang"/>
         </xsl:call-template>
      </a>&nbsp;
</xsl:template>
   <xsl:template match="PSXIndexPage" mode="paging">
      <xsl:choose>
         <xsl:when test="not(/*/currentpsfirst='')">
            <xsl:choose>
               <xsl:when test="substring-after(.,'psfirst=')=/*/currentpsfirst">
                  <font class="currentPageNumber">
                     <xsl:value-of select="@pagenum"/>
                  </font>&nbsp;
               </xsl:when>
               <xsl:otherwise>
                  <a>
                     <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
                     <xsl:value-of select="@pagenum"/>
                  </a>&nbsp;
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise>
            <xsl:choose>
               <xsl:when test="position()=1">
                  <font class="currentPageNumber">1</font>&nbsp;
               </xsl:when>
               <xsl:otherwise>
                  <a>
                     <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
                     <xsl:value-of select="@pagenum"/>
                  </a>&nbsp;
               </xsl:otherwise>
            </xsl:choose>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="PSXNextPage" mode="paging">
      <a>
         <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
         <xsl:call-template name="getLocaleString">
            <xsl:with-param name="key" select="'psx.sys_resources.viewpaging_lang@Next'"/>
            <xsl:with-param name="lang" select="$lang"/>
         </xsl:call-template>
      </a>&nbsp;
</xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_resources.viewpaging_lang@Prev">Previous page link text in paging.</key>
      <key name="psx.sys_resources.viewpaging_lang@Next">Next page link text in paging.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
