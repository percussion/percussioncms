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
	<xsl:template mode="editconfiguration_mainbody" match="*">
		<form name="editconfiguration" method="post" action="updateconfiguration.html">
			<input type="hidden" name="communityid" value="{//communityid}"/>
			<input type="hidden" name="contenttypeid" value="{//contenttypeid}"/>
			<input type="hidden" name="contenttypeid" value="{//contenttypename}"/>
			<input type="hidden" name="workflowid" value="{//workflowid}"/>
			<input type="hidden" name="locale" value="{//locale}"/>
			<input type="hidden" name="locale" value="{//localename}"/>
			<input type="hidden" name="DBActionType" value="update"/>
			<input type="hidden" name="sys_componentname" value="{//componentname}"/>
			<input type="hidden" name="sys_pagename" value="{//pagename}"/>
			<input type="hidden" name="doccancelurl" value="{//doccancelurl}"/>
			<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
				<tr class="outerboxcell">
					<td class="outerboxcellfont" align="right" valign="top">
         Edit Configuration
      </td>
				</tr>
				<tr class="headercell">
					<td>
						<table width="100%" cellpadding="0" cellspacing="1" border="0">
							<tr class="headercell2">
								<td width="30%" align="left" class="headercell2font">Content&nbsp;Type :&nbsp;Locale</td>
								<td width="90%" align="left" class="headercell2font">&nbsp;
             								<xsl:value-of select="//contenttypename"/><xsl:if test="//contenttypename!=''"> : </xsl:if><xsl:value-of select="//localename"/>
								</td>
							</tr>
							<tr class="datacell1" border="0">
								<td align="left" class="datacell1font">Community:</td>
								<td align="left" class="datacell1font"> 
									<select name="sys_community" onchange="return community_onchange('no')">
										<option>Leave this for NS</option>
										<option>1</option>
										<option>2</option>
										<option>3</option>
										<option>4</option>
										<option>5</option>
									</select>
								</td>
							</tr>
							<tr class="datacell2" border="0">
								<td align="left" class="datacell2font">Content Type:</td>
								<td align="left" class="datacell2font">
									<xsl:choose>
										<xsl:when test="//contenttypeid!=''">
											<xsl:value-of select="//contenttypename"/>
											<input type="hidden" name="sys_contenttype" value="{//contenttypeid}"/>
										</xsl:when>
										<xsl:otherwise>
											<select name="sys_contenttype" onchange="return contenttype_onchange('no')">
												<option>Leave this for NS</option>
												<option>1</option>
												<option>2</option>
												<option>3</option>
												<option>4</option>
												<option>5</option>
											</select>
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>
							<tr class="datacell1" border="0">
								<td align="left" class="datacell1font">Workflow:</td>
								<td align="left" class="datacell1font">
									<select name="sys_workflow">
										<option>Leave this for NS</option>
										<option>1</option>
										<option>2</option>
										<option>3</option>
										<option>4</option>
										<option>5</option>
									</select>
								</td>
							</tr>
							<tr class="datacell1" border="0">
								<td align="left" class="datacell1font">Locale:</td>
								<td align="left" class="datacell1font">
									<xsl:choose>
										<xsl:when test="//locale!=''">
											<xsl:value-of select="//localename"/>
											<input type="hidden" name="sys_locale" value="{//locale}"/>
										</xsl:when>
										<xsl:otherwise>
											<select name="sys_locale">
												<option>Leave this for NS</option>
												<option>1</option>
												<option>2</option>
												<option>3</option>
												<option>4</option>
												<option>5</option>
											</select>
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>
							<tr class="datacell1">
								<td colspan="2">
									<input type="button" value="Save" class="nav_body" name="save" onclick="javascript:document.editconfiguration.submit()"/>&nbsp;
             <input type="button" value="Cancel" class="nav_body" name="cancel" language="javascript" onclick="cancelFunc();"/>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr class="headercell">
					<td height="100%">&nbsp;</td>
					<!--   Fill down to the bottom   -->
				</tr>
			</table>
		</form>
		<script>
	            javascript:onFormLoad("yes");
            </script>
	</xsl:template>
</xsl:stylesheet>
