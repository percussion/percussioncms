<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="bannerinclude" select="/*/bannerincludeurl"/>
	<xsl:variable name="userstatusinclude" select="/*/userstatusincludeurl"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - Workflow Administrator</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link href="../sys_resources/css/tabs.css" rel="stylesheet" type="text/css"/>
				<script language="JavaScript" src="../sys_resources/js/checkrequired.js"><![CDATA[
]]></script>
				<script language="JavaScript" src="../sys_resources/js/formValidation.js"><![CDATA[
			]]></script>
				<script id="clientEventHandlersJS" language="javascript"><![CDATA[
				function save_onclick() {
						if(!reqField(document.UpdateStateNotif.requiredsubject.value,"Subject")) return false;
						document.UpdateStateNotif.body.value = escape(document.UpdateStateNotif.body.value);
						document.UpdateStateNotif.submit();
				}
			]]></script>
			<script language="javascript1.2">

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
							<form name="UpdateStateNotif" action="UpdateStateNotif.htm" method="get" onsubmit="return save_onclick()">
								<!--   BEGIN Banner and Login Details   -->
								<xsl:apply-templates select="*" mode="mode1"/>
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
							<xsl:when test="Subject=''">New Notification</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="Subject"/>
							</xsl:otherwise>
						</xsl:choose>
					</span>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*" mode="mode1">
		<xsl:for-each select=".">
			<table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
				<tr>
					<td id="XSpLit" colspan="2"/>
				</tr>
				<xsl:apply-templates select="." mode="mode0"/>
				<input name="workflowid" type="hidden">
					<xsl:attribute name="value"><xsl:value-of select="workflowid"/></xsl:attribute>
				</input>
				<input name="sys_componentname" type="hidden">
					<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
				</input>
				<input name="notificationid" type="hidden">
					<xsl:attribute name="value"><xsl:value-of select="notificationid"/></xsl:attribute>
				</input>
				<input name="rxorigin" type="hidden" value="editnotif"/>
				<tr class="datacell2">
					<td align="left" class="datacell1font" width="30%">ID:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
					</td>
					<td width="100%" align="left" class="datacell1font">
						<xsl:apply-templates select="notificationid"/>&nbsp;
				  </td>
				</tr>
				<tr class="datacell1">
					<td align="left" class="datacell1font">
						<font class="reqfieldfont">*</font>Subject:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
					</td>
					<td width="100%" align="left" class="datacell1font">
						<input type="text" name="requiredsubject" size="30">
							<xsl:attribute name="value"><xsl:value-of select="Subject"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<tr class="datacell1">
					<td align="left" class="datacell1font">Description:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
					</td>
					<td width="100%" align="left" class="datacell1font">
						<input type="text" name="description" size="30">
							<xsl:attribute name="value"><xsl:value-of select="description"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<tr class="datacell2">
					<td align="left" class="headercellfont">Body:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
					</td>
					<td width="100%" align="left" class="datacell1font">
						<xsl:for-each select=".">
							<textarea name="body" cols="60" rows="5">
								<xsl:value-of select="Body"/>
							</textarea>
						</xsl:for-each>
					</td>
				</tr>
				<tr class="datacell1">
					<td colspan="2" align="left" class="datacell1font">
						<input type="hidden" class="nav_body" name="DBActionType" value="UPDATE"/>
						<input type="button" class="nav_body" name="DBActionType" value="Save" onclick="javascript:save_onclick();"/>&nbsp;
						<input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
					</td>
				</tr>
				<tr class="headercell">
					<td height="100%" colspan="2">&nbsp;</td>
					<!--   Fill down to the bottom   -->
				</tr>
			</table>
			<!--   Main View Area End   -->
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
