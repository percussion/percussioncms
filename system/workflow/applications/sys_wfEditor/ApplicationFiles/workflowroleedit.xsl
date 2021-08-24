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
	<xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
	<xsl:output method="html" omit-xml-declaration="yes"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - Workflow Administrator</title>
				<link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
				<link href="/sys_resources/css/tabs.css" rel="stylesheet" type="text/css"/>
				<script src="/sys_resources/js/checkrequired.js"><![CDATA[
]]></script>
				<script src="/sys_resources/js/formValidation.js"><![CDATA[
			]]></script>
				<script id="clientEventHandlersJS" language="javascript"><![CDATA[
				function save_onclick(callSubmit) {
						if(!reqField(document.UpdateRoles.requiredname.value,"Name")) return false;
						if (callSubmit)
							document.UpdateRoles.submit();
				}
			]]></script>
			<script>
			   function cancelFunc()
			   {
			      document.location.href = '<xsl:value-of select="//cancelurl"/>';
			   }
			</script>			
			</head>
			<body class="backgroundcolour" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
				<!--   BEGIN Banner and Login Details   -->
				<xsl:call-template name="bannerAndUserStatus"/>
				<!--   END Banner and Login Details   -->
				<table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
					<tr>
						<td align="middle" valign="top" width="150" height="100%" class="outerboxcell">
							<!--   start left nav slot   -->
							<!--   start left nav slot   -->
							<xsl:for-each select="document($relatedlinks)/*/component[@slotname='slt_wf_nav']">
								<xsl:copy-of select="document(url)/*/body/*"/>
							</xsl:for-each>
							<!--   end left nav slot   -->
						</td>
						<td align="middle" width="100%" valign="top" height="100%" class="outerboxcell">
							<!--   start main body slot   -->
							<!--   start main body slot   -->
							<form name="UpdateRoles" action="UpdateRoles.htm" method="get" onsubmit="return save_onclick(false)">
								<!--   BEGIN Banner and Login Details   -->
								<xsl:apply-templates select="*" mode="mode9"/>
							</form>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="*">
		<xsl:choose>
			<xsl:when test="text()">
				<xsl:choose>
					<xsl:when test="@no-escaping">
						<xsl:value-of select="." disable-output-escaping="yes"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>&nbsp;</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="not(position()=last())">
			<br id="XSpLit"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="attribute::*">
		<xsl:value-of select="."/>
		<xsl:if test="not(position()=last())">
			<br id="XSpLit"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="*" mode="mode0">
		<xsl:variable name="workflowinfo" select="document(/*/workflowinfourl)/*"/>
		<xsl:for-each select=".">
			<tr>
				<td class="outerboxcell" align="right" valign="top" colspan="2">
					<span class="outerboxcellfont">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="$workflowinfo/workflowslink"/></xsl:attribute>
							Workflows
						</a> &gt;
						<a>
							<xsl:attribute name="href"><xsl:value-of select="$workflowinfo/workflowlink"/></xsl:attribute>
							<xsl:apply-templates select="$workflowinfo/@workflowname"/>
						</a>  &gt;
						<xsl:choose>
							<xsl:when test="workflowRoleNew/Name=''">New Role</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="workflowRoleNew/Name"/>
							</xsl:otherwise>
						</xsl:choose>
					</span>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="workflowid" mode="mode1">
		<xsl:for-each select=".">
			<input name="workflowid" type="hidden">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="roleid" mode="mode2">
		<xsl:for-each select=".">
			<input name="roleid" type="hidden">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="workflowRoleNew" mode="mode7">
		<xsl:variable name="serverroles" select="document(//serverrolesurl)/*"/>
		<xsl:variable name="excluderoles" select="document(//excluderoles)/*"/>
		<xsl:variable name="addableroles" select="$serverroles/role[not(@name=$excluderoles/role/name)]"/>
		<xsl:for-each select=".">
			<xsl:variable name="selectedrole" select="Name"/>
			<xsl:choose>
				<xsl:when test="count($addableroles) or $selectedrole!=''">
					<tr class="datacell1">
						<td align="left" class="datacell1font" width="20%">
							<font class="reqfieldfont">*</font>Name:
					 <img src="/sys_resources/images/invis.gif" height="1" width="100" border="0"/>
						</td>
						<td width="80%" align="left" class="datacell1font">
							<select name="requiredname">
								<!-- when adding a new role get all available server based roles, which have not been added yet -->
								<xsl:for-each select="$addableroles">
									<xsl:choose>
										<xsl:when test="@name=$selectedrole">
											<option value="{@name}" selected="true">
												<xsl:value-of select="@name"/>
											</option>
										</xsl:when>
										<xsl:otherwise>
											<option value="{@name}">
												<xsl:value-of select="@name"/>
											</option>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
								<!-- when changing an existing role, add to the above list one more role that user selected -->
								<xsl:if test="Name != ''">
									<option value="{Name}" selected="true">
										<xsl:value-of select="Name"/>
									</option>
								</xsl:if>
							</select>
						</td>
					</tr>
					<tr class="datacell2">
						<td align="left" class="datacell1font" width="30%">Description:
					 <img src="/sys_resources/images/invis.gif" height="1" width="100" border="0"/>
						</td>
						<td width="70%" align="left" class="datacell1font">
							<input type="text" name="roledesc" size="30">
								<xsl:attribute name="value"><xsl:value-of select="roledesc"/></xsl:attribute>
							</input>
						</td>
					</tr>
					<tr class="datacell1">
						<td colspan="2" align="left" class="datacell1font">
							<input type="hidden" class="nav_body" name="DBActionType" value="UPDATE"/>
							<input type="button" class="nav_body" name="DBActionType" value="Save" onclick="save_onclick(true);"/>&nbsp;
							<input type="button" value="Cancel" name="cancel" onclick="cancelFunc();"/>
						</td>
					</tr>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="NoEntries"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*" mode="mode8">
		<xsl:for-each select=".">
			<input name="sys_componentname" type="hidden">
				<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
			</input>
			<xsl:apply-templates select="workflowid" mode="mode1"/>
			<xsl:apply-templates select="roleid" mode="mode2"/>
			<input name="rxorigin" type="hidden" value="editrole"/>
			<xsl:apply-templates select="workflowRoleNew" mode="mode7"/>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*" mode="mode9">
		<xsl:for-each select=".">
			<table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
				<xsl:apply-templates select="." mode="mode0"/>
				<tr class="datacell2">
					<td width="100%" colspan="2">
                &nbsp;
              </td>
				</tr>
				<xsl:apply-templates select="." mode="mode8"/>
				<tr class="headercell">
					<td height="100%" colspan="2">&nbsp;</td>
					<!--   Fill down to the bottom   -->
				</tr>
			</table>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="NoEntries">
		<tr class="datacell1">
			<td align="center" colspan="3" class="datacellnoentriesfound">
            No entries found.&nbsp;
         </td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
