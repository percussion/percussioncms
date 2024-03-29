<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates select="/*" mode="copy"/>
	</xsl:template>
	<xsl:template match="*" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="View" mode="copy">
		<xsl:if test="@name!=''">
			<Node>
				<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
				<xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute>
				<xsl:attribute name="label"><xsl:value-of select="@label"/></xsl:attribute>
				<xsl:attribute name="childrenurl"><xsl:value-of select="@childrenurl"/></xsl:attribute>
				<xsl:attribute name="expanded">false</xsl:attribute>
				<xsl:if test="@name='Inbox' or @name='Outbox'">
					<xsl:attribute name="iconkey"><xsl:value-of select="@name"/></xsl:attribute>
				</xsl:if>
				<Props>
					<Prop name="sys_displayformat">
						<xsl:value-of select="@sys_displayformat"/>
					</Prop>
					<Prop name="sys_search">
						<xsl:value-of select="@sys_search"/>
					</Prop>
				</Props>
			</Node>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
