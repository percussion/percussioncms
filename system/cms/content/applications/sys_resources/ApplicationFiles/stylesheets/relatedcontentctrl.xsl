<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
                xmlns:psxi18n="com.percussion.i18n.PSI18nUtils" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:template match="*" mode="relatedcontentctrl">
		<xsl:param name="mode"/>
		<xsl:param name="editable" select="'yes'"/>
		<xsl:param name="relateddoc"/>
		<table width="100%" cellpadding="0" cellspacing="0" border="0">
			<xsl:if test="not($mode='sys_edit')">
				<tr>
					<td align="left" class="controlname">
						<b>
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@Related Content'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</b>
					</td>
				</tr>
				<xsl:apply-templates select="slot" mode="mode1">
					<xsl:with-param name="relateddoc" select="$relateddoc"/>
					<xsl:with-param name="editable" select="$editable"/>
				</xsl:apply-templates>
			</xsl:if>
			<xsl:if test="$mode='sys_edit'">
				<tr>
					<td class="headercell2font" align="left" width="20">
						<xsl:variable name="rceditorurl" select="$relateddoc/*/rceditorurl"/>
						<input type="button" name="editall">
							<xsl:attribute name="onclick"><xsl:value-of select="concat('javascript:window.open(&quot;', $rceditorurl, '&quot;', ',&quot;rcedit&quot;',',&quot;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=800,height=400,z-lock=1&quot;',')')"/></xsl:attribute>
							<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl.alt@Edit All'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
							<xsl:attribute name="title"><xsl:call-template name="getTooltip"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@Related Content Control'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
							<xsl:attribute name="accesskey"><xsl:call-template name="getMnemonic"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@Related Content Control'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
						</input>
					</td>
					<td class="headercell2font" align="left" colspan="2">&nbsp;</td>
				</tr>
			</xsl:if>
			<xsl:if test="$editable='yes'">
				<tr class="headercell">
					<td class="headercell2font" align="center">
						<input type="button" onclick="javascript:onClose()" value="Close" name="close" accesskey="C">
                     <xsl:attribute name="value">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.generic@Close'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
                     </xsl:attribute>
						</input>
					</td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>
	<xsl:template match="item" mode="mode0">
		<xsl:param name="editable"/>
		<tr class="datacell1">
			<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1"><xsl:value-of select="'datacell1'"/></xsl:when><xsl:otherwise><xsl:value-of select="'datacell2'"/></xsl:otherwise></xsl:choose></xsl:attribute>
			<td class="datacell1font" valign="top" align="left" width="25%">
            <xsl:variable name="siteid" select="relProps/prop[@name='sys_siteid']"/>
            <xsl:variable name="folderid" select="relProps/prop[@name='sys_folderid']"/>
            <xsl:variable name="previewurl" select="concat(previewurl, '&amp;sys_siteid=', $siteid, '&amp;sys_folderid=', $folderid)"/>
				<a href="javascript:void(0);">
					<xsl:attribute name="onclick"><xsl:value-of select="concat('javascript:window.open(&quot;', $previewurl, '&quot;', ',&quot;modifyslot&quot;',',&quot;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=500,height=300,z-lock=1&quot;',')')"/></xsl:attribute>
					<xsl:apply-templates select="@title"/>(
          <xsl:apply-templates select="@itemcontentid"/>)
         </a>
			</td>
			<td class="datacell1font" valign="top" align="left" width="25%">
				<xsl:apply-templates select="type"/>(
          <xsl:apply-templates select="type/@typeid"/>)
        </td>
			<td class="datacell1font" valign="top" align="left" width="25%">
				<xsl:apply-templates select="variant"/>(
          <xsl:apply-templates select="variant/@variantid"/>)
        </td>
			<xsl:if test="$editable='yes'">
				<td width="25%">
					<xsl:if test="position()=1">
						<img border="0" src="../sys_resources/images/relatedcontent/inactive_up.gif">
							<xsl:attribute name="title"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl.alt@Inactivated move up button, either it is the only item in slot or it is the first item.'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
						</img>
					</xsl:if>
					<xsl:if test="not(position()=1)">
						<a href="javascript:go('{moveupurl}');">
							<img border="0" src="../sys_resources/images/relatedcontent/up.gif">
								<xsl:attribute name="title"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl.alt@Moves the current item Left or Up and refreshes the page.'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
							</img>
						</a>
					</xsl:if>
					<xsl:if test="position()=last()">
						<img border="0" src="../sys_resources/images/relatedcontent/inactive_down.gif">
							<xsl:attribute name="title"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl.alt@Inactivated move down button, either it is the only item in slot or it is the last item.'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
						</img>
					</xsl:if>
					<xsl:if test="not(position()=last())">
						<a href="javascript:go('{movedownurl}');">
							<img border="0" src="../sys_resources/images/relatedcontent/down.gif">
								<xsl:attribute name="title"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl.alt@Moves the current item Right or Down and refreshes the page.'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
							</img>
						</a>
					</xsl:if>
					<a href="javascript:void(0);">
						<xsl:attribute name="onclick"><xsl:value-of select="concat('javascript:window.open(&quot;', slotediturl, '&quot;', ',&quot;modifyslot&quot;',',&quot;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=500,height=300,z-lock=1&quot;',')')"/></xsl:attribute>
						<img border="0" src="../sys_resources/images/relatedcontent/define.gif" alt="Move to another slot and/or change variant">
							<xsl:attribute name="title"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl.alt@Move to another slot and/or change variant'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
						</img>
					</a>
					<a href="javascript:go('{deleteurl}');">
						<img border="0" src="../sys_resources/images/relatedcontent/remove.gif" alt="Remove Item from Slot">
							<xsl:attribute name="title"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl.alt@Remove Item from Slot'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
						</img>
					</a>
					<a href="javascript:void(0);">
						<xsl:attribute name="onclick"><xsl:value-of select="concat('javascript:onClickEdit(&quot;', editurl, '&quot;)')"/></xsl:attribute>
						<img border="0" src="../sys_resources/images/relatedcontent/edit.gif" alt="Edit Item in Content Editor">
							<xsl:attribute name="title"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl.alt@Edit Item in Content Editor'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
						</img>
					</a>
				</td>
			</xsl:if>
		</tr>
	</xsl:template>
	<xsl:template match="slot" mode="mode1">
		<xsl:param name="editable"/>
		<xsl:variable name="slotid" select="@slotid"/>
		<xsl:param name="relateddoc"/>
		<xsl:choose>
		<xsl:when test="count(../slot)=1 and $slotid=''">
			<tr class="datacell1">
				<td>&nbsp;</td>
			</tr>
			<tr class="datacell1">
				<td class="datacellnoentriesfound" valign="top" align="center" width="20%">
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@No Slots registered for this content type.'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:when>
		<xsl:otherwise>
			<tr class="headercell">
				<td class="headercell2font" valign="top" align="left" width="20%">
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@Slot(ID)'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>:
            <xsl:choose>
						<xsl:when test="$editable='yes'">
							<a href="javascript:void(0);">
								<xsl:attribute name="onclick"><xsl:value-of select="concat('javascript:___openSearch(', $slotid,',&quot;',document(@slotediturl)/*/url, '&quot;)')"/></xsl:attribute>
								<xsl:value-of select="@name"/>(
                     <xsl:value-of select="@slotid"/>)
                  </a>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="@name"/>(
                  <xsl:value-of select="@slotid"/>)
               </xsl:otherwise>
					</xsl:choose>
				</td>
			</tr>
			<tr class="headercell">
				<td>
					<table width="100%" cellpadding="0" cellspacing="1" border="0">
						<tr class="datacell1">
							<td class="headercell2font" align="left" width="20%">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@Item Title(ID)'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" align="left" width="20%">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@Item Type(ID)'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" align="left" width="20%">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@Item Template(ID)'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<xsl:if test="$editable='yes'">
								<td class="headercellfont" align="center" width="20">
									<xsl:call-template name="getLocaleString">
										<xsl:with-param name="key" select="'psx.sys_resources.stylesheets.relatedcontentctrl@Action'"/>
										<xsl:with-param name="lang" select="$lang"/>
									</xsl:call-template>
								</td>
							</xsl:if>
						</tr>
						<xsl:if test="not(count($relateddoc/*/slot[@slotid=$slotid]/item))">
							<tr class="datacell1">
								<td class="datacellnoentriesfound" align="center">
									<xsl:attribute name="colspan"><xsl:choose><xsl:when test="$editable='yes'">4</xsl:when><xsl:otherwise>3</xsl:otherwise></xsl:choose></xsl:attribute>
									<xsl:call-template name="getLocaleString">
										<xsl:with-param name="key" select="'psx.generic@No entries found'"/>
										<xsl:with-param name="lang" select="$lang"/>
									</xsl:call-template>.
                     </td>
							</tr>
						</xsl:if>
						<xsl:apply-templates select="$relateddoc/*/slot[@slotid=$slotid]/item" mode="mode0">
							<xsl:sort select="@sortorder" data-type="number"/>
							<xsl:with-param name="editable" select="$editable"/>
						</xsl:apply-templates>
					</table>
				</td>
			</tr>
		</xsl:otherwise>
		</xsl:choose>
		<tr class="datacell1">
			<td>&nbsp;</td>
		</tr>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl@Related Content">Main header for related content table dialog box.</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl.alt@Edit in table mode">Alt text for Edit All button image in content editor</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl.alt@Moves the current item Left or Up and refreshes the page.">Alt text for Move Left/UP image.</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl.alt@Moves the current item Right or Down and refreshes the page.">Alt text for Move Right/Down image.</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl.alt@Move to another slot and/or change variant">Alt text for Move to another slot and/or change variant image.</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl.alt@Remove Item from Slot">Alt text for Remove Item from Slot image.</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl.alt@Edit Item in Content Editor">Alt text for Edit Item in Content Editor image.</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl@Slot(ID)">Slot header in related content table.</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl@Item Title(ID)">First column header in related content table</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl@Item Type(ID)">Second column header in related content table</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl@Item Template(ID)">Third column header in related content table</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl@Action">Fourth column header in related content table</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl.alt@Inactivated move up button, either it is the only item in slot or it is the first item.">Alt text for inactivated move up button</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl.alt@Inactivated move down button, either it is the only item in slot or it is the last item.">Alt text for inactivated move down button</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl@Related Content Control" mnemonic="A" tooltip="Type Alt-A to edit this item's related content.">Title for the edit all button.</key>
		<key name="psx.sys_resources.stylesheets.relatedcontentctrl@psx.sys_resources.stylesheets.relatedcontentctrl@No Slots registered for this content type.">Error message when no slots found for the content type.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
