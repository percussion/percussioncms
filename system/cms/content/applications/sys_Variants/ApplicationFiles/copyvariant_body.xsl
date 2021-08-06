<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:template mode="copyvariants_mainbody" match="*">
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcell" align="right" valign="top">
					<span class="outerboxcellfont">Copy Variant&nbsp;&nbsp;</span>
				</td>
			</tr>
			<tr class="headercell">
				<td>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<form name="copyvariant" method="post" action="updatecommunity.html">
							<input name="DBActionType" type="hidden" value="INSERT"/>
							<input name="sys_componentname" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
							</input>
							<input name="sys_pagename" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="pagename"/></xsl:attribute>
							</input>
							<tr class="headercell">
								<td valign="top" align="left" class="headercellfont" colspan="2">Source Content Type</td>
							</tr>
							<tr class="datacell1">
								<td align="left" class="datacell1font" width="30%">
									Please select the source to be copied
								</td>
								<td width="100%" align="left" class="datacell1font">
									<select name="srccontenttype" onchange="javascript:contenttype_onchange()">
                              <option>Select a Content Type</option>
									</select>
								</td>
							</tr>
							<tr class="headercell">
								<td valign="top" align="left" class="headercellfont" colspan="2">Source Variant</td>
							</tr>
							<tr class="datacell1">
								<td align="left" class="datacell1font" width="30%">
									Please select the source to be copied
								</td>
								<td width="100%" align="left" class="datacell1font">
									<select name="srcvariant">
                              <option>Select a Variant</option>
									</select>
								</td>
							</tr>
							<tr class="headercell">
								<td valign="top" align="left" class="headercellfont" colspan="2">New Variant</td>
							</tr>
							<tr class="datacell1">
								<td class="datacell1font">
									<font class="reqfieldfont">*</font>Name</td>
								<td class="datacell1font">
									<input name="desc" size="30"/>
								</td>
							</tr>
							<tr class="datacell1">
								<td colspan="2">
									<input type="button" value="Create" class="nav_body" name="Copy" onclick="javascript:save_onclick()"/>&nbsp;
                           <input type="button" value="Cancel" class="nav_body" name="cancel" language="javascript" onclick="javascript:history.back();"/>
								</td>
							</tr>
						</form>
					</table>
				</td>
			</tr>
			<tr class="headercell">
				<td height="100%">&nbsp;</td>
				<!--   Fill down to the bottom   -->
			</tr>
            <script language="javascript">
            javascript:onFormLoad();
         </script>
		</table>
	</xsl:template>
</xsl:stylesheet>
