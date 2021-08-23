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
	<xsl:include href="file:sys_Variants/editvariant_body.xsl"/>
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
						if(!(reqField(document.newvariant.desc.value,"Name"))){
							return false;
						}
						if(document.newvariant.stylesheet.value!=''){
							if(!(reqFileExtinURL(document.newvariant.stylesheet.value,"Style Sheet","XSL"))){
								return false;
							}				
						}				
						if(!(reqField(document.newvariant.url.value,"URL"))){
							return false;
						}
					  document.newvariant.submit();
               }
               function cancelFunc() {
                 document.location.href=document.newvariant.doccancelurl.value + "?sys_componentname=" + document.newvariant.sys_componentname.value + "&sys_pagename=" + document.newvariant.sys_pagename.value + "&contenttypeid=" + document.newvariant.contenttypeid.value;
               }
               function delete_onclick() {
                  document.newvariant.DBActionType.value='DELETE';
                  document.newvariant.submit();
               }
               function AddSlot() {
                  if (document.newvariant.variantid.value=='')
                  {
                     alert("Please save Variant before selecting a slot.")
                     return false  
                  }  
                  else
                  {
                     return true
                  }
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
							<xsl:for-each select="document($relatedlinks)/*/component[@slotname='slt_sys_nav']">
								<xsl:copy-of select="document(url)/*/body/*"/>
							</xsl:for-each>
							<!-- end XSL -->
							<!--   end left nav slot   -->
						</td>
						<td align="middle" width="100%" valign="top" height="100%" class="outerboxcell">
							<!--   start main body slot   -->
							<xsl:apply-templates mode="editvariant_mainbody"/>
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
