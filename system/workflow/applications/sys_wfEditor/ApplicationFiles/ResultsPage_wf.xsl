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
		<!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<!-- begin XSL -->
	<xsl:variable name="rxorigin" select="/*/@rxorigin"/>
	<xsl:variable name="rximagepath" select="'../rx_resources/images/en-us/'"/>
	<xsl:variable name="goCode">
		<xsl:choose>
			<xsl:when test="$rxorigin='newwf'">newwf</xsl:when>
			<xsl:when test="$rxorigin='editwf'">editwf</xsl:when>
			<xsl:when test="$rxorigin='newstate'">newstate</xsl:when>
			<xsl:when test="$rxorigin='editstate'">editstate</xsl:when>
			<xsl:when test="$rxorigin='newtrans'">newtrans</xsl:when>
			<xsl:when test="$rxorigin='edittrans'">edittrans</xsl:when>
			<xsl:when test="$rxorigin='edittransnotif'">edittransnotif</xsl:when>
			<xsl:when test="$rxorigin='newrole'">newrole</xsl:when>
			<xsl:when test="$rxorigin='editrole'">editrole</xsl:when>
			<xsl:when test="$rxorigin='newstaterole'">newstaterole</xsl:when>
			<xsl:when test="$rxorigin='editstaterole'">editstaterole</xsl:when>
			<xsl:when test="$rxorigin='newnotif'">newnotif</xsl:when>
			<xsl:when test="$rxorigin='editnotif'">editnotif</xsl:when>
			<xsl:when test="$rxorigin='edittransrole'">edittransrole</xsl:when>
			<xsl:otherwise>wfhome</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<!-- end XSL -->
	<xsl:variable name="this" select="/"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.0"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<title>Processing Request</title>
				<meta Name="Keywords" CONTENT="{concat(' rxorigin:', $rxorigin, ' goCode:', $goCode)}"/>
				<xsl:if test="not($goCode='both')">
					<xsl:variable name="absaddr">
						<xsl:choose>
							<xsl:when test="$goCode='newwf'">
								<xsl:value-of select="/*/abs/newwf"/>
							</xsl:when>
							<xsl:when test="$goCode='editwf'">
								<xsl:value-of select="/*/abs/editwf"/>
							</xsl:when>
							<xsl:when test="$goCode='newstate'">
								<xsl:value-of select="/*/abs/newstate"/>
							</xsl:when>
							<xsl:when test="$goCode='editstate'">
								<xsl:value-of select="/*/abs/editstate"/>
							</xsl:when>
							<xsl:when test="$goCode='newtrans'">
								<xsl:value-of select="/*/abs/newtrans"/>
							</xsl:when>
							<xsl:when test="$goCode='edittrans'">
								<xsl:value-of select="/*/abs/edittrans"/>
							</xsl:when>
							<xsl:when test="$goCode='edittransnotif'">
								<xsl:value-of select="/*/abs/edittransnotif"/>
							</xsl:when>
							<xsl:when test="$goCode='edittransrole'">
								<xsl:value-of select="/*/abs/edittransnotif"/>
							</xsl:when>
							<xsl:when test="$goCode='newrole'">
								<xsl:value-of select="/*/abs/newrole"/>
							</xsl:when>
							<xsl:when test="$goCode='editrole'">
								<xsl:value-of select="/*/abs/editrole"/>
							</xsl:when>
							<xsl:when test="$goCode='newstaterole'">
								<xsl:value-of select="/*/abs/newstaterole"/>
							</xsl:when>
							<xsl:when test="$goCode='editstaterole'">
								<xsl:value-of select="/*/abs/editstaterole"/>
							</xsl:when>
							<xsl:when test="$goCode='newnotif'">
								<xsl:value-of select="/*/abs/newnotif"/>
							</xsl:when>
							<xsl:when test="$goCode='editnotif'">
								<xsl:value-of select="/*/abs/editnotif"/>
							</xsl:when>
							<xsl:when test="$goCode='wfhome'">
								<xsl:value-of select="/*/abs/wfhome"/>
							</xsl:when>
						</xsl:choose>
					</xsl:variable>
					<meta>
						<xsl:attribute name="HTTP-EQUIV">refresh</xsl:attribute>
						<xsl:attribute name="CONTENT">1; URL=<xsl:value-of select="$absaddr"/></xsl:attribute>
					</meta>
					<script language="javascript">
					function bounce()
					{
					   window.location.href = "<xsl:value-of select="$absaddr"/>";
					}
					
					
            			</script>
				</xsl:if>
				<!-- end XSL -->
			</head>
			<body class="backgroundcolor" topmargin="0" leftmargin="0" marginheight="0" marginwidth="0" onload="bounce()">
				<!--   psx-docalias="bannerinclude" psx-docref="psx-bannerincludeurl"   -->
				<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td height="75">
							<table width="100%" height="75" cellpadding="0" cellspacing="0" border="0">
								<tr class="bannerbackground">
									<td width="315" valign="top" align="left">
										<img src="{concat($rximagepath,'banner_longlogo.jpg')}" width="640" height="75" border="0" alt="Rhythmyx Content Manager" title="Rhythmyx Content Manager"/>
									</td>
									<td height="75" align="left" class="tabs" width="100%">
										<table width="100%" border="0" cellspacing="0" cellpadding="0" height="75" background="{concat($rximagepath,'banner_bg_noline.gif')}">
											<tr>
												<td align="left" valign="bottom">
													<img src="../sys_resources/images/spacer.gif"/>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td height="1" class="backgroundcolor">
							<img src="../sys_resources/images/invis.gif" width="1" height="1" border="0" alt=""/>
						</td>
					</tr>
					<tr class="outerboxcell">
						<td height="100%">
							<table width="100%" cellpadding="0" cellspacing="20" border="0">
								<tr class="outerboxcell">
									<td class="outerboxcellfont" align="center" valign="middle">
                              Your request is being processed. Please wait a moment...
                           </td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
				<input type="hidden" name="xedit" value="0"/>
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
