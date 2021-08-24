<?xml version="1.0" encoding="UTF-8"?>
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
	<xsl:output method="html" omit-xml-declaration="yes"/>
	<xsl:include href="file:sys_wfLookups/stateslist.xsl"/>
	<xsl:include href="file:sys_wfLookups/roleslist.xsl"/>
	<xsl:include href="file:sys_wfLookups/statenotifslist.xsl"/>
	<xsl:include href="file:sys_wfEditor/workflowedit_body.xsl"/>
	<xsl:variable name="relatedlinks" select="/*/relatedlinks"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="extroles" select="/*/URL/extroles"/>
	<xsl:variable name="extstates" select="/*/URL/extstate"/>
	<xsl:variable name="extstatenotifs" select="/*/URL/extnotifs"/>

	<xsl:template match="/">
		<html>
			<head>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>Rhythmyx - Workflow Administrator</title>
				<link rel="stylesheet" type="text/css" href="/sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="/rx_resources/css/templates.css"/>
				<link href="/sys_resources/css/tabs.css" rel="stylesheet" type="text/css"/>
				<script src="/sys_resources/js/delconfirm.js"><![CDATA[
					]]></script>
				<script src="/sys_resources/js/checkrequired.js"><![CDATA[
					]]></script>
				<script><![CDATA[
					rxorigin="editview"
					]]></script>
				<script src="/sys_resources/js/formValidation.js"><![CDATA[
					]]></script>
				<script id="clientEventHandlersJS"><![CDATA[
					function save_onclick() {
						if(!reqField(document.UpdateWorkflow.requiredname.value,"Name")) return false;
						document.UpdateWorkflow.submit();
					}
					]]></script>
				<script>

					function cancelFunc()
					{
					document.location.href = '<xsl:value-of select="//workflowlink"/>';
					}

				</script>
			</head>
			<body class="backgroundcolour" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
				<!--   BEGIN Banner and Login Details   -->
				<xsl:call-template name="bannerAndUserStatus"/>
				<!--   END Banner and Login Details   -->
				<table width="100%" cellpadding="0" cellspacing="1" height="100%" border="0">
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
							<xsl:apply-templates mode="workfloweditbody"/>
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
