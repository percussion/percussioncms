<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:template match="/">
      <xsl:apply-templates select="searchresultsdata"/>
   </xsl:template>
   <xsl:template match="searchresultsdata">
      <Node>
         <Node name="NewSearch" type="NewSearch" label="New Search" childrenurl="" expanded="false">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.NewSearch@New Search'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
            <Props>
               <Prop name="hidden">true</Prop>
               <Prop name="sys_displayformat">7</Prop>
               <Prop name="sys_search">6</Prop>
               <Prop name="sys_slotid">
                  <xsl:value-of select="slotid"/>
               </Prop>
               <Prop name="sys_contentid">
                  <xsl:value-of select="contentid"/>
               </Prop>
               <Prop name="sys_revision">
                  <xsl:value-of select="revision"/>
               </Prop>
            </Props>
         </Node>
      </Node>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cxItemAssembly.NewSearch@New Search">Document Assembly new search item label  &quot;New Search&quot; in the navigation pane</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
