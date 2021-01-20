<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
   %HTMLlat1;
   <!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
   %HTMLsymbol;
   <!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
   %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
   <xsl:template mode="sys_mainbody" match="*">
      <xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
      <xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
      <xsl:variable name="pagename" select="pagename"/>
      <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
         <tr class="outerboxcell">
            <td class="outerboxcellfont" align="right" valign="top">
         Variants
      </td>
         </tr>
         <xsl:for-each select="category">
         <xsl:if test="not(categoryname='Folder')">
            <tr class="headercell">
               <td>
                  <table width="100%" cellpadding="0" cellspacing="1" border="0">
                     <tr class="headercell">
                        <!--   Repeats once per category   -->
                        <td valign="top" align="left" class="headercellfont" colspan="4">
                           <xsl:apply-templates select="categoryname"/>
                        </td>
                     </tr>
                     <tr class="headercell">
                        <!--   Repeats once per category   -->
                        <td valign="top" align="right" colspan="4">
                           <a>
                              <xsl:attribute name="href"><xsl:value-of select="addvarianturl"/></xsl:attribute>
                              <b>New Variant</b>
                           </a>
               &nbsp;&nbsp;&nbsp;
               <a>
                              <xsl:attribute name="href"><xsl:value-of select="copyvarianturl"/></xsl:attribute>
                              <b>Copy Variant</b>
                           </a>
               &nbsp;&nbsp;&nbsp;
            </td>
                     </tr>
                     <tr class="headercell2">
                        <td width="5%" align="center" class="headercell2font">&nbsp;</td>
                        <td width="30%" align="left" class="headercell2font">Variant&nbsp;Name&nbsp;(ID)&nbsp;</td>
                        <td width="45%" align="left" class="headercell2font">Description&nbsp;</td>
                        <td width="20%" align="left" class="headercell2font">Produces&nbsp;</td>
                     </tr>
                     <xsl:apply-templates select="variant" mode="variantdisplay"/>
                  </table>
               </td>
            </tr>
            </xsl:if>
         </xsl:for-each>
         <tr>
            <td align="center">
               <xsl:apply-templates select="/" mode="paging"/>
            </td>
         </tr>
         <tr class="headercell">
            <td height="100%">&nbsp;</td>
            <!--   Fill down to the bottom   -->
         </tr>
      </table>
   </xsl:template>
   <xsl:template match="variant" mode="variantdisplay">
      <xsl:if test="not(ID='')">
         <tr class="datacell1">
            <!--   Repeats once per view row   -->
            <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
            <td align="center" class="datacell1font" width="5%">
               <a href="javascript:delConfirm('{deletvarianturl}');">
                  <img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
               </a>
            </td>
            <td align="left" class="datacell1font" width="30%">
               <a>
                  <xsl:attribute name="href"><xsl:value-of select="editvarianturl"/></xsl:attribute>
                  <xsl:apply-templates select="name"/>&nbsp;(
         <xsl:apply-templates select="ID"/>)
         </a>&nbsp;
         </td>
            <td align="left" class="datacell1font" width="45%">
               <xsl:apply-templates select="description"/>&nbsp;
         </td>
            <td align="left" class="datacell1font" width="20%">
               <!-- begin XSL -->
               <xsl:choose>
                  <xsl:when test="./output='1'">Page</xsl:when>
                  <xsl:when test="./output='2'">Snippet</xsl:when>
               </xsl:choose>
               <!-- end XSL -->
         &nbsp;</td>
         </tr>
      </xsl:if>
      <xsl:if test="ID='' and position()=1">
         <tr class="datacell1">
            <!--   Repeats once per view row   -->
            <td align="center" colspan="4" class="datacellnoentriesfound">
         No entries found.&nbsp;</td>
         </tr>
      </xsl:if>
   </xsl:template>
</xsl:stylesheet>
