<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
<xsl:template mode="contenttype_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
   <tr class="outerboxcell">
      <td class="outerboxcellfont" align="right" valign="top">
         Copy Variant
      </td>
   </tr>
   <xsl:for-each select="category">
   <tr class="headercell">
     <td>
       <table width="100%" cellpadding="0" cellspacing="1" border="0">
         <tr class="headercell">
           <td valign="top" align="left" class="headercellfont">
             <xsl:apply-templates select="categoryname"/>
           </td>
         </tr>
         <tr class="headercell2">
           <td width="20%" align="left" class="headercell2font">Variant Name (ID)</td>
           <td width="20%" align="left" class="headercell2font">Style Sheet</td>
           <td width="50" align="left" class="headercell2font">Assembly URL</td>
           <td width="10%" align="left" class="headercell2font">Produces</td>
         </tr>
         <xsl:apply-templates select="variant" mode="mode6"/>
         <tr>
            <td align="center" colspan="10">
               <xsl:apply-templates select="/" mode="paging"/>
            </td>
         </tr>
       </table>
     </td>
   </tr>
 </xsl:for-each>
 <tr class="headercell">
   <td height="100%">&nbsp;</td>
   <!--   Fill down to the bottom   -->
 </tr>
</table>
</xsl:template>
  <xsl:template match="variant" mode="mode6">
    <xsl:for-each select=".">
      <tr class="datacell1">        <!--   Repeats once per view row   -->

        <td align="left" class="datacell1font">
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="copyvarianturl"/>
            </xsl:attribute>

            <xsl:apply-templates select="name"/>&nbsp;(
            <xsl:apply-templates select="ID"/>)
          </a>&nbsp;
        </td>

        <td align="left" class="datacell1font">
          <xsl:apply-templates select="ss"/>&nbsp;
        </td>

        <td align="left" class="datacell1font">
          <xsl:apply-templates select="url"/>&nbsp;
        </td>

        <td align="left" class="datacell1font">          <!-- begin XSL -->
                                      <xsl:choose> <xsl:when test="./output='1'">Page</xsl:when> 
                                      <xsl:when test="./output='2'">Snippet</xsl:when> 
                                      </xsl:choose> 
                                      <!-- end XSL -->
&nbsp;</td>

      </tr>

    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
