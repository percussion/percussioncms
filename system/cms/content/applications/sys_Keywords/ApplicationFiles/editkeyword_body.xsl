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
	<xsl:template mode="editkeywords_mainbody" match="*">
		<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
		<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
		<xsl:variable name="componentname" select="componentname"/>
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcell" align="right" valign="top" colspan="2">
					<span class="outerboxcellfont">Edit Keyword</span>
				</td>
			</tr>
			<tr class="headercell">
				<td>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<form name="updatekeyword" method="post">
							<xsl:attribute name="action"><xsl:choose><xsl:when test="categoryid=''">insertkeyword.html</xsl:when><xsl:otherwise>updatekeyword.html</xsl:otherwise></xsl:choose></xsl:attribute>
							<input name="sys_componentname" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
							</input>
							<input name="sys_pagename" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="pagename"/></xsl:attribute>
							</input>
							<input name="DBActionType" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="dbactiontype"/></xsl:attribute>
							</input>
							<input name="categoryid" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="categoryid"/></xsl:attribute>
							</input>
							<input name="doccancelurl" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="cancelurl"/></xsl:attribute>
							</input>
							<input name="lookuptype" type="hidden" value="1"/>
							<input name="lookupvalue" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="lookupvalue"/></xsl:attribute>
							</input>
							<input name="lookupsequence" type="hidden" value="1"/>
							<xsl:if test="string(categoryid)">
								<tr class="headercell2">
									<td width="30%" align="left" class="datacell1font">Keyword&nbsp;ID&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
									<td width="90%" align="left" class="headercell2font">
										<xsl:apply-templates select="categoryid"/>
									</td>
								</tr>
							</xsl:if>
							<tr class="datacell1">
								<td width="30%" align="left" class="datacell1font">
									<font class="reqfieldfont">*</font>Keyword Name</td>
								<td width="70%" align="left" class="datacell1font">
									<input name="lookupname">
										<xsl:attribute name="value"><xsl:value-of select="lookupname"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell1">
								<td width="30%" align="left" class="datacell1font">Description</td>
								<td width="70%" align="left" class="datacell1font">
									<input name="lookupdescription" size="40">
										<xsl:attribute name="value"><xsl:value-of select="lookupdescription"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell2">
								<td colspan="2">
									<input type="button" value="Save" class="nav_body" name="save" language="javascript" onclick="return save_onclick()"/>&nbsp;
             <input type="button" value="Cancel" class="nav_body" name="cancel" language="javascript" onclick="cancelFunc();"/>
								</td>
							</tr>
						</form>
						<xsl:if test="not(categoryid='')">
							<tr class="headercell">
								<td align="left" class="headercellfont" colspan="2">
            &nbsp;&nbsp;&nbsp;
           </td>
							</tr>
							<tr class="headercell">
								<td align="left" class="headercellfont">Choice&nbsp;List</td>
								<td width="100%" align="right" class="headercellfont">
									<a>
										<xsl:attribute name="href"><xsl:value-of select="newlookupitemurl"/></xsl:attribute>
										<b>Add&nbsp;Choice</b>
									</a>
            &nbsp;&nbsp;&nbsp;
           </td>
							</tr>
							<tr>
								<td colspan="10">
									<table width="100%" cellpadding="0" cellspacing="1" border="0">
										<tr class="headercell2">
											<td width="5%" class="headercellfont">&nbsp;</td>
											<td width="30%" class="headercellfont">Choice&nbsp;Label&nbsp;</td>
											<td width="45%" class="headercellfont">Description&nbsp;</td>
											<td width="10%" class="headercellfont">Choice&nbsp;Value&nbsp;</td>
											<td width="10%" class="headercellfont">Sort&nbsp;Order&nbsp;</td>
										</tr>
										<xsl:for-each select="entry">
											<tr class="datacell1">
												<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
												<xsl:choose>
													<xsl:when test="count(.)=1 and name=''">
														<td align="center" colspan="5" class="datacellnoentriesfound">
										No entries found.&nbsp;
									</td>
													</xsl:when>
													<xsl:otherwise>
														<td class="datacell1font" align="center">
															<a href="javascript:delConfirm('{deleteurl}');">
																<img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
															</a>
														</td>
														<td class="datacell1font">
															<a>
																<xsl:attribute name="href"><xsl:value-of select="editurl"/></xsl:attribute>
																<xsl:apply-templates select="name"/>
															</a>
														</td>
														<td class="datacell1font">
															<xsl:apply-templates select="description"/>
														</td>
														<td class="datacell1font">
															<xsl:apply-templates select="value"/>
														</td>
														<td class="datacell1font">
															<xsl:apply-templates select="sequence"/>
														</td>
													</xsl:otherwise>
												</xsl:choose>
											</tr>
										</xsl:for-each>
									</table>
								</td>
							</tr>
						</xsl:if>
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
