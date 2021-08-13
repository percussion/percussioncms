<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:variable name="systemSitePermission" select="//@SystemSitePermissions"/>
   <xsl:variable name="systemFolderPermission" select="//@SystemFolderPermissions"/>      
   <xsl:template match="/">
      <Node name="ContentExplorer" type="ROOT" label="Content Explorer" expanded="true" childrenurl="">
         <Node name="Sites" iconkey="Sites" type="SystemSite" label="Sites" childrenurl="" expanded="false">
            <xsl:attribute name="permissions"><xsl:value-of select="$systemSitePermission" /></xsl:attribute>           
            <Props>
               <Prop name="sys_contentid">2</Prop>
            </Props>
         </Node>
         <Node name="Folders" iconkey="Folders" type="SystemFolder" label="Folders" childrenurl="" expanded="false">
            <xsl:attribute name="permissions"><xsl:value-of select="$systemFolderPermission" /></xsl:attribute>                    
            <Props>
               <Prop name="sys_contentid">3</Prop>
            </Props>
         </Node>
         <Node name="Views" iconkey="Views" type="SystemCategory" label="Views" childrenurl="" expanded="true">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.SystemCategory@Views'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
            <Node name="MyContent" iconkey="MyContent" type="SystemView" label="My Content" childrenurl="../sys_cxSupport/Views.html?sys_category=1" expanded="true">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.SystemView@My Content'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
            </Node>
            <Node name="CommunityContent" iconkey="CommunityContent" type="SystemView" label="Community Content" childrenurl="../sys_cxSupport/Views.html?sys_category=2" expanded="true">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.SystemView@Community Content'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
            </Node>
            <Node name="AllContent" iconkey="AllContent" type="SystemView" label="All Content" childrenurl="../sys_cxSupport/Views.html?sys_category=3" expanded="true">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.SystemView@All Content'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
            </Node>
            <Node name="OtherContent" iconkey="OtherContent" type="SystemView" label="Other Content" childrenurl="../sys_cxSupport/Views.html?sys_category=4" expanded="true">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.SystemView@Other Content'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
            </Node>
         </Node>
         <Node name="SearchResults" iconkey="SearchResults" type="SystemCategory" helptypehint="Searches" label="Search Results" childrenurl="../sys_DisplayFormats/getSearchesForCommunity.html" expanded="true">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.SystemCategory@Searches'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
         </Node>
      </Node>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cx.SystemSite@Sites">Content Explorer top level item &quot;Sites&quot; in the navigation pane</key>
      <key name="psx.sys_cx.SystemFolder@Folders">Content Explorer top level item &quot;Folders&quot; in the navigation pane</key>
      <key name="psx.sys_cx.SystemCategory@Views">Content Explorer top level item &quot;Views&quot; in the navigation pane</key>
      <key name="psx.sys_cx.SystemView@My Content">Content Explorer system view label &quot;My Content&quot; in the navigation pane</key>
      <key name="psx.sys_cx.SystemView@Community Content">Content Explorer system view label &quot;Community Content&quot; in the navigation pane</key>
      <key name="psx.sys_cx.SystemView@All Content">Content Explorer system view label&quot;All Content&quot; in the navigation pane</key>
      <key name="psx.sys_cx.SystemView@Other Content">Content Explorer system view label&quot;Other Content&quot;in the navigation pane</key>
      <key name="psx.sys_cx.SystemCategory@Searches">Content Explorer top level item &quot;Search Results&quot; in the navigation pane</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
