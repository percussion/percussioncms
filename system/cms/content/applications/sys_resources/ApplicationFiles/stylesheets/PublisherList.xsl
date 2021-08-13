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

<xsl:template match="configparamlookup" mode="publisherlist">
<xsl:if test="string-length(item/publisherid)!=0">
<table border="0" cellspacing="1" cellpadding="0" height="100%" width="100%" class="headercell">
		<tr class="datacell1">
			<td height="21" width="5%" align="left" class="headercell2font">&nbsp;</td>
			<td height="21" width="20%" align="left" class="headercell2font">Name&nbsp;&nbsp;&nbsp;</td>
			<td height="21" width="35%" align="left" class="headercell2font">Value&nbsp;&nbsp;&nbsp;</td>
			<td height="21" width="40%" align="left" class="headercell2font">Description&nbsp;&nbsp;&nbsp;</td>
		</tr>
		<xsl:for-each select="item">
			<tr class="datacell1">
        <td class="datacell1" height="20" align="center">
          <xsl:choose>
            <xsl:when test="type = '0'">
                <img src="../sys_resources/images/nodelete.gif" width="21" height="21" border="0" alt="Non-deletable" />   						
            </xsl:when>
            <xsl:otherwise>
					<a href="javascript:delConfirm('{delete}');">
                <img src="../sys_resources/images/delete.gif" width="21" height="21" border="0" alt="Delete" />
               </a>        			
            </xsl:otherwise>
          </xsl:choose>
				</td>
				<td class="datacell1" height="20" align="left"><span class="datacell1font">
					<a>
						<xsl:attribute name="href">
							<xsl:value-of select="editurl" />
						</xsl:attribute>
						<xsl:value-of select="name" />&#160;
					</a>
        			</span></td>
				<td class="datacell1" height="20" align="left"><span class="datacell1font">
					<xsl:value-of select="value" />&#160;
        			</span></td>
				<td class="datacell1" height="20" align="left"><span class="datacell1font">
					<xsl:value-of select="desc" />&#160;
        			</span></td>
			</tr>
		</xsl:for-each>
	</table>
</xsl:if>
    </xsl:template>

</xsl:stylesheet>
