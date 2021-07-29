<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_bannerTemplate.xsl"/>
	<xsl:import href="file:rx_resources/stylesheets/rx_bannerTemplate.xsl"/>
	<xsl:include href="file:sys_AutoTranslation/editconfiguration_body.xsl"/>
	<xsl:include href="file:sys_resources/stylesheets/viewpaging.xsl"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:variable name="configparamsdoc" select="document(/*/configparamsurl)"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - System Administrator</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<script src="../sys_resources/js/formValidation.js"/>
				<script src="../sys_AutoTranslation/configinit.js"/>
				<script language="javascript" ><![CDATA[
               function locale(id, name)
               {
                   this.id = id;
                   this.name = name;
               }

               function workflow(id, name)
               {
                   this.id = id;
                   this.name = name;
               }

               function contenttype(id, name, workflows, locales)
               {
                   this.id = id;
                   this.name = name;
                   this.workflows = workflows;
                   this.locales= locales;
               }

               function community(id, name, contenttypes)
               {
                   this.id = id;
                   this.name = name;
                   this.contenttypes = contenttypes;
               }
               ]]></script>
				<!-- begin XSL -->
				<xsl:element name="script">
					<xsl:attribute name="language">javascript</xsl:attribute>
					<xsl:text>communities = new Array(</xsl:text>
					<xsl:for-each select="$configparamsdoc//Community">
						<xsl:text>new community(</xsl:text>
						<xsl:value-of select="@communityid"/>
						<xsl:text>, &quot;</xsl:text>
						<xsl:value-of select="@communityname"/>
						<xsl:text>&quot;, new Array(</xsl:text>
						<xsl:for-each select="ContentTypes/ContentType">
							<xsl:text>new contenttype(</xsl:text>
							<xsl:value-of select="@contenttypeid"/>
							<xsl:text>,&quot;</xsl:text>
							<xsl:value-of select="@contenttypename"/>
							<xsl:text>&quot;, new Array(</xsl:text>
							<xsl:for-each select="Workflows/Workflow">
								<xsl:text>new workflow(</xsl:text>
								<xsl:value-of select="@workflowid"/>
								<xsl:text>,&quot;</xsl:text>
								<xsl:value-of select="@workflowname"/>
								<xsl:text>&quot; )</xsl:text>
								<xsl:if test="not(position() = last())">
									<xsl:text>,</xsl:text>
								</xsl:if>
							</xsl:for-each>)
							<xsl:text>, new Array(</xsl:text>
							<xsl:for-each select="Locales/Locale">
								<xsl:text>new locale(&quot;</xsl:text>
								<xsl:value-of select="@locale"/>
								<xsl:text>&quot;,&quot;</xsl:text>
								<xsl:value-of select="@localename"/>
								<xsl:text>&quot; )</xsl:text>
								<xsl:if test="not(position() = last())">
									<xsl:text>,</xsl:text>
								</xsl:if>
							</xsl:for-each>))
								<xsl:if test="not(position() = last())">
								<xsl:text>,</xsl:text>
							</xsl:if>
						</xsl:for-each>))
                     <xsl:if test="not(position() = last())">
							<xsl:text>,</xsl:text>
						</xsl:if>
					</xsl:for-each>
					<xsl:text>);</xsl:text>
				</xsl:element>
				<!-- end XSL -->
			<script language="javascript">
	         function cancelFunc() {
	           document.location.href=document.editconfiguration.doccancelurl.value+"?	sys_componentname="+document.editconfiguration.sys_componentname.value+"&amp;sys_pagename="+document.editconfiguration.sys_pagename.value;
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
							<xsl:apply-templates mode="editconfiguration_mainbody"/>
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
