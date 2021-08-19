<?xml version='1.0' encoding='UTF-8'?>
<!--
Style sheet to remove the visibilty context entries for community after moving the community visibility into ACL security.
-->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="com.percussion.i18n"
                extension-element-prefixes="psxi18n" exclude-result-prefixes="psxi18n">
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:template match="/">
      <xsl:apply-templates select="/*" mode="copy"/>
   </xsl:template>
   <xsl:template match="*" mode="copy">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <!-- Remove any visibility context entry for community since this is already taken care by the ACL of the action object -->
   <xsl:template match="Action[PSXVisibilityContextEntry[@propName='2']]" mode="copy" priority="10"/>
</xsl:stylesheet>
