<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
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
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="this" select="/"/>
	<xsl:strip-space elements="*"/>
	<xsl:template match="/">
		<html>
			<head>
				<meta name="generator" content="Percussion XSpLit Version 3.0"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.resultpage@Rhythmyx - Inline Content Search Results'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
			</head>
			<script language="javascript"><![CDATA[
            var editorname = "";
            if(top.opener!=null && top.opener.INLINE_SEARCH_PAGE!=null)
            {
               editorname = "ektroneditor";
            }
            else
            {
               editorname = "dhtmleditor";
            }
            
            var closeWindowthrCancel = 1;
            function onClickCancel()
            {
               if(closeWindowthrCancel==1)
               {
                  window.returnValue="cancel";
                  self.close();
               }
            }
            function onClickSearchAgain()
            {
               closeWindowthrCancel = 0;
               if(editorname == "ektroneditor")
               {
                  this.location.href =  top.opener.INLINE_SEARCH_PAGE + "&inlineslotid=" + document.forms[0].inlineslotid.value + "&inlinetext=" + document.forms[0].inlinetext.value + "&inlinetype=" + document.forms[0].inlinetype.value;
               }
               else
               {
                  window.returnValue="searchagain";
                  self.close();
               }
            }
            function onClickUpdate(varid, conid, urlstring)
            {
               if(editorname == "ektroneditor")
               {
                  document.updaterelateditems.sys_dependentid.value = conid;
                  document.updaterelateditems.sys_dependentvariantid.value = varid;
                  document.updaterelateditems.urlstring.value = urlstring;
                  setTimeout("document.updaterelateditems.submit()",200);
               }
               else
               {
                  window.returnValue =  "sys_dependentid=" + conid + "&sys_dependentvariantid=" + varid + "&urlstring=" + escape(urlstring);
                  self.close();
               }
            }
            ]]></script>
			<body onload="javascript:self.focus();">
				<form name="updaterelateditems" method="post" action="../sys_ceInlineSearch/returnvariant.html">
					<input type="button" name="searchagain" value="Search Again" onclick="javascript:onClickSearchAgain()">
						<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ceInlineSearch.resultpage@Search Again'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					</input>&nbsp;
               <input type="button" name="cancel" value="Close" onclick="javascript:onClickCancel()">
						<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					</input>
					<input type="hidden" name="inlineslotid" value="{//inlineslotid}"/>
					<input type="hidden" name="inlinetext" value="{//inlinetext}"/>
					<input type="hidden" name="inlinetype" value="{//inlinetype}"/>
					<input type="hidden" name="urlstring" value=""/>
					<input type="hidden" name="sys_command" value="update"/>
					<input type="hidden" name="sys_contentid" value=""/>
					<input type="hidden" name="sys_revision" value=""/>
					<input type="hidden" name="sys_slotid" value="{/*/slotid}"/>
					<input type="hidden" name="sys_variantid" value=""/>
					<input type="hidden" name="sys_context" value=""/>
					<input type="hidden" name="sys_authtype" value=""/>
					<input type="hidden" name="httpcaller" value=""/>
					<input type="hidden" name="sys_dependentid" value=""/>
					<input type="hidden" name="sys_dependentvariantid" value=""/>
					<div align="center">
						<table width="100%" cellpadding="0" cellspacing="3" border="0">
							<xsl:apply-templates select="/*/search" mode="mode2"/>
							<tr class="headercell"><td align="center"><span class="headercellfont">Link Title: </span><input name="linktitle" value="" type="text" size="40"/></td></tr>
							<tr class="headercell">
								<td align="center">
									<xsl:apply-templates select="/" mode="paging"/>&nbsp;
         	</td>
							</tr>
						</table>
					</div>
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
	<xsl:template match="search" mode="mode2">
		<table width="100%" cellpadding="4" cellspacing="0" border="0">
			<tr class="headercell">
				<td align="left" class="headercellfont">
					<xsl:if test="string-length(variantid)">
						<xsl:value-of select="variantname"/>(<xsl:value-of select="variantid"/>)
               </xsl:if>&nbsp;
            </td>
			</tr>
			<tr>
				<td valign="top" class="headercell">
					<table width="100%" border="0" cellspacing="1" cellpadding="0">
						<tr class="headercell2">
							<td class="headercell2font" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.resultpage@Content Title (ID)'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>&nbsp;&nbsp;&nbsp;
                     </td>
							<td class="headercell2font" align="center">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.resultpage@Content Type'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
							<td class="headercell2font" align="center">&nbsp;</td>
						</tr>
						<xsl:if test="string-length(variantid)">
							<xsl:apply-templates select="item" mode="item">
								<xsl:with-param name="varid" select="variantid"/>
							</xsl:apply-templates>
						</xsl:if>
						<xsl:if test="not(string-length(variantid))">
							<tr class="datacell1">
								<td class="datacellnoentriesfound" colspan="4" align="center">
									<xsl:call-template name="getLocaleString">
										<xsl:with-param name="key" select="'psx.generic@No entries found'"/>
										<xsl:with-param name="lang" select="$lang"/>
									</xsl:call-template>.
                        </td>
							</tr>
						</xsl:if>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="item" mode="item">
		<xsl:param name="varid"/>
		<tr class="datacell1">
			<xsl:variable name="url">
				<xsl:choose>
					<xsl:when test="//inlinetype='rxvariant'">
						<xsl:value-of select="previewurlint"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="previewurl"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<td align="left" class="datacell1font">
				<a href="#">
					<xsl:attribute name="onclick">javascript:onClickUpdate('<xsl:value-of select="$varid"/>','<xsl:value-of select="contentid"/>','<xsl:value-of select="$url"/>')</xsl:attribute>
					<xsl:value-of select="contentname"/>(<xsl:value-of select="contentid"/>)
		</a>
			</td>
			<td align="left" class="datacell1font">
				<xsl:value-of select="contenttype"/>
			</td>
			<td align="left" class="datacell1font">
				<a>
					<xsl:attribute name="href">#</xsl:attribute>
					<xsl:attribute name="onclick">javascript:window.open(&quot;<xsl:value-of select="previewurl"/>&quot;,&quot;preview&quot;, &quot;toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=400,height=300,z-lock=1&quot;)</xsl:attribute>
					<img src="../sys_resources/images/preview.gif" alt="Preview" align="top" border="0">
						<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ceInlineSearch.resultpage.alt@Preview'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
					</img>
				</a>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="*" mode="paging">
		<table>
			<tr>
				<td>
					<xsl:apply-templates select="PSXPrevPage" mode="paging"/>
					<xsl:apply-templates select="PSXIndexPage" mode="paging"/>
					<xsl:apply-templates select="PSXNextPage" mode="paging"/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="PSXPrevPage" mode="paging">
		<a>
			<xsl:attribute name="onclick"><xsl:text>javascript:closeWindowthrCancel=0</xsl:text></xsl:attribute>
			<xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>Prev</a>&nbsp;
</xsl:template>
	<xsl:template match="PSXIndexPage" mode="paging">
		<xsl:choose>
			<xsl:when test="not(/*/currentpsfirst='')">
				<xsl:choose>
					<xsl:when test="substring-after(.,'psfirst=')=/*/currentpsfirst">
						<font class="currentPageNumber">
							<xsl:value-of select="@pagenum"/>
						</font>&nbsp;
               </xsl:when>
					<xsl:otherwise>
						<a>
							<xsl:attribute name="onclick"><xsl:text>javascript:closeWindowthrCancel=0</xsl:text></xsl:attribute>
							<xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
							<xsl:value-of select="@pagenum"/>
						</a>&nbsp;
               </xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="position()=1">
						<font class="currentPageNumber">1</font>&nbsp;
               </xsl:when>
					<xsl:otherwise>
						<a>
							<xsl:attribute name="onclick"><xsl:text>javascript:closeWindowthrCancel=0</xsl:text></xsl:attribute>
							<xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
							<xsl:value-of select="@pagenum"/>
						</a>&nbsp;
               </xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="PSXNextPage" mode="paging">
		<a>
			<xsl:attribute name="onclick"><xsl:text>javascript:closeWindowthrCancel=0</xsl:text></xsl:attribute>
			<xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>Next</a>&nbsp;
</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_ceInlineSearch.resultpage@Rhythmyx - Inline Content Search Results">Title for inline content search result page.</key>
		<key name="psx.sys_ceInlineSearch.resultpage@Search Again">Label for Search Again button in inline content search result page.</key>
		<key name="psx.sys_ceInlineSearch.resultpage@Content Title (ID)">Second column header in inline content search result page.</key>
		<key name="psx.sys_ceInlineSearch.resultpage@Content Type">Third column header in inline content search result page.</key>
		<key name="psx.sys_ceInlineSearch.resultpage.alt@Preview">Alt text for preview image in inline content search result page.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
