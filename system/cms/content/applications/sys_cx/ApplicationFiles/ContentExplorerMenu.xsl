<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:template match="/">
      <MenuBar>
         <Action label="Content" name="Content" type="MENU" sortrank="1" url="" handler="CLIENT">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@Content'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.Content@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
             <Action label="New" name="Create_New" type="MENU" sortrank="1" handler="SERVER" url="../sys_cx/newmenu.html">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@New'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.New@N'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
		<Params>
  		      <Param name="sys_uicontext">$$sys_uicontext</Param>
		</Params>
            </Action>
            <Action label="Search..." name="Search" type="MENUITEM" sortrank="2" handler="CLIENT" url="CLIENT">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@Search...'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control S</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.Search...@S'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
            <Action label="CONTEXT" name="CONTEXT" type="CONTEXTMENU" sortrank="3" handler="SERVER" url=""/>
         </Action>
         <Action label="View" name="View" type="MENU" sortrank="2" handler="CLIENT" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@View'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.View@V'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            <Action label="Refresh" name="Refresh" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@Refresh'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control R</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.Refresh@R'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
            <Action label="Options..." name="Options" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@Options...'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control O</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.Options...@O'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
         </Action>
         <Action label="Help" name="Help" type="MENU" sortrank="3" handler="SERVER" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@Help'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.Help@H'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            <Action label="Content Explorer" name="topMenuHelp" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@Content Explorer'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">pressed F1</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.Content Explorer@X'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
            <Action label="About" name="About" type="MENUITEM" sortrank="2" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@About...'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control B</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.About...@B'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
         </Action>
      </MenuBar>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cx.menu@Content">Main menu item label &quot;Content&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.Content@C">Main menu item label &quot;Content&quot;</key>
      <key name="psx.sys_cx.menu@New">Main menu item label &quot;New&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.New@N">Main menu item mnemonic for label &quot;New&quot;</key>
      <key name="psx.sys_cx.menu@Search...">Main menu item label &quot;Search...&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.Search...@S">Main menu item mnemonic for label &quot;Search...&quot;</key>
      <key name="psx.sys_cx.menu@View">Main menu item label &quot;View&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.View@V">Main menu item mnemonic for label &quot;View&quot;</key>
      <key name="psx.sys_cx.menu@Refresh">Main menu item label &quot;Refresh&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.Refresh@R">Main menu item mnemonic for label &quot;Refresh&quot;</key>
      <key name="psx.sys_cx.menu@Options...">Main menu item label &quot;Options...&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.Options...@O">Main menu item mnemonic for label &quot;Options...&quot;</key>
      <key name="psx.sys_cx.menu@Site Explorer">Main menu item label &quot;Site Explorer&quot; that launches Site Explorer</key>
      <key name="psx.sys_cx.menu.mnemonic.Site Explorer@X">Main menu item mnemonic for label &quot;Site Explorer&quot; that launches Site Explorer</key>
      <key name="psx.sys_cx.menu@Help">Main menu item label &quot;Help&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.Help@H">Main menu item mnemonic for label &quot;Help&quot;</key>
      <key name="psx.sys_cx.menu@Content Explorer">Main menu item label for help topic &quot;Content Explorer&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.Content Explorer@X">Main menu item mnemonic for label for help topic &quot;Content Explorer&quot;</key>
      <key name="psx.sys_cx.menu@About...">Main menu item label &quot;About...&quot;</key>
      <key name="psx.sys_cx.menu.mnemonic.About...@B">Main menu item mnemonic for label &quot;About...&quot;</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
