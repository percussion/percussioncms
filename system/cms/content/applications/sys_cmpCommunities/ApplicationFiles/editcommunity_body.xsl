<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "../../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "../../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "../../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:template mode="editcommunity_mainbody" match="*">
		<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
		<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
		<xsl:variable name="componentname" select="componentname"/>
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcell" align="right" valign="top">
					<span class="outerboxcellfont">Edit Community&nbsp;&nbsp;</span>
				</td>
			</tr>
			<tr class="headercell">
				<td>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<form name="editcommunity" method="post" action="updatecommunity.html">
							<input name="DBActionType" type="hidden" value="UPDATE"/>
							<input type="hidden" name="doccancelurl">
								<xsl:attribute name="value"><xsl:value-of select="cancelurl"/></xsl:attribute>
							</input>
							<input name="communityid" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="communityid"/></xsl:attribute>
							</input>
							<input name="sys_componentname" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
							</input>
							<input name="sys_pagename" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="pagename"/></xsl:attribute>
							</input>
							<tr class="headercell2">
								<td width="30%" align="left" class="headercell2font">Community&nbsp;ID&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								<td width="90%" align="left" class="headercell2font">&nbsp;
             <xsl:apply-templates select="communityid"/>
								</td>
							</tr>
							<tr class="datacell1">
								<td class="datacell1font">
									<font class="reqfieldfont">*</font>Name</td>
								<td class="datacell1font">
									<input name="communityname" size="30">
										<xsl:attribute name="value"><xsl:value-of select="communityname"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell2">
								<td class="datacell1font">Description</td>
								<td class="datacell1font">
									<input name="communitydesc" size="60">
										<xsl:attribute name="value"><xsl:value-of select="communitydesc"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell1">
								<td colspan="2">
									<input type="button" value="Save" class="nav_body" name="save" onclick="javascript:save_onclick()"/>&nbsp;
             <input type="button" value="Cancel" class="nav_body" name="cancel" language="javascript" onclick="cancelFunc();"/>
								</td>
							</tr>
						</form>
					</table>
				</td>
			</tr>
			<tr class="headercell">
				<td>
					<xsl:if test="not(communityid='')">
						<table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
							<tr class="outerboxcell">
								<td colspan="2">&nbsp;</td>
							</tr>
							<tr class="outerboxcell">
								<td class="outerboxcellfont" colspan="2">&nbsp;Community Properties</td>
							</tr>
							<tr class="headercell2">
								<td class="headercell2font" width="30%" align="left" height="12">
									&nbsp;Property Name
								</td>
								<td class="headercell2font" width="70%" align="left">
									&nbsp;Property Description
								</td>
							</tr>
							<tr class="datacell1">
								<td class="datacell1font" align="left">
									<a href="{rolecommlookupurl}">&nbsp;Roles</a>
								</td>
								<td class="datacell1font" align="left">
									&nbsp;Allowable roles
								</td>
							</tr>
							<tr class="datacell2">
								<td class="datacell1font" align="left">
									<a href="{wfcommlookupurl}">&nbsp;Workflows</a>
								</td>
								<td class="datacell1font" align="left">
									&nbsp;Allowable  workflow definitions
								</td>
							</tr>
							<tr class="datacell1">
								<td class="datacell1font" align="left">
									<a href="{ctypecommlookupurl}">&nbsp;Content Types</a>
								</td>
								<td class="datacell1font" align="left">
									&nbsp;Allowable content types
								</td>
							</tr>
							<tr class="datacell2">
								<td class="datacell1font" align="left">
									<a href="{varcommlookupurl}">&nbsp;Variants</a>
								</td>
								<td class="datacell1font" align="left">
									&nbsp;Allowable variants
								</td>
							</tr>
							<tr class="datacell1">
								<td class="datacell1font" align="left">
									<a href="{compcommlookupurl}">&nbsp;Components</a>
								</td>
								<td class="datacell1font" align="left">
									&nbsp;Allowable interface components
								</td>
							</tr>
							<tr class="datacell1">
								<td class="datacell1font" align="left">
									<a href="{sitecommlookupurl}">&nbsp;Sites</a>
								</td>
								<td class="datacell1font" align="left">
									&nbsp;Allowable sites
								</td>
							</tr>
						</table>
					</xsl:if>
				</td>
			</tr>
			<tr class="headercell">
				<td height="100%">&nbsp;</td>
				<!--   Fill down to the bottom   -->
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
