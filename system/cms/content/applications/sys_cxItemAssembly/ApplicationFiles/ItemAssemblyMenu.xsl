<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:template match="/">
      <MenuBar>
         <Action label="Content" name="Content" type="MENU" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.menu@Content'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey">C</Prop>
	    </Props>
            <Action label="CONTEXT" name="CONTEXT" type="CONTEXTMENU" handler="SERVER" url=""/>
         </Action>
         <Action label="View" name="View" type="MENU" handler="SERVER" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.menu@View'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey">V</Prop>
	    </Props>
            <Action label="Refresh" name="Refresh" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.menu@Refresh'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control R</Prop>
		<Prop name="MnemonicKey">R</Prop>
	    </Props>            </Action>
            <Action label="Options..." name="Options" type="MENUITEM" handler="CLIENT" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.menu@Options...'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control O</Prop>
		<Prop name="MnemonicKey">O</Prop>
	    </Props>
           </Action>
         </Action>
         <Action label="Help" name="Help" type="MENU" handler="SERVER" url="">
            <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.menu@Help'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="MnemonicKey">H</Prop>
	    </Props>
            <Action label="Item Assembly" name="topMenuHelp" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.menu@Item Assembly'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">pressed F1</Prop>
		<Prop name="MnemonicKey">I</Prop>
	    </Props>
            </Action>
            <Action label="About..." name="About" type="MENUITEM" handler="CLIENT" url="">
               <xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cxItemAssembly.menu@About...'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	    <Props>
		<Prop name="AcceleratorKey">control B</Prop>
		<Prop name="MnemonicKey">B</Prop>
	    </Props>
            </Action>
         </Action>
      </MenuBar>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cxItemAssembly.menu@Content">Main menu item label &quot;Content&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu.mnemonic.Content@C">Main menu item mnemonic for label &quot;Content&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu@View">Main menu item label &quot;View&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu.mnemonic.View@V">Main menu item mnemonic for label &quot;View&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu@Refresh">Main menu item label &quot;Refresh&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu.mnemonic.Refresh@R">Main menu item mnemonic for label &quot;Refresh&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu@Options...">Main menu item label &quot;Options...&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu.mnemonic.Options...@O">Main menu item mnemonic for label &quot;Options...&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu@Help">Main menu item label &quot;Help&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu.mnemonic.Help@H">Main menu item mnemonic for label &quot;Help&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu@Item Assembly">Main menu item label for help topic &quot;Item Assembly&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu.mnemonic.Item Assembly@A">Main menu item mnemonic for label for help topic &quot;Item Assembly&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu@About...">Main menu item label &quot;About...&quot;</key>
      <key name="psx.sys_cxItemAssembly.menu.mnemonic.About...@B">Main menu item mnemonic for label &quot;About...&quot;</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
