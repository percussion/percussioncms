<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
		]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:template mode="component_mainbody" match="*">
		<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus" />
		<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
		<xsl:variable name="componentname" select="componentname"/>
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcellfont" align="left" valign="top">
					Sort By:<a href="{//components/@compbynameurl}">Name</a><a href="{//components/@compbyidurl}">ID</a>
				</td>
				<td class="outerboxcellfont" align="right" valign="top">
					Components
				</td>
			</tr>
			<tr class="headercell">
				<td colspan="2">
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<tr class="headercell">        <!--   Repeats once per category   -->
							<td valign="top" align="right" colspan="10">
								<a>
									<xsl:attribute name="href">
										<xsl:value-of select="newcomponenturl"/>
									</xsl:attribute>
									<b>New Component </b>
								</a>
							</td>
						</tr>
						<tr class="headercell2">
							<td width="5%" align="center" class="headercell2font"></td>
							<td width="30%" align="left" class="headercell2font">Name(ID)</td>
							<td width="30%" align="left" class="headercell2font">DisplayName</td>
							<td width="35%" align="left" class="headercell2font">Description</td>
						</tr>
						<xsl:for-each select="list">
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
									<xsl:when test="count(.)=1 and componentid=''">
										<td align="center" colspan="5" class="datacellnoentriesfound">
											No entries found.
										</td>
									</xsl:when>
									<xsl:otherwise>
										<td align="center" class="datacell1font">
											<a href="javascript:delConfirm('{deletecomponenturl}');">
												<img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
											</a>
										</td>
										<td align="left" class="datacell1font">
											<a>
												<xsl:attribute name="href">
													<xsl:value-of select="editcomponenturl"/>
												</xsl:attribute>
												<xsl:apply-templates select="componentname"/>(<xsl:apply-templates select="componentid"/>)
											</a>
										</td>
										<td align="left" class="datacell1font">
											<xsl:apply-templates select="componentdisplayname"/>
										</td>
										<td align="left" class="datacell1font">
											<xsl:apply-templates select="componentdesc"/>
										</td>
									</xsl:otherwise>
								</xsl:choose>
							</tr>
						</xsl:for-each>
						<tr>
							<td align="center" colspan="5">
								<xsl:apply-templates select="/" mode="paging"/>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr class="headercell">
				<td height="100%" colspan="2"></td>
				<!--   Fill down to the bottom   -->
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="component">
	</xsl:template>
</xsl:stylesheet>
