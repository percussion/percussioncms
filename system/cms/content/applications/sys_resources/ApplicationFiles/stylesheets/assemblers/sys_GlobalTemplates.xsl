<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"  exclude-result-prefixes="psxi18n" version="1.1">
   <!-- This template will catch any misnamed global template or sites without local or global template. -->
	<xsl:template match="/*/sys_AssemblerInfo" mode="psx-global-template">
      <xsl:call-template name="xsplit_root"/>
   </xsl:template>
</xsl:stylesheet>
