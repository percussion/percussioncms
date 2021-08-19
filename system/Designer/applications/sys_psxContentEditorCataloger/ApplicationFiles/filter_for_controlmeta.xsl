<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:psxctl="urn:percussion.com/control"
   xmlns="http://www.w3.org/1999/xhtml">

<xsl:template match="/">
<Controls>
	<xsl:apply-templates select="*"/>
</Controls>
</xsl:template>

<xsl:template match="psxctl:ControlMeta">
	<xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="*">
	<xsl:apply-templates select="*"/>
</xsl:template>

</xsl:stylesheet>
