<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
	<!-- main template -->
<xsl:output method="xml"/>
<xsl:template match="/">
   <ActionList>
      <xsl:apply-templates select="*//ActionLinkList" mode="copy1"/>
   </ActionList>
</xsl:template>
	<!-- take away the Label in the UI set -->
	<xsl:template match="ActionLinkList" mode="copy1">
		<xsl:apply-templates select="*" mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy"/>
	<xsl:template match="*|@*" mode="copyParams">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copyParams"/>
			<xsl:apply-templates mode="copyParams"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="ActionLink[@isTransition='yes']" mode="copy">
		<Action>
			<xsl:attribute name="actionid"><xsl:value-of select="concat('transition', Param[@name='sys_transitionid'])"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="DisplayLabel"/></xsl:attribute>
			<xsl:attribute name="url">test.html</xsl:attribute>
			<xsl:apply-templates select="Param" mode="copyParams"/>
		</Action>
	</xsl:template>
</xsl:stylesheet>
