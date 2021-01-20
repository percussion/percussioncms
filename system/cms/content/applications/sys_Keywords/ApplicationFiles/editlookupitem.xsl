<?xml version="1.0" encoding="UTF-8"?>
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
	<xsl:import href="file:rx_resources/stylesheets/rx_bannerTemplate.xsl"/>
	<xsl:include href="file:sys_Keywords/editlookupitem_body.xsl"/>
	<xsl:include href="file:sys_resources/stylesheets/viewpaging.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:variable name="lookupdoc" select="document(/*/lookuplisturl)"/>
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
				<xsl:element name="script">
					<xsl:attribute name="type">text/javascript</xsl:attribute>
					<xsl:attribute name="language">javascript</xsl:attribute>
					<xsl:text>var labelslist = new Array(""</xsl:text>
					<xsl:variable name="lookupid" select="//lookupid"/>
					<xsl:for-each select="$lookupdoc/lookuplist/entry[not(@lookupid = $lookupid)]">
						<xsl:text>,"</xsl:text>
						<xsl:value-of select="@lookupdisplay"/>
						<xsl:text>"</xsl:text>
					</xsl:for-each>
					<xsl:text>);</xsl:text>
					<xsl:text>var valueslist = new Array(""</xsl:text>
					<xsl:for-each select="$lookupdoc/lookuplist/entry[not(@lookupid = $lookupid)]">
						<xsl:text>,"</xsl:text>
						<xsl:value-of select="@lookupvalue"/>
						<xsl:text>"</xsl:text>
					</xsl:for-each>
					<xsl:text>);</xsl:text>
				</xsl:element>
				<script language="javascript"><![CDATA[
         			function save_onclick()
         			{
					if(!(reqField(document.updatelookupitem.lookupname.value,"Choice Label"))){
						return false;
					}
					for(i=0;i<labelslist.length;i++)
					{
						if(document.updatelookupitem.lookupname.value==labelslist[i] )
						{
							alert("A choice already exists with the same label, Please select a different label for the choice.");
							return;
						}
					}	         
					if(!(reqField(document.updatelookupitem.lookupvalue.value,"Choice Value"))){
						return false;
					}	         
					for(i=0;i<valueslist.length;i++)
					{
						if(document.updatelookupitem.lookupvalue.value==valueslist[i] )
						{
							alert("A choice already exists with the same value, Please select a different value for the choice.");
							return;
						}
					}	         
					if(!(numberField(document.updatelookupitem.lookupsequence.value,"Sort Order"))){
						return false;
					}
            			if(document.updatelookupitem.lookuptype.value == "")
                 			document.updatelookupitem.lookuptype.value = 1;
             			document.updatelookupitem.submit();
         			}
         			]]>
         function cancelFunc() {
           document.location.href=document.updatelookupitem.doccancelurl.value+"?sys_componentname="+document.updatelookupitem.sys_componentname.value+"&amp;sys_pagename="+document.updatelookupitem.sys_pagename.value;
         }
        </script>
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
							<xsl:apply-templates mode="editlookupitem_mainbody"/>
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
