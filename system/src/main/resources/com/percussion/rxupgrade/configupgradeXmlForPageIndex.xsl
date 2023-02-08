<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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
    <!-- add property -->
    <xsl:template match="PSXSearchConfig/Properties" mode="copy">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="copy"/>
            <xsl:apply-templates mode="copy"/>
            <xsl:if test="not(Property[@name='index_type_on_startup'])">
                <Property name="index_type_on_startup">percPage</Property>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
