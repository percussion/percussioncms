<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
		<!ENTITY % w3centities-f PUBLIC
				"-//W3C//ENTITIES Combined Set//EN//XML"
				"http://www.w3.org/2003/entities/2007/w3centities-f.ent"
				>
		%w3centities-f;
		]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:template mode="slots_mainbody" match="*">
		<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
		<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
		<xsl:variable name="componentname" select="componentname"/>
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcellfont" align="right" valign="top">
         Slots by Name
      </td>
			</tr>
			<xsl:for-each select="category">
				<tr class="headercell">
					<td>
						<table width="100%" cellpadding="0" cellspacing="1" border="0">
							<tr class="headercell">
								<!--   Repeats once per category   -->
								<td width="100%" valign="top" align="right" colspan="10">
									<a>
										<xsl:attribute name="href"><xsl:value-of select="newsloturl"/></xsl:attribute>
										<b>New Slot</b>
									</a>
               &nbsp;&nbsp;&nbsp;
           </td>
							</tr>
							<tr class="headercell2">
								<td width="5%" align="center" class="headercell2font">&nbsp;</td>
								<td width="50%" align="left" class="headercell2font">Slot&nbsp;Name&nbsp;(ID)&nbsp;&nbsp;&nbsp;</td>
								<td width="45%" align="left" class="headercell2font">Description</td>
							</tr>
							<xsl:for-each select="slot">
								<tr class="datacell1">
									<!--   Repeats once per view row   -->
									<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
									<xsl:choose>
										<xsl:when test="count(.)=1 and ID=''">
											<td align="center" colspan="3" class="datacellnoentriesfound">
								No entries found.&nbsp;
							</td>
										</xsl:when>
										<xsl:otherwise>
											<td align="center" width="5%" class="datacell1font">
												<xsl:choose>
													<xsl:when test="systemslot=1">
														<img height="21" alt="Delete" src="../sys_resources/images/nodelete.gif" width="21" border="0"/>
													</xsl:when>
													<xsl:otherwise>
														<a href="javascript:delConfirm('{deletsloturl}');">
															<img height="21" alt="Delete" src="/sys_resources/images/delete.gif" width="21" border="0"/>
														</a>
													</xsl:otherwise>
												</xsl:choose>
											</td>
											<td align="left" width="50%" class="datacell1font">
												<a>
													<xsl:attribute name="href"><xsl:value-of select="sloturl"/></xsl:attribute>
													<xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="ID"/>)
							 </a>&nbsp;
						  </td>
											<td align="left" width="45%" class="datacell1font">
												<xsl:apply-templates select="desc"/>&nbsp;
						  </td>
										</xsl:otherwise>
									</xsl:choose>
								</tr>
							</xsl:for-each>
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
</xsl:stylesheet>
