<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" exclude-result-prefixes="psxi18n"
                extension-element-prefixes="psxi18n">

	<xsl:template name="rx-context">
		<xsl:param name="attribute-name"/>
		<xsl:param name="attribute-value"/>
		
		<xsl:attribute name="{$attribute-name}">
			<xsl:value-of select="$attribute-value"/>
		</xsl:attribute>
	</xsl:template>

</xsl:stylesheet>
