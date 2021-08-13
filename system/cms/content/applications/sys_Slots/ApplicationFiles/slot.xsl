<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns="http://www.w3.org/1999/xhtml" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
	<xsl:import href="file:rx_resources/stylesheets/rx_bannerTemplate.xsl"/>
	<xsl:include href="file:sys_Slots/slot_body.xsl"/>
	<xsl:include href="file:sys_resources/stylesheets/viewpaging.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:variable name="slotlist" select="document(//slotlisturl)"/>
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
				if(!(reqField(document.newslot.slotname.value,"Name"))){
					return false;
				}
				if(!isSlotNameUnique(document.newslot.slotid.value,
				   document.newslot.slotname.value))
				{
					window.alert("Name: The slot name must be unique.");
					return false;
				}
				document.newslot.submit();
         }
         function cancelFunc() {
           document.location.href=document.newslot.doccancelurl.value+"?sys_componentname="+document.newslot.sys_componentname.value+"&sys_pagename="+document.newslot.sys_pagename.value;
         }
         
         function isSlotNameUnique(id,name)
         {
           for(i=0; i<slotlist.length; i++)
           {
             if(name.toLowerCase() == slotlist[i].name.toLowerCase() &&
             	id != slotlist[i].id)
             	return false;
           }
           return true;
         
         }
         
         function PSSlot(id, name)
         {
           this.id = id;
           this.name = name;
         }
         
         ]]></script>
				<xsl:element name="script">
					<xsl:attribute name="language">javascript</xsl:attribute>
					<xsl:text>var slotlist = new Array(</xsl:text>
					<xsl:for-each select="$slotlist//slot">
						<xsl:text>new PSSlot(</xsl:text>
						<xsl:value-of select="slotid"/>
						<xsl:text>, &quot;</xsl:text>
						<xsl:value-of select="slotname"/>
						<xsl:text>&quot;)</xsl:text>
						<xsl:if test="not(position() = last())">
							<xsl:text>,</xsl:text>
						</xsl:if>
					</xsl:for-each>);
         </xsl:element>
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
							<xsl:apply-templates mode="slot_mainbody"/>
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
