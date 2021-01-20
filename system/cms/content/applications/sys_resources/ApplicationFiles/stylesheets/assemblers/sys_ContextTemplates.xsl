<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >

	<xsl:template name="rx-context">
		<xsl:param name="attribute-name"/>
		<xsl:param name="attribute-value"/>
		
		<xsl:attribute name="{$attribute-name}">
			<xsl:value-of select="$attribute-value"/>
		</xsl:attribute>
	</xsl:template>

</xsl:stylesheet>
