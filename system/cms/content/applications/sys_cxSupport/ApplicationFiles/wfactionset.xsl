<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:output method="xml"/>
	<!-- url to use to get the action list for each content item -->
	<xsl:variable name="url" select="//@url"/>
	<!-- main template -->
	<xsl:template match="/">
		<ActionList>
			<xsl:variable name="actionlists">
				<xsl:for-each select="ActionSet/item">
					<xsl:copy-of select="document(concat($url, '&amp;sys_contentid=', sys_contentid, '&amp;sys_revision=', sys_revision))/*"/>
				</xsl:for-each>
			</xsl:variable>
			<!-- number of items in the request -->
			<xsl:variable name="checkcount">
				<xsl:value-of select="count($actionlists/ActionList)"/>
			</xsl:variable>
			<xsl:apply-templates select="$actionlists/ActionList[position()=1]">
				<xsl:with-param name="actionlists" select="$actionlists"/>
				<xsl:with-param name="checkcount" select="$checkcount"/>
			</xsl:apply-templates>
		</ActionList>
	</xsl:template>
	<xsl:template match="ActionList">
		<xsl:param name="actionlists"/>
		<xsl:param name="checkcount"/>
		<xsl:for-each select="Action">
			<xsl:variable name="name" select="Params/Param[@name='WFAction']"/>
			<!-- for an action to be common to all items its occurences must be equal to the number of items in the request -->
			<xsl:if test="count($actionlists/ActionList/Action[Params/Param[@name='WFAction']=$name])=$checkcount">
				<xsl:choose>
					<xsl:when test="$checkcount=1">
						<xsl:copy>
						<xsl:copy-of select="@*"/>
							<xsl:attribute name="handler"><xsl:choose><xsl:when test="Params/Param[@name='showAdhoc']='yes'">SERVER</xsl:when><xsl:otherwise>SERVER</xsl:otherwise></xsl:choose></xsl:attribute>
						<xsl:copy-of select="*"/>
						</xsl:copy>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="not($actionlists/ActionList/Action[Params/Param[@name='WFAction']=$name]/Params/Param[@name='showAdhoc']='yes')">
							<xsl:copy-of select="."/>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
