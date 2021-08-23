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
	<xsl:include href="file:sys_wfLookups/workflowactionlistbox.xsl"/>
	<xsl:include href="file:sys_wfLookups/exttransitionnotifs.xsl"/>
	<xsl:include href="file:sys_wfLookups/exttransitionroles.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="extroles" select="/*/URL/extroles"/>
	<xsl:variable name="extstates" select="/*/URL/extstates"/>
	<xsl:variable name="bannerinclude" select="/*/bannerincludeurl"/>
	<xsl:variable name="userstatusinclude" select="/*/userstatusincludeurl"/>
	<xsl:variable name="exttransitionnotifs" select="/*/URL/exttransitionnotifs"/>
	<xsl:variable name="exttransitionroles" select="/*/URL/exttransitionroles"/>
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
				<script language="JavaScript" src="../sys_resources/js/delconfirm.js"><![CDATA[
]]></script>
				<script language="JavaScript" src="../sys_resources/js/formValidation.js"><![CDATA[
			]]></script>
				<script id="clientEventHandlersJS" language="javascript"><![CDATA[
				function save_onclick() {
						if(!reqField(document.UpdateTransition.requiredlabel.value,"Label")) return false;
						if(!reqField(document.UpdateTransition.requiredtrigger.value,"Trigger")) return false;
						if(document.UpdateTransition.transitiontype.value!=1){
							if(document.UpdateTransition.approvaltype[document.UpdateTransition.approvaltype.selectedIndex].value==2)
							{
								document.UpdateTransition.requiredapprovals.value = -1;
							}
							else
							{
								if(!reqNumberField(document.UpdateTransition.requiredapprovals.value,"Approvals Required")) return false;
								if(parseInt(document.UpdateTransition.requiredapprovals.value,10)<1){
									alert("Approvals Required: field value should be greater than or equal to 1");
									return false;
								}
								document.UpdateTransition.requiredapprovals.value = parseInt(document.UpdateTransition.requiredapprovals.value,10);

							}
						}
						if(document.UpdateTransition.transitiontype.value==1){
							if(document.UpdateTransition.agingtype.selectedIndex!=2){
								if(!reqNumberField(document.UpdateTransition.aginginterval.value,"Aging Interval")) return false;
								if(parseInt(document.UpdateTransition.aginginterval.value,10)<1){
									alert("Aging Interval: field value should be greater than or equal to 1");
									return false;
								}
							}
							else{
								if(document.UpdateTransition.systemfield.selectedIndex==0){
									alert("System Field: is a required field.");
									return false;
								}
								if(!numberField(document.UpdateTransition.aginginterval.value,"Aging Interval")) return false;
								if(parseInt(document.UpdateTransition.aginginterval.value,10)<1){
									alert("Aging Interval: field value should be greater than or equal to 1");
									return false;
								}
							}
						}
						document.UpdateTransition.submit();
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
							<form name="UpdateTransition" action="UpdateTransition.htm" method="get" onsubmit="return save_onclick()">
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
		<xsl:variable name="stateinfo" select="document(/*/stateinfourl)/*"/>
		<xsl:for-each select=".">
			<tr>
				<td class="outerboxcell" align="right" valign="top">
					<span class="outerboxcellfont">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="$stateinfo/workflowslink"/></xsl:attribute>
				Workflows
			</a> &gt;
			<a>
							<xsl:attribute name="href"><xsl:value-of select="$stateinfo/workflowlink"/></xsl:attribute>
							<xsl:apply-templates select="$stateinfo/@workflowname"/>
						</a> &gt;
			<a>
							<xsl:attribute name="href"><xsl:value-of select="$stateinfo/statelink"/></xsl:attribute>
							<xsl:apply-templates select="$stateinfo/@statename"/>
						</a> &gt;
			<xsl:choose>
							<xsl:when test="transition/label=''"> New <xsl:if test="isaging='1'">Aging </xsl:if>Transition</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="transition/label"/>
							</xsl:otherwise>
						</xsl:choose>
					</span>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*" mode="mode14">
		<xsl:for-each select=".">
			<tr class="datacell2">
				<td align="left" class="datacell1font">To-State:
          <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				</td>
				<td width="100%" align="left" class="datacell1font">
					<select name="tostate" size="1">
						<xsl:attribute name="selected"><xsl:value-of select="transition/tostateid"/></xsl:attribute>
						<xsl:for-each select="state">
							<option>
								<xsl:variable name="value">
									<xsl:value-of select="stateid"/>
								</xsl:variable>
								<xsl:if test="$this/workflowtransitionedit/transition/tostateid=$value">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:attribute name="value"><xsl:value-of select="stateid"/></xsl:attribute>
								<xsl:apply-templates select="name"/>
							</option>
						</xsl:for-each>
					</select>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*" mode="mode19">
		<xsl:for-each select="role">
			<option>
				<xsl:variable name="value">
					<xsl:value-of select="name"/>
				</xsl:variable>
				<xsl:if test="$this/workflowtransitionedit/transition/roles=$value">
					<xsl:attribute name="selected"/>
				</xsl:if>
				<xsl:attribute name="value"><xsl:value-of select="name"/></xsl:attribute>
				<xsl:apply-templates select="name"/>
			</option>
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
					<input name="transitiontype" type="hidden">
						<xsl:attribute name="value"><xsl:value-of select="isaging"/></xsl:attribute>
					</input>
					<xsl:variable name="isagingvar">
						<xsl:value-of select="isaging"/>
					</xsl:variable>
					<input name="rxorigin" type="hidden" value="edittrans"/>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<tr class="datacell1">
							<td align="left" class="headercellfont">ID:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
							</td>
							<td width="100%" align="left" class="datacell1font">
								<xsl:apply-templates select="transitionid"/>
							</td>
						</tr>
						<xsl:for-each select="transition">
							<tr class="datacell2">
								<td width="30%" align="left" class="datacell1font">
									<font class="reqfieldfont">*</font>Label:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="70%" align="left" class="datacell1font">
									<input type="text" name="requiredlabel" size="30">
										<xsl:attribute name="value"><xsl:value-of select="label"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell1">
								<td align="left" class="datacell1font">Description:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<input type="text" name="description" size="40">
										<xsl:attribute name="value"><xsl:value-of select="desc"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell2">
								<td align="left" class="datacell1font">
									<font class="reqfieldfont">*</font>Trigger:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<input type="text" name="requiredtrigger" size="30">
										<xsl:attribute name="value"><xsl:value-of select="trigger"/></xsl:attribute>
									</input>
								</td>
							</tr>
							<tr class="datacell1">
								<td align="left" class="datacell1font">From-State:
					 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<xsl:variable name="stateidvar">
										<xsl:value-of select="../stateid"/>
									</xsl:variable>
									<span>
										<xsl:value-of select="document($extstates)/*/state[stateid=$stateidvar]/name"/>
									</span>
									<span>&nbsp;(</span>
									<span>
										<xsl:apply-templates select="../stateid"/>
									</span>
									<span>)&nbsp;</span>
								</td>
							</tr>
							<xsl:apply-templates select="document($extstates)" mode="mode14"/>
							<xsl:if test="not($isagingvar='1')">
								<tr class="datacell1">
									<td align="left" class="datacell1font">
										<font class="reqfieldfont"></font>Approval Type:
						 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
									</td>
									<td width="100%" align="left" class="datacell1font">
										<select name="approvaltype">
												<option value="1">Specified Number</option>
												<option value="2">
												<xsl:if test="approvals='-1'"><xsl:attribute name="selected">1</xsl:attribute></xsl:if>	
												Each Role</option>
										</select>
									</td>
								</tr>
								<tr class="datacell1">
									<td align="left" class="datacell1font">
										<font class="reqfieldfont">*</font>Approvals Required:
						 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
									</td>
									<td width="100%" align="left" class="datacell1font">
										<input type="text" name="requiredapprovals" size="6">
											<xsl:attribute name="value">
												<xsl:choose>
													<xsl:when test="approvals=''">1</xsl:when>
													<xsl:when test="approvals='-1'"></xsl:when>
													<xsl:otherwise><xsl:value-of select="approvals"/></xsl:otherwise>
												</xsl:choose>
											</xsl:attribute>
										</input>&nbsp;(Required if Approval Type is set to "Specified Number")
									</td>
								</tr>
								<tr class="datacell2">
									<td align="left" class="datacell1font">Comment:
						 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
									</td>
									<td width="100%" align="left" class="datacell1font">
										<select name="commentrequired">
											<xsl:attribute name="selected"><xsl:value-of select="commentrequired"/></xsl:attribute>
											<option value="n">
												<xsl:if test="commentrequired=&apos;n&apos;">
													<xsl:attribute name="selected"/>
												</xsl:if>
								Optional
						  </option>
											<option value="y">
												<xsl:if test="commentrequired=&apos;y&apos;">
													<xsl:attribute name="selected"/>
												</xsl:if>
								Required
						  </option>
											<option value="d">
												<xsl:if test="commentrequired=&apos;d&apos;">
													<xsl:attribute name="selected"/>
												</xsl:if>
								Do not show
						  </option>
										</select>
									</td>
								</tr>
								<tr class="datacell2">
									<td align="left" class="datacell1font">Default Transition:
						 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
									</td>
									<td width="100%" align="left" class="datacell1font">
										<select name="defaulttransition">
											<xsl:attribute name="selected"><xsl:value-of select="defaulttransition"/></xsl:attribute>
											<option value="n">
												<xsl:if test="defaulttransition=&apos;n&apos;">
													<xsl:attribute name="selected"/>
												</xsl:if>No</option>
											<option value="y">
												<xsl:if test="defaulttransition=&apos;y&apos;">
													<xsl:attribute name="selected"/>
												</xsl:if>Yes</option>
										</select>
									</td>
								</tr>
							</xsl:if>
							<tr class="datacell1">
								<td align="left" class="datacell1font">Workflow Action:
                <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
								</td>
								<td width="100%" align="left" class="datacell1font">
									<!-- begin XSL -->
									<xsl:apply-templates select="/workflowtransitionedit/workflowactionlist" mode="workflowactions">
										<xsl:with-param name="selectedaction">
											<xsl:value-of select="/workflowtransitionedit/transition/actions"/>
										</xsl:with-param>
									</xsl:apply-templates>&nbsp;
								<!-- end XSL -->
								</td>
							</tr>
							<xsl:if test="not($isagingvar='1')">
								<tr class="datacell2">
									<td align="left" class="datacell1font">Transition Role:
							<img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
									</td>
									<td width="100%" align="left" class="datacell1font">
										<xsl:for-each select="roles">
											<select name="transitionroles" size="1">
												<xsl:attribute name="selected"><xsl:value-of select="."/></xsl:attribute>
												<option value="*ALL*">
													<xsl:if test=".=&apos;*ALL*&apos;">
														<xsl:attribute name="selected"/>
													</xsl:if>
								-- All roles --
							  </option>
												<option value="*Specified*">
													<xsl:if test=".=&apos;*Specified*&apos;">
														<xsl:attribute name="selected"/>
													</xsl:if>
								-- Specified roles --
							  </option>
												<!--<xsl:apply-templates select="document($extroles)" mode="mode19"/>-->
											</select>
										</xsl:for-each>
									</td>
								</tr>
							</xsl:if>
							<xsl:if test="$isagingvar='1'">
								<input type="hidden" name="requiredapprovals" value="1"/>
								<input type="hidden" name="commentrequired" value="n"/>
								<input type="hidden" name="transitionroles" value="*ALL*"/>
								<tr class="datacell2">
									<td align="left" class="datacell1font">Aging Type:
						 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
									</td>
									<td width="100%" align="left" class="datacell1font">
										<xsl:variable name="agingtype" select="agingtype"/>
										<select name="agingtype">
											<xsl:for-each select="document(/*/agingtypesurl)/*/item">
												<option value="{value}">
													<xsl:if test="$agingtype=value">
														<xsl:attribute name="selected"/>
													</xsl:if>
													<xsl:value-of select="display"/>
												</option>
											</xsl:for-each>
										</select>
									</td>
								</tr>
								<tr class="datacell1">
									<td align="left" class="datacell1font">Aging Interval(min):
						 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
									</td>
									<td width="100%" align="left" class="datacell1font">
										<input type="text" name="aginginterval" size="6">
											<xsl:attribute name="value"><xsl:value-of select="aginginterval"/></xsl:attribute>
										</input>&nbsp;(Required for Absolute and Repeated Aging Types)
					  </td>
								</tr>
								<tr class="datacell2">
									<td align="left" class="datacell1font">System Field:
						 <img src="../sys_resources/images/invis.gif" height="1" width="100" border="0"/>
									</td>
									<td width="100%" align="left" class="datacell1font">
										<xsl:variable name="systemfield" select="systemfield"/>
										<select name="systemfield">
											<option>&nbsp;</option>
											<xsl:for-each select="document(/*/systemfieldurl)/*/item">
												<option value="{value}">
													<xsl:if test="$systemfield=value">
														<xsl:attribute name="selected"/>
													</xsl:if>
													<xsl:value-of select="display"/>
												</option>
											</xsl:for-each>
										</select>&nbsp;(Required for System Field Aging Type)
					  </td>
								</tr>
							</xsl:if>
						</xsl:for-each>
						<tr class="datacell1">
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
				<xsl:if test="not(transitionid='') and not(isaging='1')">
					<tr>
						<td class="headercell">
							<table width="100%" cellpadding="0" cellspacing="1" border="0">
								<tr>
									<td colspan="10" class="outerboxcell" align="right">
										<span class="outerboxcellfont">Transition Roles</span>
									</td>
								</tr>
								<xsl:apply-templates select="document($exttransitionroles)" mode="exttransitionroles"/>
							</table>
						</td>
					</tr>
				</xsl:if>
				<xsl:if test="not(transitionid='')">
					<tr>
						<td class="headercell">
							<table width="100%" cellpadding="0" cellspacing="1" border="0">
								<tr>
									<td colspan="10" class="outerboxcell" align="right">
										<span class="outerboxcellfont">Transition Notifications</span>
									</td>
								</tr>
								<xsl:apply-templates select="document($exttransitionnotifs)" mode="exttransitionnotifs"/>
							</table>
						</td>
					</tr>
				</xsl:if>
				<tr class="headercell">
					<td height="100%">&nbsp;</td>
					<!--   Fill down to the bottom   -->
				</tr>
			</table>
			<!--   Main View Area End   -->
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
