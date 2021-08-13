<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:template match="/">
      <xsl:apply-templates select="itemassemblydata"/>
   </xsl:template>
   <xsl:template match="itemassemblydata">
      <Node name="ItemAssembler" type="ROOT" label="Item Assembler" expanded="true" childrenurl="">
         <xsl:if test="not(mode='Site Centric')">
            <Props>
               <Prop name="sys_contentid">
                  <xsl:value-of select="contentid"/>
               </Prop>
            </Props>
            <Node name="Parent" type="Parent" label="{label}" childrenurl="{concat(itemslotsurl,'?sys_contentid=', contentid, '&amp;sys_revision=', revision, '&amp;sys_variantid=', variantid, '&amp;sys_slotname=', slotname)}" expanded="true">
               <Props>
                  <Prop name="sys_contentid">
                     <xsl:value-of select="contentid"/>
                  </Prop>
                  <Prop name="sys_revision">
                     <xsl:value-of select="revision"/>
                  </Prop>
                  <Prop name="sys_variantid">
                     <xsl:value-of select="variantid"/>
                  </Prop>
               </Props>
            </Node>
         </xsl:if>
         <Node name="SearchResults" iconkey="SearchResults" type="SystemCategory" label="Search Results" childrenurl="{concat(searchresultsurl,'?sys_contentid=', contentid, '&amp;sys_revision=', revision, '&amp;sys_variantid=', variantid, '&amp;sys_slotname=', slotname,  '&amp;sys_slotid=', slotid)}" expanded="true">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.SystemCategory@Search Results'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
         </Node>
      </Node>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cxItemAssembly.SystemCategory@Search Results">Document Assembly system category item label  &quot;Search Results&quot; in the navigation pane</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
