<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:template match="/">
      <Node>
         <Node name="EmptySearch" iconkey="EmptySearch" type="EmptySearch" label="EmptySearch" childrenurl="" expanded="false">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_DisplayFomats.NewSearch@New Search'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
            <Props>
               <Prop name="sys_displayformat">0</Prop>
               <Prop name="sys_search">0</Prop>
            </Props>
         </Node>
         <Node name="NewSearch" iconkey="NewSearch" type="NewSearch" label="New Search" childrenurl="" expanded="false">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_DisplayFomats.NewSearch@Search Results'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
            <Props>
               <Prop name="sys_displayformat">0</Prop>
               <!-- This is a fake id. It must be different than all other ids and must 
                  match the EMPTY_SEARCHID value in PSSearchViewCatalog.java.-->
               <Prop name="sys_search">-1</Prop>
               <Prop name="hidden">true</Prop>
            </Props>
         </Node>
         <!-- order implementer searches first and then saved searches -->
         <xsl:apply-templates select="//PSXSearch[TYPE='CustomSearch' or TYPE='StandardSearch']" mode="copy"/>
         <xsl:apply-templates select="//PSXSearch[TYPE='Search']" mode="copy"/>
      </Node>
   </xsl:template>
   <xsl:template match="PSXSearch" mode="copy" priority="10">
      <Node name="{INTERNALNAME}" label="{DISPLAYNAME}" childrenurl="{CUSTOMURL}" type="{TYPE}">
         <!-- Search maps to SavedSearch -->
         <xsl:if test="TYPE='Search'">
            <xsl:attribute name="type">SavedSearch</xsl:attribute>
         </xsl:if>
         <Props>
            <Prop name="sys_displayformat">
               <xsl:value-of select="DISPLAYFORMAT"/>
            </Prop>
            <Prop name="sys_search">
               <xsl:value-of select="PSXKey/SEARCHID"/>
            </Prop>
         </Props>
      </Node>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_DisplayFomats.NewSearch@Search Results">Content Explorer search results item label  &quot;New Search&quot; in the navigation pane</key>
      <key name="psx.sys_DisplayFomats.NewSearch@New Search">Content Explorer new search item label  &quot;New Search&quot; in the navigation pane</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
