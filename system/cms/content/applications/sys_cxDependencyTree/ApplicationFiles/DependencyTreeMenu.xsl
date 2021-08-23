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
            <Action label="CONTEXT" name="CONTEXT" type="CONTEXTMENU" sortrank="1" handler="SERVER" url=""/>
         </Action>
         <Action label="View" name="View" type="MENU" handler="SERVER" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu@View'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu.mnemonic.View@V'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            <Action label="Refresh" name="Refresh" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu@Refresh'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control R</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu.mnemonic.Refresh@R'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
            <Action label="Options..." name="Options" type="MENUITEM" handler="CLIENT" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu@Options...'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control O</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu.mnemonic.Options...@O'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
         </Action>
         <Action label="Help" name="Help" type="MENU" handler="SERVER" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu@Help'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu.mnemonic.Help@H'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            <Action label="Impact Analysis" name="topMenuHelp" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu@Impact Analysis'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">pressed F1</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu.mnemonic.Impact Analysis@A'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
            <Action label="About..." name="About" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu@About...'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control B</Prop>
		<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxDependencyTree.menu.mnemonic.About...@B'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
	    </Props>
            </Action>
         </Action>
      </MenuBar>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cxDependencyTree.menu@View">Main menu item label &quot;View&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu.mnemonic.View@V">Main menu item mnemonic for label &quot;View&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu@Refresh">Main menu item label &quot;Refresh&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu.mnemonic.Refresh@R">Main menu item mnemonic for label &quot;Refresh&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu@Options...">Main menu item label &quot;Option   s...&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu.mnemonic.Options...@O">Main menu item mnemonic for label &quot;Option   s...&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu@Help">Main menu item label &quot;Help&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu.mnemonic.Help@H">Main menu item mnemonic for label &quot;Help&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu@Impact Analysis">Main menu item label for help topic &quot;Impact Analysis&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu.mnemonic.Impact Analysis@A">Main menu item mnemonic for label for help topic &quot;Impact Analysis&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu@About...">Main menu item label &quot;About...&quot;</key>
      <key name="psx.sys_cxDependencyTree.menu.mnemonic.About...@B">Main menu item mnemonic for label &quot;About...&quot;</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
