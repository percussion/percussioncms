<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns="http://www.springframework.org/schema/beans" xmlns:foo="http://www.springframework.org/schema/beans" exclude-result-prefixes="foo" >
	<xml:output method="xml" encoding="UTF-8"/>
	<!-- main template -->
	<xsl:template match="/">
		<xsl:apply-templates select="." mode="copy"/>
	</xsl:template>
	<!-- copy any attribute or template -->
	<xsl:template match="@*|*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
			<xsl:apply-templates mode="copy"/>
		</xsl:copy>
	</xsl:template>
	<!-- add supportedContentTypes entry to sys_metadataDeliveryHandler Bean -->
	<xsl:template match="foo:bean[@id='sys_metadataDeliveryHandler']" mode="copy">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="copy"/>
                <xsl:if test="not(foo:property[@name='supportedContentTypes'])"><!-- if it already exists don't add it -->
                <xsl:element name="property">
                    <xsl:attribute name="name">supportedContentTypes</xsl:attribute>
                    <xsl:element name="list">
                        <xsl:element name="value">percPage</xsl:element>
                    </xsl:element>
                </xsl:element>
                </xsl:if>
            <xsl:apply-templates mode="copy"/>
        </xsl:copy>
	</xsl:template>
</xsl:stylesheet>
