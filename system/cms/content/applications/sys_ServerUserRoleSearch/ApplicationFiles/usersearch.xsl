<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
		%HTMLspecial;
		<!ENTITY % w3centities-f PUBLIC
				"-//W3C//ENTITIES Combined Set//EN//XML"
				"http://www.w3.org/2003/entities/2007/w3centities-f.ent"
				>
		%w3centities-f;
		]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:include href="file:sys_resources/stylesheets/redirect.xsl"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@Rhythmyx - Content Editor - User Search'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<script><![CDATA[
      			  function Search_onclick() 
      				{
      					var urlstr = document.URL.split("?");
      					document.updatesearch.action=urlstr[0]+"?sys_command=GetUsers&sys_role="+document.updatesearch.rolename[document.updatesearch.rolename.selectedIndex].value+"&namefilter=";
      					if(document.updatesearch.namefilter.value!=''){
      						document.updatesearch.action = document.updatesearch.action + document.updatesearch.namefilter.value;
      					}
      					document.updatesearch.submit();
      				}
      			  function Ok_onclick() 
      				{
      					if(!window.opener || window.opener.closed)
      					{
      						self.close();
      						return;
      					}
      					var val = ";";
      					for (var i=0; i < document.updatesearch.elements.length; i++)
      					{
      						if (document.updatesearch.elements[i].type=="checkbox" && document.updatesearch.elements[i].checked==true)
      						{  
      							val = val + document.updatesearch.elements[i].value + ";";
      						}
      					}
      					window.opener.setUserDataField(val);
      					self.close();
      				}
	     	]]></script>
			</head>
			<body onload="javascript:self.focus();">
				<!--   psx-docalias="createcontentlookup" psx-docref="psx-createcontentlookupurl"   -->
				<form name="updatesearch" method="post">
					<input type="hidden" name="fromRoles" value="{//@fromRoles}"/>
					<input type="hidden" name="sys_contentid" value="{//@contentid}"/>
					<table align="center" width="75%" border="0" cellspacing="1" cellpadding="0" class="headercell">
						<xsl:apply-templates select="rolelist" mode="mode0"/>
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
	<xsl:template match="rolelist[@id='GetRoles']" mode="mode0">
		<tr class="outerboxcell">
			<td colspan="2" align="center" class="outerboxcellfont">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@User Search'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</td>
		</tr>
		<tr>
			<td colspan="2">&nbsp;</td>
		</tr>
		<tr class="datacell1">
			<td class="datacell1font">
            <label for="rolename">
				<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Role@R'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
               <xsl:call-template name="getMnemonicLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@Role'"/>
                  <xsl:with-param name="mnemonickey" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Role@R'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>:
            </label>
         </td>
			<td class="datacell1font">
				<select name="rolename" size="1">
					<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Role@R'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					<xsl:for-each select="role">
						<option>
							<xsl:attribute name="value"><xsl:value-of select="@name"/></xsl:attribute>
							<xsl:value-of select="@name"/>
						</option>
					</xsl:for-each>
				</select>
			</td>
		</tr>
		<tr class="datacell2">
			<td class="datacell1font">
            <label for="namefilter">
				<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Name Filter@N'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
               <xsl:call-template name="getMnemonicLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@Name Filter'"/>
                  <xsl:with-param name="mnemonickey" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Name Filter@N'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </label>
			</td>
			<td class="datacell1font">
				<input type="text" name="namefilter" width="15">
					<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Name Filter@N'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				</input>
			</td>
		</tr>
		<tr class="datacell1">
			<td colspan="2" align="center">
				<br id="XSpLit"/>
				<input type="button" name="Search" value="Search" language="javascript" onclick="return Search_onclick()">
					<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Search@S'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@Search'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				</input>&nbsp;
		       <input type="button" name="Close" value="Close" language="javascript" onclick="window.close()">
					<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				</input>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="rolelist" mode="mode0">
		<tr class="outerboxcell">
			<td colspan="2" align="center" class="outerboxcellfont">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@User Search Results'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</td>
		</tr>
		<tr>
			<td colspan="2">&nbsp;</td>
		</tr>
		<xsl:for-each select="role">
			<tr class="datacell1">
				<td class="headercellfont" align="left" colspan="2">
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@Role'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>:
               <xsl:value-of select="@name"/>
				</td>
			</tr>
			<xsl:if test="(not(count(user)))">
				<tr class="datacell2">
					<td class="datacellnoentriesfound" colspan="2" align="center">
						<xsl:call-template name="getLocaleString">
							<xsl:with-param name="key" select="'psx.generic@No entries found'"/>
							<xsl:with-param name="lang" select="$lang"/>
						</xsl:call-template>.
				</td>
				</tr>
			</xsl:if>
			<xsl:if test="(count(user))">
				<xsl:for-each select="user">
					<xsl:if test="not(.='rxserver')">
						<tr class="datacell1">
							<td width="10%" class="datacell1font">
								<input type="checkbox" name="usernames" value="{.}">
									<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.translate.mnemonic.UserName@A'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
								</input>
							</td>
							<td class="datacell1font">
								<xsl:value-of select="."/>
							</td>
						</tr>
					</xsl:if>
				</xsl:for-each>
			</xsl:if>
		</xsl:for-each>
		<tr class="datacell1">
			<td colspan="2" align="center">
				<br id="XSpLit"/>
				<input type="button" name="Back" value="Back&nbsp;" language="javascript" onclick="history.back();">
					<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Back@B'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@Back'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				</input>&nbsp;
            <input type="button" name="Ok" value="&nbsp;&nbsp;&nbsp;OK&nbsp;&nbsp;&nbsp;" language="javascript" onclick="return Ok_onclick()">
					<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Ok@O'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ServerUserRoleSearch.usersearch@Ok'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				</input>&nbsp;
            <input type="button" name="Close" value="Close" language="javascript" onclick="window.close()">
					<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				</input>
			</td>
		</tr>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_ServerUserRoleSearch.usersearch@Rhythmyx - Content Editor - User Search">Title for the User Search dialog box opens up when clicked on Search button on Workflow Ad hoc Assignees.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch@User Search">Main header for User Search dialog box.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch@Name Filter">Label before Name Filter drop drown list box.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch@Search">Label of search button</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch@User Search Results">Main header for User search results page.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch@Role">Label before Roles drop drown list box.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch@Back">Label of back button</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch@Ok">Label of OK button</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Role@R">Mnemonic for label &quot;Role&quot;.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Name Filter@N">Mnemonic for label &quot;Name Filter&quot;.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Search@S">Mnemonic for label &quot;Search&quot;.</key>
		<key name="psx.sys_ServerUserRoleSearch.translate.mnemonic.UserName@A">Mnemonic for label &quot;UserName&quot;.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Back@B">Mnemonic for label &quot;Back&quot;.</key>
		<key name="psx.sys_ServerUserRoleSearch.usersearch.mnemonic.Ok@O">Mnemonic for label &quot;Ok&quot;.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
