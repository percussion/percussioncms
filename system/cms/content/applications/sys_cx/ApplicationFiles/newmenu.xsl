<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:template match="/">
		<NewMenu>
			<xsl:if test="//@uicontext='SystemFolder' or //@uicontext='SystemSite' or //@uicontext='Folder' or //@uicontext='SiteSubfolder' or //@uicontext='Site'">
				<Action actionid="15" handler="CLIENT" label="Folder..." modeid="3" name="Create_New_Folder" type="MENUITEM" uicontextid="9" url="">
					<xsl:attribute name="label"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu@Folder...'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				    <Props>
					<Prop name="MnemonicKey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cx.menu.mnemonic.Folder...@F'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></Prop>
				    </Props>
				</Action>
			</xsl:if>
			<xsl:if test="not(/Dummy/@uicontext='SystemFolder' or /Dummy/@uicontext='SystemSite') ">
				<xsl:copy-of select="document(/Dummy/@newcontenturl)/*"/>
			</xsl:if>
		</NewMenu>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_cx.menu@Folder...">Create new folder menu item label &quot;Folder...&quot;</key>
		<key name="psx.sys_cx.menu.mnemonic.Folder...@F">Main menu item mnemonic for label &quot;Folder...&quot;</key>
		<key name="psx.sys_cx.menu@Item">Create new item menu label &quot;Item&quot;</key>
		<key name="psx.sys_cx.menu.mnemonic.Item@I">Main menu item mnemonic for label &quot;Item&quot;</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
