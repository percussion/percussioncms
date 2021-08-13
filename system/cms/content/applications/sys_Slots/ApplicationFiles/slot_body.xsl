<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="xalan://com.percussion.i18n.PSI18nUtils"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:template mode="slot_mainbody" match="*">
		<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
		<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
		<xsl:variable name="componentname" select="componentname"/>
		<xsl:variable name="slotlookup" select="document(/*/slotlookupurl)"/>
		<xsl:variable name="slottype" select="$slotlookup//slottype"/>
		<xsl:variable name="systemslot" select="$slotlookup//systemslot"/>
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcellfont" align="right" valign="top">  
         Edit Slot Properties
      </td>
			</tr>
			<tr class="headercell">
				<td valign="top" align="left" class="headercellfont">
					<!-- begin XSL -->
					<xsl:if test="string(editslot/slottypeid)">
						<xsl:for-each select="$slotlookup/*"> 
           Slot Name(id):&nbsp;<xsl:value-of select="slotname"/> (<xsl:value-of select="slotid"/>) 
           </xsl:for-each>
					</xsl:if>
					<!-- end XSL -->
            &nbsp;
     </td>
			</tr>
			<tr class="headercell">
				<td>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<form name="newslot" method="post" action="editslot.html">
							<input name="DBActionType" type="hidden" value="UPDATE"/>
							<input name="sys_componentname" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
							</input>
							<input name="sys_pagename" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="pagename"/></xsl:attribute>
							</input>
							<input name="slotid" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="editslot/slotid"/></xsl:attribute>
							</input>
							<xsl:for-each select="$slotlookup/*">
								<input name="systemslot" type="hidden" value="{systemslot}"/>
								<xsl:choose>
									<xsl:when test="systemslot=1">
										<tr class="datacell1">
											<td align="left" class="datacell1font" width="30%">
												<font class="reqfieldfont">*</font>Name</td>
											<td align="left" class="datacell1font" width="70%">
												<input type="hidden" name="slotname" value="{slotname}"/>
												<xsl:value-of select="slotname"/>
											</td>
										</tr>
										<tr class="datacell2">
											<td align="left" class="datacell1font">Description</td>
											<td align="left" class="datacell1font">
												<input type="hidden" name="slotdesc" value="{slotdesc}"/>
												<xsl:value-of select="slotdesc"/>
											</td>
										</tr>
										<tr class="datacell2">
											<td align="left" class="datacell1font">Slot Type</td>
											<td align="left" class="datacell1font">
												<input type="hidden" name="slottype" value="{slottype}"/>
												<xsl:choose>
													<xsl:when test="slottype=1">Inline Slot</xsl:when>
													<xsl:otherwise>Regular Slot</xsl:otherwise>
												</xsl:choose>
											</td>
										</tr>
									</xsl:when>
									<xsl:otherwise>
										<tr class="datacell1">
											<td align="left" class="datacell1font" width="30%">
												<font class="reqfieldfont">*</font>Name</td>
											<td align="left" class="datacell1font" width="70%">
												<input name="slotname" value="{slotname}"/>
											</td>
										</tr>
										<tr class="datacell2">
											<td align="left" class="datacell1font">Description</td>
											<td align="left" class="datacell1font">
												<input name="slotdesc" size="50" value="{slotdesc}"/>
											</td>
										</tr>
										<tr class="datacell2">
											<td align="left" class="datacell1font">Slot Type</td>
											<td align="left" class="datacell1font">
												<select name="slottype">
													<option value="0">Regular Slot</option>
													<option value="1">
														<xsl:if test="slottype=1">
															<xsl:attribute name="selected"/>
														</xsl:if>
												Inline Slot
											</option>
												</select>
											</td>
										</tr>
									</xsl:otherwise>
								</xsl:choose>
								<tr class="datacell2">
									<td align="left" class="datacell1font">Allowed Active Assembly Relationship Type Name</td>
									<td align="left" class="datacell1font">
										<xsl:variable name="aarelationshiptype" select="aarelationshiptype"/>
										<xsl:variable name="relationships" select="document(//aarellookupurl)//sys_Lookup"/>
										<select name="aarelationshiptype">
											<xsl:for-each select="$relationships/PSXEntry">
												<option value="{Value}">
													<xsl:if test="$aarelationshiptype=Value">
														<xsl:attribute name="selected">selected</xsl:attribute>
													</xsl:if>
													<xsl:value-of select="PSXDisplayText"/>
												</option>
											</xsl:for-each>
										</select>
									</td>
								</tr>
							</xsl:for-each>
							<tr class="datacell1">
								<td align="left" class="datacell1font" colspan="2">
									<input type="button" value="Save" name="save" language="javascript" onclick="return save_onclick()"/>&nbsp;
                  <input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
									<input type="hidden" name="doccancelurl">
										<xsl:attribute name="value"><xsl:value-of select="cancelurl"/></xsl:attribute>
									</input>
								</td>
							</tr>
						</form>
					</table>
				</td>
			</tr>
			<xsl:if test="string(editslot/slottypeid)">
				<tr class="headercell">
					<td>
						<table width="100%" cellpadding="0" cellspacing="1" border="0">
							<tr class="headercell">
								<td align="left" class="datacell1font" colspan="3">
                  &nbsp;
              </td>
							</tr>
							<tr class="headercell">
								<!--   Repeats once per category   -->
								<td valign="top" width="40%" align="left" class="headercellfont" colspan="2">Edit Slot: Allowed Content</td>
								<td valign="top" width="55%" align="right" class="headercellfont">
									<a>
										<xsl:attribute name="href"><xsl:value-of select="editslot/addvariant"/><xsl:if test="$slottype='1'">&amp;outputformat=1&amp;outputformat=2</xsl:if></xsl:attribute>
                     Add&nbsp;Allowed&nbsp;Content
                </a>
								</td>
							</tr>
							<tr class="headercell2">
								<td align="center" width="5%" class="headercell2font">&nbsp;</td>
								<td width="40%" align="center" class="headercell2font">Content&nbsp;Type&nbsp;(id)&nbsp;&nbsp;&nbsp;</td>
								<td width="55%" align="center" class="headercell2font">Variant&nbsp;(id)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
							</tr>
							<xsl:for-each select="editslot">
								<xsl:for-each select="variant">
									<xsl:if test="string(contentid)">
										<tr class="datacell1">
											<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
											<td align="center">
												<a href="javascript:delConfirm('{slotdelete}');">
													<img height="21" alt="Delete" src="../sys_resources/images/delete.gif" width="21" border="0"/>
												</a>
											</td>
											<td class="datacell1font">
												<xsl:apply-templates select="contentname"/>(
                      <xsl:apply-templates select="contentid"/>)
                    </td>
											<td class="datacell1font">
												<xsl:value-of select="variantname"/>(
                      <xsl:apply-templates select="variantid"/>)
                    </td>
										</tr>
									</xsl:if>
									<xsl:if test="count(.)=1 and contentid = ''">
										<tr class="datacell1">
											<td align="center" colspan="3" class="datacellnoentriesfound">
								No entries found.&nbsp;
							</td>
										</tr>
									</xsl:if>
								</xsl:for-each>
							</xsl:for-each>
						</table>
					</td>
				</tr>
			</xsl:if>
			<tr class="headercell">
				<td height="100%">&nbsp;</td>
				<!--   Fill down to the bottom   -->
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
