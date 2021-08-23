<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
	<xsl:import href="file:rx_resources/stylesheets/rx_bannerTemplate.xsl"/>
	<xsl:include href="file:sys_cmpComponents/editchildcomponent_body.xsl"/>
	<xsl:include href="file:sys_resources/stylesheets/viewpaging.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - System Administrator</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<script src="../sys_resources/js/formValidation.js"><![CDATA[
          ]]></script>
				<script id="clientEventHandlersJS" language="javascript"><![CDATA[
         function save_onclick() {
				if(document.editchildcomponent.neworold.value=="new"){
					if(document.editchildcomponent.childcomponent[document.editchildcomponent.childcomponent.selectedIndex].value==""){
						alert("Name: field is a required field.");
						return;
					}
					else{
						document.editchildcomponent.childcomponentid.value = document.editchildcomponent.childcomponent[document.editchildcomponent.childcomponent.selectedIndex].value;
					}
				}
	         document.editchildcomponent.submit();
				return true;          
         }
         function cancelFunc() {
           document.location.href=document.editchildcomponent.doccancelurl.value;
         }
         ]]></script>
			</head>
			<body class="backgroundcolor" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
				<table width="100%" cellpadding="0" cellspacing="1" border="0">
					<tr>
						<td align="middle" width="100%" valign="top" height="100%" class="outerboxcell">
							<!--   start main body slot   -->
							<xsl:apply-templates mode="editchildcomponent_mainbody"/>
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
