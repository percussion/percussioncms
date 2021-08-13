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
   <xsl:variable name="viewflagelem" select="document('../rxconfig/Server/config.xml')/*/BrowserUISettings/ContentActions/@uiType"/>
   <xsl:variable name="viewflag">
      <xsl:choose>
         <xsl:when test="$viewflagelem">
            <xsl:value-of select="$viewflagelem"/>
         </xsl:when>
         <xsl:otherwise>actionMenu</xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:output method="xml"/>
   <xsl:template match="/">
      <xsl:variable name="userroles" select="document(*/userrolesurl)/*/UserStatus"/>
      <xsl:variable name="componentcontext" select="document(*/contexturl)/*/context"/>
      <html>
         <head>
            <meta name="generator" content="Percussion XSpLit Version 3.5"/>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_cmpCaMenuNewContent.newcontentmenu@New Content Menu'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
         </head>
         <body>
            <table width="100%" cellpadding="1" cellspacing="0" border="0">
               <tr class="datacell1">
                  <td align="left" class="outerboxcellfont">
                     <xsl:call-template name="getLocaleString">
                        <xsl:with-param name="key" select="'psx.sys_cmpCaMenuNewContent.newcontentmenu@New Content'"/>
                        <xsl:with-param name="lang" select="$lang"/>
                     </xsl:call-template>
                  </td>
               </tr>
               <xsl:apply-templates select="*" mode="mode0"/>
            </table>
            <span>&nbsp;</span>
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
      <xsl:for-each select="item">
         <tr>
            <!-- begin XSL -->
            <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
            <!-- end XSL -->
            <td align="left">
               <!-- begin XSL -->
               <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1font'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2font'"/></xsl:otherwise></xsl:choose></xsl:attribute>
               <!-- end XSL -->
               <xsl:value-of select="'&nbsp;&nbsp;&nbsp;'"/>
               <xsl:choose>
                  <xsl:when test="$viewflag='ceLink' or $viewflag='both'">
                     <a>
                        <xsl:attribute name="href"><xsl:value-of select="url"/></xsl:attribute>
                        <xsl:value-of select="displaytext"/>
                     </a>
                  </xsl:when>
                  <xsl:otherwise>
                     <a href="javascript:void(0);">
                        <xsl:attribute name="onclick"><xsl:text>javascript:PSEditContent("</xsl:text><xsl:value-of select="url"/><xsl:text>", "","")</xsl:text></xsl:attribute>
                        <xsl:value-of select="displaytext"/>
                     </a>
                  </xsl:otherwise>
               </xsl:choose>
            </td>
         </tr>
      </xsl:for-each>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cmpCaMenuNewContent.newcontentmenu@New Content Menu">Page title for new content menu component.</key>
      <key name="psx.sys_cmpCaMenuNewContent.newcontentmenu@New Content">Left navigation bar new content menu items category name.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
