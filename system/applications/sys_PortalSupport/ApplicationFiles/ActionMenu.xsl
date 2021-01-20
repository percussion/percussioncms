<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n">
	<xsl:output method="xml" omit-xml-declaration="yes"/>
	<xsl:include href="file:sys_resources/stylesheets/assemblers/sys_popmenu.xsl"/>
	<xsl:include href="file:rx_resources/stylesheets/assemblers/rx_popmenu.xsl"/>
	<xsl:include href="file:sys_resources/stylesheets/assemblers/sys_wfActions.xsl"/>
	<xsl:include href="file:sys_resources/stylesheets/assemblers/sys_cmsActions.xsl"/>
	<xsl:variable name="imagepath" select="/*/@imagepath"/>
	<xsl:variable name="itemactions" select="document(/*/@actionlisturl)/*"/>
	<xsl:variable name="sessionid" select="/*/@sessionid"/>
	<xsl:variable name="contentid" select="/*/@contentid"/>
	<xsl:variable name="folderid" select="/*/@folderid"/>
	<xsl:variable name="siteid" select="/*/@siteid"/>
	<xsl:variable name="currentrevision" select="/*/@currentrevision"/>
	<xsl:variable name="tiprevision" select="/*/@tiprevision"/>
	<xsl:variable name="omitlink" select="/*/@omitlink"/>
	<xsl:variable name="name" select="/*/@menuname"/>
	<xsl:variable name="contentvalid" select="/*/@contentvalid"/>
	<xsl:variable name="actionsetid">
	<xsl:choose>
		<xsl:when test="$name = ''"><xsl:value-of select="$contentid"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="$name"/></xsl:otherwise>
	</xsl:choose>	
	</xsl:variable>
	<xsl:template match="/">
		<xsl:apply-templates select="$itemactions" mode="mainmenu">
			<xsl:with-param name="actionsetid" select="$actionsetid"/>
			<xsl:with-param name="contentid" select="$contentid"/>
			<xsl:with-param name="siteid" select="$siteid"/>
			<xsl:with-param name="folderid" select="$folderid"/>
			<xsl:with-param name="sessionid" select="$sessionid"/>
			<xsl:with-param name="revision" select="$currentrevision"/>
			<xsl:with-param name="tiprevision" select="$tiprevision"/>
			<xsl:with-param name="contentvalid" select="$contentvalid"/>
			<xsl:with-param name="portal">yes</xsl:with-param>
		</xsl:apply-templates>
		<xsl:if test="$omitlink = ''">
			<a class="Menu" href="javascript:void(0)" onclick="return true" onmousedown="PSEnterTopItem('{$actionsetid}',event)" onmouseout="PSExitTopItem('{$actionsetid}')">
				<img src="{$imagepath}/rxactionmenu.gif" border="0" align="absmiddle" alt="Action Menu"/>
			</a>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
