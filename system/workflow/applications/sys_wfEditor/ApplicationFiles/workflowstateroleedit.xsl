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
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="extroles" select="document(/*/URL/extroles)"/>
	<xsl:variable name="extstateroles" select="document(/*/URL/extstateroles)"/>
	<xsl:variable name="bannerinclude" select="/*/bannerincludeurl"/>
	<xsl:variable name="userstatusinclude" select="/*/userstatusincludeurl"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
   <xsl:variable name="showInInBoxValues" select="document(/*/ShowInInBoxLookupUrl)/*"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - Workflow Administrator</title>
				<link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
				<link href="../sys_resources/css/tabs.css" rel="stylesheet" type="text/css"/>
				<script language="JavaScript" src="../sys_resources/js/checkrequired.js"><![CDATA[
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
							<form action="UpdateStateRole.htm" method="get" onsubmit="return checkrequired(this)">
								<!--   BEGIN Banner and Login Details   -->
								<xsl:apply-templates select="*" mode="mode17"/>
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
	<xsl:template match="*" mode="mode16">
		<xsl:for-each select=".">
			<input name="workflowid" type="hidden">
				<xsl:attribute name="value"><xsl:value-of select="workflowid"/></xsl:attribute>
			</input>
			<input name="sys_componentname" type="hidden">
				<xsl:attribute name="value"><xsl:value-of select="componentname"/></xsl:attribute>
			</input>
			<input name="stateid" type="hidden">
				<xsl:attribute name="value"><xsl:value-of select="stateid"/></xsl:attribute>
			</input>
			<input name="roleid" type="hidden">
				<xsl:attribute name="value"><xsl:value-of select="roleid"/></xsl:attribute>
			</input>
			<input name="rxorigin" type="hidden" value="editstaterole"/>
			<!-- Role Row Starts -->
			<tr class="datacell1">
				<td align="left" class="datacell1font" width="30%">Role:
          <img src="/sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				</td>
				<td width="100%" align="left" class="datacell1font">
					<select name="assignedroleid" size="1">
						<xsl:attribute name="selected"><xsl:value-of select="roleid"/></xsl:attribute>
						<xsl:for-each select="$extroles//role[not(name=$extstateroles//staterole/rolename)]">
							<option>
								<xsl:variable name="value">
									<xsl:value-of select="roleid"/>
								</xsl:variable>
								<xsl:if test="$this/workflowstateroleedit/roleid=$value">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:attribute name="value"><xsl:value-of select="roleid"/></xsl:attribute>
								<xsl:apply-templates select="name"/>
							</option>
						</xsl:for-each>
						<!-- when changing an existing role, add to the above list one more role that user selected -->
						<xsl:if test="workflowStateRoleEdit/Name != ''">
							<option value="{workflowStateRoleEdit/NameValue}" selected="true">
								<xsl:value-of select="workflowStateRoleEdit/Name"/>
							</option>
						</xsl:if>
					</select>
				</td>
			</tr>
			<!-- Role Row Ends -->

			<tr class="datacell2">
				<td align="left" class="datacell1font">Assignment:
			 <img src="/sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				</td>
				<td width="100%" align="left" class="datacell1font">
					<select name="assignmenttype">
						<xsl:attribute name="selected"><xsl:value-of select="assignmenttype"/></xsl:attribute>
						<xsl:variable name="assignmenttypeval">
							<xsl:value-of select="assignmenttype"/>
						</xsl:variable>
						<xsl:for-each select="document(assignmenttypeurl)/*/item[value!=4]">
                  <!--There are only three types of Assignment Types to be shown here, None(value=1), Reader(2), Assignee(3). 
                     Admin(4) Assignment type has been added to the lookup values for visibility context purpose and need to be hidden here.--> 
							<option>
								<xsl:attribute name="value"><xsl:value-of select="value"/></xsl:attribute>
								<xsl:if test="$assignmenttypeval=value">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="display"/>&nbsp;
				         </option>
						</xsl:for-each>
					</select>
				</td>
			</tr>
			<tr class="datacell1">
				<td align="left" class="datacell1font">Ad-hoc:
			 <img src="/sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				</td>
				<td width="100%" align="left" class="datacell1font">
					<select name="adhoc">
						<xsl:attribute name="selected"><xsl:value-of select="adhoc"/></xsl:attribute>
						<xsl:variable name="adhocval">
							<xsl:value-of select="adhoc"/>
						</xsl:variable>
						<xsl:for-each select="document(adhocurl)/*/item">
							<option>
								<xsl:attribute name="value"><xsl:value-of select="value"/></xsl:attribute>
								<xsl:if test="$adhocval=value">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="display"/>&nbsp;
			  </option>
						</xsl:for-each>
					</select>
				</td>
			</tr>
			<tr class="datacell2">
				<td align="left" class="datacell1font">Notify:
			 <img src="/sys_resources/images/invis.gif" height="1" width="100" border="0"/>
				</td>
				<td width="100%" align="left" class="datacell1font">
					<select name="notify">
						<xsl:attribute name="selected"><xsl:value-of select="notify"/></xsl:attribute>
						<option value="y">
							<xsl:if test="notify=&apos;y&apos;">
								<xsl:attribute name="selected"/>
							</xsl:if>Yes</option>
						<option value="n">
							<xsl:if test="notify=&apos;n&apos;">
								<xsl:attribute name="selected"/>
							</xsl:if>No</option>
					</select>
				</td>
			</tr>
			<tr class="datacell1">
			  <td align="left" class="datacell1font">Show in In-Box:
				 <img src="/sys_resources/images/invis.gif" height="1" width="100" border="0"/>
			  </td>
            <td width="100%" align="left" class="datacell1font">
               <select name="ShowInInBox">
                  <xsl:variable name="selectedValue" select="ShowInInBox"/>
                  <xsl:attribute name="selected"><xsl:value-of select="$selectedValue"/></xsl:attribute>
                  <xsl:for-each select="$showInInBoxValues/PSXEntry">
                     <option>
                        <xsl:variable name="optionValue" select="./Value"/>
                        <xsl:attribute name="value"><xsl:value-of select="$optionValue"/></xsl:attribute>
                        <xsl:if test="$selectedValue=$optionValue">
                           <xsl:attribute name="selected"/>
                        </xsl:if>
                        <xsl:value-of select="./PSXDisplayText"/>
                     </option>
                  </xsl:for-each>
               </select>
            </td>
			</tr>
			<tr class="datacell1">
				<td colspan="2" align="left" class="datacell1font">
					<input type="hidden" class="nav_body" name="DBActionType" value="UPDATE"/>
					<input type="button" class="nav_body" name="updatebutton" value="Save" onclick="javascript:submit();"/>&nbsp;
					<input type="button" value="Cancel" name="cancel" language="javascript" onclick="cancelFunc();"/>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*" mode="mode17">
		<xsl:variable name="stateinfo" select="document(/*/stateinfourl)/*"/>
		<xsl:for-each select=".">
			<table width="100%" height="100%" cellpadding="0" cellspacing="1" border="0">
				<tr>
					<td id="XSpLit" colspan="2"/>
				</tr>
				<tr>
					<td class="outerboxcell" align="right" valign="top" colspan="2">
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
								<xsl:when test="workflowStateRoleEdit/Name=''"> New Assigned Role</xsl:when>
								<xsl:otherwise>
									<xsl:apply-templates select="workflowStateRoleEdit/Name"/>
								</xsl:otherwise>
							</xsl:choose>
						</span>
					</td>
				</tr>
				<tr>
					<td width="100%" class="headercell" colspan="2">&nbsp;
             </td>
				</tr>
				<xsl:apply-templates select="." mode="mode16"/>
				<tr class="headercell">
					<td height="100%" colspan="2">&nbsp;</td>
					<!--   Fill down to the bottom   -->
				</tr>
			</table>
			<!--   Main View Area End   -->
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
