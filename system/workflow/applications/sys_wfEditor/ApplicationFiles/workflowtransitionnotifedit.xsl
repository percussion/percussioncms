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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
	<xsl:include href="file:sys_wfLookups/workflowactionlistbox.xsl"/>
	<xsl:include href="file:sys_wfLookups/exttransitionnotifs.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="extroles" select="/*/URL/extroles"/>
	<xsl:variable name="extstates" select="/*/URL/extstates"/>
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
						if(document.UpdateTransitionNotif.notificationid[document.UpdateTransitionNotif.notificationid.selectedIndex].value==""){
								alert("Notification: field is a required field");
								return false;
						}
						document.UpdateTransitionNotif.submit();
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
							<form name="UpdateTransitionNotif" action="UpdateTransitionNotif.htm" method="get" onsubmit="return save_onclick()">
								<!--   BEGIN Banner and Login Details   -->
								<xsl:apply-templates select="*" mode="mode26"/>
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
		<xsl:variable name="transitioninfo" select="document(/*/transitioninfo)/*"/>
		<xsl:for-each select=".">
			<tr>
				<td class="outerboxcell" align="right" valign="top">
					<span class="outerboxcellfont">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="$transitioninfo/workflowslink"/></xsl:attribute>
				Workflows
			</a> &gt;
			<a>
							<xsl:attribute name="href"><xsl:value-of select="$transitioninfo//workflowlink"/></xsl:attribute>
							<xsl:apply-templates select="$transitioninfo/@workflowname"/>
						</a> &gt;
			<a>
							<xsl:attribute name="href"><xsl:value-of select="$transitioninfo/statelink"/></xsl:attribute>
							<xsl:apply-templates select="$transitioninfo/@statename"/>
						</a> &gt;
			<a>
							<xsl:attribute name="href"><xsl:value-of select="$transitioninfo/transitionlink"/></xsl:attribute>
							<xsl:apply-templates select="$transitioninfo/@transitionlabel"/>
						</a> &gt;Transition Notification
          </span>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*" mode="mode25">
		<xsl:for-each select=".">
			<tr>
				<td valign="top">
					<!--   View Start   -->
					<input name="sys_componentname" type="hidden">
						<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
					</input>
					<input name="workflowid" type="hidden">
						<xsl:attribute name="value"><xsl:value-of select="workflowid"/></xsl:attribute>
					</input>
					<input name="stateid" type="hidden">
						<xsl:attribute name="value"><xsl:value-of select="stateid"/></xsl:attribute>
					</input>
					<input name="transitionid" type="hidden">
						<xsl:attribute name="value"><xsl:value-of select="transitionid"/></xsl:attribute>
					</input>
					<input name="transitionnotifid" type="hidden">
						<xsl:attribute name="value"><xsl:value-of select="transitionnotifid"/></xsl:attribute>
					</input>
					<input name="sys_isaging" type="hidden">
						<xsl:attribute name="value"><xsl:value-of select="isaging"/></xsl:attribute>
					</input>
					<input name="rxorigin" type="hidden" value="edittransnotif"/>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<xsl:for-each select="transitionnotif">
							<tr class="datacell1">
								<td align="left" class="headercellfont" width="30%">ID:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="70%" align="left" class="datacell1font">
									<xsl:apply-templates select="transitionnotifid"/>
								</td>
							</tr>
							<tr class="datacell2">
								<td align="left" class="datacell1font">Notification: Subject(ID)
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<xsl:variable name="notificationid" select="notificationid"/>
									<select name="notificationid">
										<option value="">--Choose--</option>
										<xsl:for-each select="document(/*/extstatenotifsurl)/*/statenotif">
											<xsl:if test="not(notifid='')">
												<option value="{notifid}">
													<xsl:if test="$notificationid=notifid">
														<xsl:attribute name="selected"/>
													</xsl:if>
													<xsl:value-of select="subject"/>(<xsl:value-of select="notifid"/>)
							  </option>
											</xsl:if>
										</xsl:for-each>
									</select>
								</td>
							</tr>
							<tr class="datacell1">
								<td align="left" class="datacell1font">State Role Recipients Type:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<xsl:variable name="recipienttype" select="transitionnotifreciptype"/>
									<select name="recipienttype">
										<xsl:for-each select="document(/*/rolerecipientsurl)/*/item">
											<option value="{value}">
												<xsl:if test="$recipienttype=value">
													<xsl:attribute name="selected"/>
												</xsl:if>
												<xsl:value-of select="display"/>
											</option>
										</xsl:for-each>
									</select>
								</td>
							</tr>
							<tr class="datacell2">
								<td align="left" class="datacell1font">Additional Recipient List:<br/>(Comma separated list of email addresses)
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<textarea name="addrecipientlist" cols="60" rows="5">
										<xsl:value-of select="transitionnotifaddrecip"/>
									</textarea>
								</td>
							</tr>
							<tr class="datacell1">
								<td align="left" class="datacell1font">CC List:<br/>(Comma separated list of email addresses)
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<textarea name="cclist" cols="60" rows="5">
										<xsl:value-of select="transitionnotifcclist"/>
									</textarea>
								</td>
							</tr>
						</xsl:for-each>
						<tr class="datacell2">
							<td colspan="2" align="left" class="datacell1font">
								<input type="hidden" class="nav_body" name="DBActionType" value="UPDATE"/>
								<input type="button" class="nav_body" name="DBActionType" value="Save" onclick="javascript:save_onclick();"/>&nbsp;
								<input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*" mode="mode26">
		<xsl:for-each select=".">
			<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td id="XSpLit"/>
				</tr>
				<xsl:apply-templates select="." mode="mode0"/>
				<tr>
					<td width="100%" class="headercell">&nbsp;
              </td>
				</tr>
				<xsl:apply-templates select="." mode="mode25"/>
				<tr class="headercell">
					<td height="100%">&nbsp;</td>
					<!--   Fill down to the bottom   -->
				</tr>
			</table>
			<!--   Main View Area End   -->
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
