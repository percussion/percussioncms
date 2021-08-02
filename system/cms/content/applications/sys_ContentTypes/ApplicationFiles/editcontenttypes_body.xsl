<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:template mode="edcontenttypes_mainbody" match="*">
		<xsl:variable name="userroles" select="document(userrolesurl)/UserStatus"/>
		<xsl:variable name="componentcontext" select="document(contexturl)/componentcontext/context"/>
		<xsl:variable name="componentname" select="componentname"/>
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="outerboxcell">
				<td class="outerboxcell" align="right" valign="top" colspan="2">
					<span class="outerboxcellfont">Edit Content Type</span>
				</td>
			</tr>
			<tr class="headercell">
				<td>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<form name="editcontenttype" method="post">
							<xsl:attribute name="action"><xsl:choose><!--when contenttype is created --><xsl:when test="contenttypeid=''"><xsl:choose><xsl:when test="//sys_community=0">newcontenttype_gen.html</xsl:when><xsl:otherwise>newcontenttype_comm.html</xsl:otherwise></xsl:choose></xsl:when><!--when contenttype is updated --><xsl:otherwise><xsl:value-of select="'updatecontenttype.html'"/></xsl:otherwise></xsl:choose></xsl:attribute>
							<input name="DBActionType" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="dbactiontype"/></xsl:attribute>
							</input>
							<input type="hidden" name="doccancelurl">
								<xsl:attribute name="value"><xsl:value-of select="cancelurl"/></xsl:attribute>
							</input>
							<input name="contenttypeid" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="contenttypeid"/></xsl:attribute>
							</input>
							<input name="objecttype" type="hidden">
								<xsl:attribute name="value"><xsl:choose><xsl:when test="objecttype!=''"><xsl:value-of select="objecttype"/></xsl:when><xsl:otherwise>1</xsl:otherwise></xsl:choose></xsl:attribute>
							</input>
							<input name="sys_componentname" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
							</input>
							<input name="sys_pagename" type="hidden">
								<xsl:attribute name="value"><xsl:value-of select="pagename"/></xsl:attribute>
							</input>
							<tr class="headercell2">
								<td width="30%" align="left" class="headercell2font">Content&nbsp;Type&nbsp;ID&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								<td width="90%" align="left" class="headercell2font">&nbsp;
             <xsl:apply-templates select="contenttypeid"/>
								</td>
							</tr>
							<tr class="datacell1">
								<td class="datacell1font">
									<font class="reqfieldfont">*</font>Name</td>
								<td class="datacell1font">
									<input name="contenttypename" size="30">
										<xsl:attribute name="value"><xsl:value-of select="contenttypename"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell2">
								<td class="datacell1font">Description</td>
								<td class="datacell1font">
									<input name="contenttypedesc" size="60">
										<xsl:attribute name="value"><xsl:value-of select="contenttypedesc"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell1">
								<td class="datacell1font">
									<font class="reqfieldfont">*</font>New Request URL</td>
								<td class="datacell1font">
									<input name="contenttypenewurl" size="60">
										<xsl:attribute name="value"><xsl:value-of select="contenttypenewurl"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell2">
								<td class="datacell1font">
									<font class="reqfieldfont">*</font>Query Request URL</td>
								<td class="datacell1font">
									<input name="contenttypequeryurl" size="60">
										<xsl:attribute name="value"><xsl:value-of select="contenttypequeryurl"/></xsl:attribute>
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
				<td height="100%">&nbsp;</td>
				<!--   Fill down to the bottom   -->
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
