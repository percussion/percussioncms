<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<!-- main template -->
	<xsl:output method="xml" encoding="UTF-8" />
	<xsl:template match="/">
		<TransitionListResponse>
			<xsl:apply-templates select="//BasicInfo/ActionLinkList" mode="copy1"/>
		</TransitionListResponse>
	</xsl:template>
	<!-- take away the Label in the UI set -->
	<xsl:template match="ActionLinkList" mode="copy1">
		<xsl:apply-templates select="*" mode="copy"/>
	</xsl:template>
	<xsl:template match="@*|*" mode="copy"/>
	<xsl:template match="ActionLink[@isTransition='yes']" mode="copy">
		<Transition>
			<xsl:attribute name="id"><xsl:value-of select="Param[@name='sys_transitionid']"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="DisplayLabel"/></xsl:attribute>
			<xsl:attribute name="comment">
				<xsl:choose>
					<xsl:when test="@commentRequired='yes'">true</xsl:when>
					<xsl:otherwise>false</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
		</Transition>
	</xsl:template>
</xsl:stylesheet>
