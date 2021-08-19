<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="urn:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml">
   <xsl:variable name="rxroot" select="/"/>
   <xsl:variable name="systemp" select="document('../sys_resources/stylesheets/sys_Templates.xsl')/*"/>
   <xsl:template match="/">
      <xsl:variable name="controls">
         <!-- copy all psxctl:ControlMeta nodes from rx_Templates.xsl into controls variable -->
         <xsl:apply-templates select="*" mode="copyrx"/>
         <!-- copy only those psxctl:ControlMeta nodes from sys_Templates.xsl
			 which are NOT found in the rx_Templates.xsl into controls variable-->
         <xsl:apply-templates select="$systemp/*" mode="copysys"/>
      </xsl:variable>
      <!-- copy and sort a merged set of psxctl:ControlMeta nodes into the final XML document -->
      <Controls>
         <xsl:apply-templates select="$controls/*" mode="copysorted">
            <xsl:sort select="@name"/>
         </xsl:apply-templates>
      </Controls>
   </xsl:template>
   <!-- copy all psxctl:ControlMeta nodes from rx_Templates.xsl into controls variable -->
   <xsl:template match="*" mode="copyrx">
      <xsl:apply-templates select="*" mode="copyrx"/>
   </xsl:template>
   <xsl:template match="psxctl:ControlMeta" mode="copyrx">
      <xsl:copy-of select="."/>
   </xsl:template>
   <!-- copy only those psxctl:ControlMeta nodes from sys_Templates.xsl
	 which are NOT found in the rx_Templates.xsl into controls variable-->
   <xsl:template match="*" mode="copysys">
      <xsl:apply-templates select="*" mode="copysys"/>
   </xsl:template>
   <xsl:template match="psxctl:ControlMeta" mode="copysys">
      <xsl:variable name="sys_ctrlname" select="@name"/>
      <xsl:variable name="rxctrl" select="$rxroot//psxctl:ControlMeta[@name=$sys_ctrlname]"/>
      <xsl:choose>
         <xsl:when test="$rxctrl">
            <!-- this control is overridden by rx_Templates, skip it -->
         </xsl:when>
         <xsl:otherwise>
            <xsl:copy-of select="."/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <!-- copy and alpha sort a merged collection of
		psxctl:ControlMeta nodes into the final XML document -->
   <xsl:template match="*" mode="copysorted">
      <xsl:apply-templates select="*" mode="copysorted"/>
   </xsl:template>
   <xsl:template match="psxctl:ControlMeta" mode="copysorted">
      <xsl:copy-of select="."/>
   </xsl:template>
</xsl:stylesheet>
