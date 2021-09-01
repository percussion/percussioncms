<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxSecurity="com.percussion.security.xsl" extension-element-prefixes="psxSecurity"
                exclude-result-prefixes="psxSecurity">
    <xsl:output method="html" omit-xml-declaration="yes"/>
    <xsl:template name="getCSRFTokenName">
        <xsl:value-of select="psxSecurity:PSSecureXSLUtils.getCSRFTokenName()"/>
    </xsl:template>
    <xsl:template name="getCSRFTokenValue">
        <xsl:value-of select="psxSecurity:PSSecureXSLUtils.getCSRFTokenValue()"/>
    </xsl:template>
</xsl:stylesheet>
