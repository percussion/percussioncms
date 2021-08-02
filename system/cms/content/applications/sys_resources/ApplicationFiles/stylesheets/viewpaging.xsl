<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >

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
   <a><xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>Prev</a>&nbsp;
</xsl:template>
<xsl:template match="PSXIndexPage" mode="paging">
   <xsl:choose>
      <xsl:when test="not(/*/currentpsfirst='')">
            <xsl:choose>
               <xsl:when test="substring-after(.,'psfirst=')=/*/currentpsfirst">
                  <font class="currentPageNumber"><xsl:value-of select="@pagenum"/></font>&nbsp;
               </xsl:when>
               <xsl:otherwise>
                  <a><xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute><xsl:value-of select="@pagenum"/></a>&nbsp;
               </xsl:otherwise>
            </xsl:choose>
     </xsl:when>
     <xsl:otherwise>
            <xsl:choose>
               <xsl:when test="position()=1">
                  <font class="currentPageNumber">1</font>&nbsp;
               </xsl:when>
               <xsl:otherwise>
                  <a><xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute><xsl:value-of select="@pagenum"/></a>&nbsp;
               </xsl:otherwise>
            </xsl:choose>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>
<xsl:template match="PSXNextPage" mode="paging">
   <a><xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>Next</a>&nbsp;
</xsl:template>

</xsl:stylesheet>
