<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
	<xsl:template mode="editvariantsite_mainbody" match="*">
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcellfont" align="right" valign="top">
         Add Sites to Variant
      </td>
			</tr>
			<tr class="headercell">
				<td>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<tr class="headercell">
							<td width="100%" align="left" valign="middle" class="headercellfont" colspan="2">Variant Name (ID) : <xsl:value-of select="variantname"/>(<xsl:value-of select="variantid"/>)</td>
						</tr>
						<tr class="headercell2">
							<td width="5%" align="center" valign="middle" class="headercell2font">&nbsp;</td>
							<td align="left" width="95%" class="headercell2font">Site (ID)</td>
						</tr>
						<xsl:for-each select="site">
							<tr class="datacell1">
								<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
								<xsl:choose>
									<xsl:when test="not(string-length(siteid)) and position()=1">
										<td class="datacellnoentriesfound" colspan="2" align="center">
                     No entries found.&nbsp;
                  </td>
									</xsl:when>
									<xsl:otherwise>
										<td width="5%" align="center" class="datacell1font">
											<a href="{addurl}">
												<img height="17" alt="Add" src="../sys_resources/images/new.gif" width="17" border="0"/>
											</a>
										</td>
										<td align="left" class="datacell1font">
											<xsl:value-of select="sitename"/>(<xsl:value-of select="siteid"/>)
						</td>
									</xsl:otherwise>
								</xsl:choose>
							</tr>
						</xsl:for-each>
					</table>
				</td>
			</tr>
			<tr class="headercell">
				<td height="100%">&nbsp;</td>
				<!--   Fill down to the bottom   -->
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
