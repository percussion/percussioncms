<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
	<xsl:import href="file:rx_resources/stylesheets/rx_bannerTemplate.xsl"/>
	<xsl:include href="file:sys_pubEditions/neweditionbody.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - Publishing Administrator</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<script type="text/javaScript" src="../sys_resources/js/formValidation.js"><![CDATA[
          ]]></script>
				<script type="text/javascript" id="clientEventHandlersJS" language="javascript"><![CDATA[
				function cancelFunc() {
				  document.location.href=document.newedition.doccancelurl.value;
				}
				function previewContentList(s) {
					currWin = window.open('../sys_pubEditions/bounceToContentList.html?bounceto=' + escape(s));
				}
				function save_onclick() {
						if(document.newedition.destinationsitelist.selectedIndex==0) document.newedition.destinationsitelist[document.newedition.destinationsitelist.selectedIndex].value="";
						if(document.newedition.sourcesitelist.selectedIndex==0) document.newedition.sourcesitelist[document.newedition.sourcesitelist.selectedIndex].value="";
						if(!(reqField(document.newedition.editiontitle.value,"Edition Name"))){
							return false;
						}
						if(document.newedition.destinationsitelist[document.newedition.destinationsitelist.selectedIndex].value==""){
							alert("Destination site: field is a required field");
							return false;
						}
						if(!(numberField(document.newedition.pubstatusid.value,"Recovery Publication(id)"))){
							return false;
						}
						if(document.newedition.editiontypelist[document.newedition.editiontypelist.selectedIndex].value=="3" && document.newedition.pubstatusid.value==""){
							alert("Recovery Publication(id): field is a required field if Edition Type field is Recovery.");
							return false;
						}    
						if(document.newedition.editiontypelist[document.newedition.editiontypelist.selectedIndex].value=="4" && document.newedition.sourcesitelist[document.newedition.sourcesitelist.selectedIndex].value=="" ){
							alert("Mirror Source Site: field is a required field if Edition Type field is Mirror.");
							return false;
						}    
					 document.newedition.submit();
				}
         ]]></script>
			</head>
			<body class="backgroundcolor" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
				<!--   BEGIN Banner and Login Details   -->
				<xsl:call-template name="bannerAndUserStatus"/>
				<!--   END Banner and Login Details   -->
				<table width="100%" cellpadding="0" cellspacing="1" border="0">
					<tr>
						<td align="middle" valign="top" width="150" height="100%" class="outerboxcell">
							<!--   start left nav slot   -->
							<!-- begin XSL -->
							<xsl:for-each select="document($relatedlinks)/*/component[@slotname='slt_pub_nav']">
								<xsl:copy-of select="document(url)/*/body/*"/>
							</xsl:for-each>
							<!-- end XSL -->
							<!--   end left nav slot   -->
						</td>
						<td align="middle" width="100%" valign="top" height="100%" class="outerboxcell">
							<!--   start main body slot   -->
							<xsl:apply-templates mode="newedition_mainbody">
              </xsl:apply-templates>
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
