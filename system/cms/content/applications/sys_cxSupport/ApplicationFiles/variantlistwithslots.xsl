<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:urlencoder="java.net.URLEncoder"
                exclude-result-prefixes="urlencoder">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates select="/*" mode="copy"/>
	</xsl:template>
	<xsl:template match="ActionList" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="*" mode="copy">
            <xsl:sort select="@label"/>
         </xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="Action" mode="copy">
		<xsl:copy>
			<!--Activeassembly pages goto actionsets first and then gets redirected to activeassembly page. If parameters exist on variant assembly url then url need to be encoded. It needs to be encoded twice as it passes through showDocument javascript function which decodes it once.
			-->
			<xsl:copy-of select="@*[not(name()='assemblyurl')]"/>
			<xsl:attribute name="url"><xsl:value-of select="concat(@url,'&amp;sys_assemblyurl=',urlencoder:encode(@assemblyurl))"/></xsl:attribute>
			<xsl:apply-templates select="*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="Props" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:for-each select="*">
			<Prop>
				<xsl:attribute name="propid">0</xsl:attribute>
				<xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>
				<xsl:value-of select="."/>
			</Prop>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="Params" mode="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:for-each select="*">
			<Param>
				<xsl:attribute name="paramid">0</xsl:attribute>
				<xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>
				<xsl:value-of select="."/>
			</Param>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
