<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
<xsl:template mode="slotedit_mainbody" match="*">
<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" /> 
<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
<xsl:variable name="componentname" select="componentname"/>
<xsl:variable name="slotlookup" select="/*/slotlookupurl"/>
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
   <tr class="outerboxcell">
      <td class="outerboxcellfont" align="right" valign="top">  
         Add Allowed Content
      </td>
   </tr>
    <xsl:for-each select="category">
      <tr class="headercell">
        <td valign="top">          <!--   View Start   -->
          <table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
            <tr class="headercell">        <!--   Repeats once per category   -->
              <td valign="top" align="left" class="headercellfont" colspan="10">
                <div align="left">Content Type(id):&nbsp;
                  <xsl:apply-templates select="categoryname"/>&nbsp;<xsl:if test="not(categoryid='')">(
                  <xsl:apply-templates select="categoryid"/>)</xsl:if>
                </div>
              </td>
            </tr>
            <tr class="datacell1">
              <td align="center" width="5%" valign="middle" class="headercell2font">&nbsp;</td>
              <td align="center" width="30%" class="headercell2font">Variant&nbsp;Name(id)&nbsp;&nbsp;&nbsp;&nbsp;</td>
              <td align="center" width="20%" class="headercell2font">Style&nbsp;Sheet&nbsp;&nbsp;&nbsp;&nbsp;</td>
              <td align="center" width="38%" class="headercell2font">Assembly&nbsp;URL&nbsp;&nbsp;&nbsp;&nbsp;</td>
              <td align="center" width="10%" class="headercell2font">Produces&nbsp;&nbsp;&nbsp;&nbsp;</td>
            </tr>
            <xsl:for-each select="variant">
					<tr class="datacell1">        <!--   Repeats once per view row   -->
						<xsl:attribute name="class"> 
							<xsl:choose> 
								<xsl:when test="position() mod 2 = 1"> 
									<xsl:value-of select="'datacell1'"/> 
								</xsl:when> 
								<xsl:otherwise> 
									<xsl:value-of select="'datacell2'"/> 
								</xsl:otherwise> 
							</xsl:choose> 
						</xsl:attribute> 
						<xsl:choose>
							<xsl:when test="count(.)=1 and variantid=''">
								<td align="center" colspan="5" class="datacellnoentriesfound">
									No entries found.&nbsp;
								</td>
							</xsl:when>
							<xsl:otherwise>
							  <td align="center" class="datacell1font">
								 <a>
									<xsl:attribute name="href">
									  <xsl:value-of select="addvarianturl"/>
									</xsl:attribute>
									<img height="17" alt="Add" src="../sys_resources/images/new.gif" width="17" border="0"/>
								 </a>
							  </td>
							  <td align="left" class="datacell1font">
								 <xsl:apply-templates select="variantname"/>&nbsp;(
								 <xsl:apply-templates select="variantid"/>)&nbsp;
							  </td>
							  <td align="left" class="datacell1font">
								 <xsl:apply-templates select="ss"/>&nbsp;
							  </td>
							  <td align="left" class="datacell1font">
								 <xsl:apply-templates select="url"/>&nbsp;
							  </td>
							  <td align="left" class="datacell1font">          <!-- begin XSL -->
													  <xsl:choose> <xsl:when test="output='1'" >Page</xsl:when> 
													  <xsl:when test="output='2'" > Snippet</xsl:when> </xsl:choose> 
													  <!-- end XSL -->
								&nbsp;
							  </td>
							</xsl:otherwise>
						</xsl:choose>
					</tr>
				</xsl:for-each>
            <tr class="headercell"><td>&nbsp;</td></tr>
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
</xsl:stylesheet>
