<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html [
	<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="itemslotvariants" select="/*/itemslotvariantsurl"/>
	<xsl:variable name="parentslots" select="/*/contentslotvariantsurl"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_rcSupport.modifyslotitem@Modify Item Slot/Variant'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<meta name="Generator" content="EditPlus"/>
				<meta name="Author"/>
				<meta name="Keywords"/>
				<meta name="Description"/>
				<script language="javascript" ><![CDATA[
               function variant(id, name)
               {
                   this.id = id;
                   this.name = name;
               }
               
               function slot(id, name, variants)
               {
                   this.id = id;
                   this.name = name;
                   this.variants = variants;
               }
            ]]></script>
				<!-- begin XSL -->
				<xsl:element name="script">
					<xsl:attribute name="language">javascript</xsl:attribute>
					<xsl:text>slots = new Array(</xsl:text>
					<xsl:for-each select="document($itemslotvariants)/contentslotvariantlist/slot[@slotid=document($parentslots)/contentslotvariantlist/slot/@slotid]">
						<xsl:text>new slot(</xsl:text>
						<xsl:value-of select="@slotid"/>
						<xsl:text>, &quot;</xsl:text>
						<xsl:value-of select="@name"/>
						<xsl:text>&quot;, new Array(</xsl:text>
						<xsl:variable name="variant"/>
						<xsl:for-each select="variant">
							<xsl:text>new variant(</xsl:text>
							<xsl:value-of select="@variantid"/>
							<xsl:text>,&quot;</xsl:text>
							<xsl:value-of select="@name"/>
							<xsl:text>&quot; )</xsl:text>
							<xsl:if test="not(position() = last())">
								<xsl:text>,</xsl:text>
							</xsl:if>
						</xsl:for-each>))
                  <xsl:if test="not(position() = last())">
							<xsl:text>,</xsl:text>
						</xsl:if>
					</xsl:for-each>
					<xsl:text>);</xsl:text>
				</xsl:element>
				<!-- end XSL -->
				<script language="javascript" ><![CDATA[
               function onFormLoad() 
               {
                   self.resizeTo(425, 201);
                   document.forms[0].newslotid.options.length = slots.length;
                   for(i=0; i<slots.length; i++)
                   {
                       document.forms[0].newslotid.options[i].value=slots[i].id;
                       document.forms[0].newslotid.options[i].text=slots[i].name;
                       if(slots[i].id == document.forms[0].sys_slotid.value)
                           document.forms[0].newslotid.options.selectedIndex = i;
                   }
                   slot_onchange();
                   self.focus();
               }
               
               function slot_onchange() 
               {
                   if(slots.length < 1)
                      return;
               
                   index = document.forms[0].newslotid.options.selectedIndex;
                   variants = slots[index].variants;
                   document.forms[0].newvariantid.options.length = variants.length;
                   for(i=0; i<variants.length; i++)
                   {
                       document.forms[0].newvariantid.options[i].value=variants[i].id;
                       document.forms[0].newvariantid.options[i].text=variants[i].name;
                       if(variants[i].id == document.forms[0].itemvariantid.value)
                           document.forms[0].newvariantid.options.selectedIndex = i;
                   }
               }
               
               function submitForm() 
               {
                     if(!window.opener || window.opener.closed)
                     {
                        self.close();
                        return;
                     }
               
                   window.opener.name= "rceditor";
                   document.modifyslotitem.target = "rceditor";
                   var caller = window.opener.location.href;
                   document.modifyslotitem.httpcaller.value = caller;
                   document.modifyslotitem.action = "updaterelateditems.html";
               	   document.modifyslotitem.submit();
                   // Must delay window close for Netscape 6.2
                   // otherwise form does not get submitted
                   window.setTimeout('self.close()',200);
               }
                  function parseParam(param, href)
                  {
                     var value = "";
                     if(param == null || param=="")
                        return value;
               
                     value = href.substring(index+param.length+1);
                     index = value.indexOf("&");
                     if(index == -1)
                        return value;
               
                     value = value.substring(0, index);
               
                     return value;
                  }
         ]]></script>
			</head>
			<body bgcolor="0xffffff" onload="javascript:onFormLoad()">
				<!--   psx-docalias="itemslotvariants" psx-docref="psx-itemslotvariantsurl"   -->
				<form method="post" action="" name="modifyslotitem">
					<xsl:apply-templates select="*/sysid" mode="mode0"/>
					<xsl:apply-templates select="*/contentid" mode="mode1"/>
					<xsl:apply-templates select="*/revision" mode="mode2"/>
					<xsl:apply-templates select="*/variantid" mode="mode3"/>
					<input type="hidden" name="sys_command" value="modify"/>
					<xsl:apply-templates select="*/slot" mode="mode4"/>
					<xsl:apply-templates select="*/variant" mode="mode5"/>
					<xsl:apply-templates select="*/rxcontext" mode="mode6"/>
					<xsl:apply-templates select="*/authtype" mode="mode7"/>
					<input type="hidden" name="httpcaller"/>
					<table width="100%" border="0" cellspacing="1" cellpadding="0" class="headercell" summary="controls for editing metadata">
						<!--  ItemContent  -->
						<xsl:apply-templates select="*/relateditem" mode="mode8"/>
						<tr class="datacell1">
							<td class="controlname" width="45%">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_rcSupport.modifyslotitem@Slot'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>:
                     </td>
							<td>
								<select name="newslotid" onchange="return slot_onchange()"/>
							</td>
						</tr>
						<!--  Normal Control  -->
						<tr class="datacell2">
							<td class="controlname">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_rcSupport.modifyslotitem@Variant'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>:
                     </td>
							<td>
								<select name="newvariantid"/>
							</td>
						</tr>
						<tr class="datacell1">
							<td align="center" colspan="2">
								<br/>
								<input type="button" value="Update" class="nav_body" onclick="javascript:submitForm();">
									<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Update@U'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
									<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Update'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
								</input>&nbsp;&nbsp;
                        <input type="button" value="Close" class="nav_body" onclick="javascript:window.close();">
									<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
									<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
								</input>
							</td>
						</tr>
					</table>
				</form>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="*">
		<xsl:choose>
			<xsl:when test="text()">
				<xsl:choose>
					<xsl:when test="@no-escaping">
						<xsl:value-of select="." disable-output-escaping="yes"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>&nbsp;</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="not(position()=last())">
			<br id="XSpLit"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="attribute::*">
		<xsl:value-of select="."/>
		<xsl:if test="not(position()=last())">
			<br id="XSpLit"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="*/sysid" mode="mode0">
		<xsl:for-each select=".">
			<input type="hidden" name="sysid">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*/contentid" mode="mode1">
		<xsl:for-each select=".">
			<input type="hidden" name="sys_contentid">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*/revision" mode="mode2">
		<xsl:for-each select=".">
			<input type="hidden" name="sys_revision">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*/variantid" mode="mode3">
		<xsl:for-each select=".">
			<input type="hidden" name="sys_variantid">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*/slot" mode="mode4">
		<xsl:for-each select=".">
			<input type="hidden" name="sys_slotid">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*/variant" mode="mode5">
		<xsl:for-each select=".">
			<input type="hidden" name="itemvariantid">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*/rxcontext" mode="mode6">
		<xsl:for-each select=".">
			<input type="hidden" name="sys_context">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*/authtype" mode="mode7">
		<xsl:for-each select=".">
			<input type="hidden" name="sys_authtype">
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			</input>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="*/relateditem" mode="mode8">
		<xsl:for-each select=".">
			<tr class="headercell">
				<td class="controlname" colspan="2">
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_rcSupport.modifyslotitem@Related Item (ID)'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>:&nbsp;
               <xsl:apply-templates select="."/>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_rcSupport.modifyslotitem@Modify Item Slot/Variant">Title for change variant/slot dialog box, opens up when clicked on Arrange and Change Variant Slot menu item.</key>
		<key name="psx.sys_rcSupport.modifyslotitem@Slot">Label for Slot drop down list box.</key>
		<key name="psx.sys_rcSupport.modifyslotitem@Variant">Label for Variant drop down list box.</key>
		<key name="psx.sys_rcSupport.modifyslotitem@Related Item (ID)">Header for change variant/slot dialog box.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
