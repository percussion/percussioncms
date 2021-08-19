<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"  exclude-result-prefixes="psxi18n" version="1.1">
	<xsl:template name="psx-global-template-dispatcher">
		<xsl:choose>
         <!--This is a dummy template and does nothing. has been added to support old style global templates.-->
         <xsl:when test="/*/sys_AssemblerInfo[@psxglobaltemplate='psx_dummy-template']">
         </xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="xsplit_root"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
