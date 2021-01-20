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
	<xsl:include href="file:sys_wfLookups/statenotifslist.xsl"/>
	<xsl:include href="file:sys_wfLookups/stateroleslist.xsl"/>
	<xsl:include href="file:sys_wfLookups/transitionslist.xsl"/>
	<xsl:include href="file:sys_wfLookups/agingtransitionslist.xsl"/>
	<xsl:include href="file:sys_wfEditor/workflowstateedit_body.xsl"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="extstateroles" select="/*/URL/extstateroles"/>
	<xsl:variable name="exttransitions" select="/*/URL/exttransitions"/>
	<xsl:variable name="extagingtransitions" select="/*/URL/extagingtransitions"/>
	<xsl:variable name="bannerinclude" select="/*/bannerincludeurl"/>
	<xsl:variable name="userstatusinclude" select="/*/userstatusincludeurl"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - Workflow Administrator</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link href="../sys_resources/css/tabs.css" rel="stylesheet" type="text/css"/>
				<script language="JavaScript" src="../sys_resources/js/delconfirm.js"><![CDATA[
]]></script>
				<script language="JavaScript" src="../sys_resources/js/checkrequired.js"><![CDATA[
]]></script>
				<script language="JavaScript"><![CDATA[
rxorigin="editstate"
]]></script>
				<script language="JavaScript" src="../sys_resources/js/formValidation.js"><![CDATA[
			]]></script>
				<script id="clientEventHandlersJS" language="javascript"><![CDATA[
				function save_onclick() {
						if(!reqField(document.UpdateState.requiredname.value,"Name")) return false;
						if(!numberField(document.UpdateState.sortorder.value,"Sort Order")) return false;
						document.UpdateState.submit();
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
				<table width="100%" cellpadding="0" height="100%" cellspacing="1" border="0">
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
							<xsl:apply-templates mode="workflowstateeditbody" select="*"/>
							<!--   end main body slot   -->
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
</xsl:stylesheet>
