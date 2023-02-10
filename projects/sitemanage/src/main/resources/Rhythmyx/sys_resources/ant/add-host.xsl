<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	
    <xsl:param name="perc.site" select="'adam'" />
    
	<xsl:template match="@* | node()">
	   <xsl:copy>
	      <xsl:apply-templates select="@* | node()" />
	   </xsl:copy>
	</xsl:template>
	
	
	<xsl:template match="Engine[@name='Catalina']" priority="100">
        <xsl:copy>
        <xsl:apply-templates select="@*" />
        <xsl:if test="not(Host[@name=$perc.site])">
        <Host name="{$perc.site}" appBase="{concat($perc.site,'apps')}" />
        </xsl:if>
        <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
	</xsl:template>
    
    
</xsl:stylesheet>
