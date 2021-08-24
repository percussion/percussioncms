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
    <xsl:output method="html" omit-xml-declaration="yes"/>
  <xsl:template match="*" mode="statelist">
		<tr>
		  <td colspan="10" class="headercellfont" align="right">
			 <a href="{newlink}">New State</a>
		  </td>
		</tr>
      <tr class="headercell2">
        <td height="21" width="5%" align="center" class="headercell2font">&nbsp;</td>
        <td height="21" width="35%" align="left" class="headercell2font">Name (ID)</td>
        <td height="21" width="45%" align="left" class="headercell2font">Description</td>
        <td height="21" width="15%" align="left" class="headercell2font">Sort Order</td>
      </tr>
    <xsl:for-each select="state">
     <xsl:if test="stateid != ''">
      <tr class="datacell1">
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
        <td class="datacell1font" height="21" align="center">
            <a>
              <xsl:attribute name="href">javascript: delConfirm('<xsl:value-of select="deletelink"/>' );</xsl:attribute>
              <img src="/sys_resources/images/delete.gif" width="21" height="21" border="0"/>
            </a>
        </td>
        <td class="datacell1font" height="21" align="left">
             <a>
              <xsl:attribute name="href">
                <xsl:value-of select="link"/>
              </xsl:attribute>
					<xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="stateid"/>)&nbsp;
            </a>
        </td>
        <td class="datacell1font" height="21">
				<xsl:apply-templates select="description"/>&nbsp;
        </td>
        <td class="datacell1font" height="21">
				<xsl:apply-templates select="sortorder"/>&nbsp;
        </td>
      </tr>
     </xsl:if>
	  <xsl:if test="count(.)=1 and stateid = ''">
		<tr class="datacell1">
			<td align="center" colspan="4" class="datacellnoentriesfound">
				No entries found.&nbsp;
			</td>
      </tr>
	  </xsl:if>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
