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
	<xsl:template mode="copyworkfloweditbody" match="*">
		<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
			<form name="CopyWorkflow" action="" method="post" enctype="multipart/form-data" onsubmit="return save_onclick()">
				<!--   BEGIN Banner and Login Details   -->
				<tr class="outerboxcell">
					<td align="right" valign="top" colspan="2">
						<span class="outerboxcellfont">
				Copy  Workflow
                  </span>
					</td>
				</tr>
				<tr class="headercell">
					<td valign="top" colspan="2">
						<!--   View Start   -->
						<input name="sys_componentname" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
						</input>
						<input name="workflowid" type="hidden" value=""/>
						<input name="DBActionType" type="hidden" value="INSERT"/>
						<input name="rxorigin" type="hidden" value="editwf"/>
						<table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
							<tr class="headercell">
								<td valign="top" align="left" class="headercellfont" colspan="2">Souce Workflow</td>
							</tr>
							<tr class="datacell1">
								<td align="left" class="datacell1font" width="30%">
									Please select the source to be copied
								</td>
								<td width="100%" align="left" class="datacell1font">
									<select name="updateurls">
										<xsl:for-each select="/CopyWorkflowEdit/Workflow">
											<option value="{UpdateLink}">
												<xsl:value-of select="WorkflowName"/>
											</option>
										</xsl:for-each>
									</select>
								</td>
							</tr>
							<tr class="headercell">
								<td valign="top" align="left" class="headercellfont" colspan="2">New Workflow</td>
							</tr>
							<tr class="datacell1">
								<td align="left" class="datacell1font" width="30%">
									<font class="reqfieldfont">*</font>Name:
							 <img src="/sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<input type="text" name="requiredname" size="30">
										<xsl:attribute name="value"><xsl:value-of select="Name"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell1">
								<td colspan="2" align="left" class="datacell1font">
									<input type="button" class="nav_body" name="DBActionType" value="Create" onclick="javascript:save_onclick	();"/>&nbsp;
									<input type="button" class="nav_body" name="Cancel" value="Cancel" onclick="javascript:history.back();"/>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr class="headercell">
					<td height="100%">&nbsp;</td>
					<!--   Fill down to the bottom   -->
				</tr>
			</form>
		</table>
	</xsl:template>
</xsl:stylesheet>
