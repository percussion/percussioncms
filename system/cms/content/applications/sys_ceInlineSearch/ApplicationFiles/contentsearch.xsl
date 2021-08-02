<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "percussion:/DTD/HTMLlat1x.ent">
		%HTMLlat1;
		<!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "percussion:/DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
		<!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "percussion:/DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="searchdefcommelem" select="document('../rxconfig/Server/config.xml')/*/BrowserUISettings/SearchSettings/@useCurrentCommunity"/>
	<xsl:variable name="searchdefcomm">
		<xsl:choose>
			<xsl:when test="$searchdefcommelem">
				<xsl:value-of select="$searchdefcommelem"/>
			</xsl:when>
			<xsl:otherwise>yes</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="this" select="/"/>
	<xsl:variable name="CreateContentlookup" select="/*/contenttypesurl"/>
	<xsl:variable name="CreateStatuslookup" select="/*/statusurl"/>
	<xsl:variable name="contentlookup" select="document(//contenttypesurl)"/>
	<xsl:variable name="communitycontentlookup" select="document(//communitycontentlookupurl)"/>
	<xsl:variable name="checkContentTypes">
		<xsl:choose>
			<xsl:when test="//logincommunity!=0">
				<xsl:for-each select="$communitycontentlookup//community">
					<xsl:if test="contenttype[displayname=$contentlookup//item/display]">1</xsl:if>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="not($contentlookup//item/display='')">1</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:template match="/">
		<html>
			<head>
				<script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
				<script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
				<xsl:if test="//logincommunity!=0">
					<script language="javascript" ><![CDATA[
               function contenttype(id, name)
               {
                   this.id = id;
                   this.name = name;
               }

               function community(id, name, contenttypes)
               {
                   this.id = id;
                   this.name = name;
                   this.contenttypes = contenttypes;
               }
               ]]></script>
					<!-- begin XSL -->
					<xsl:element name="script">
						<xsl:attribute name="language">javascript</xsl:attribute>
						<xsl:text>communities = new Array(</xsl:text>
						<xsl:for-each select="$communitycontentlookup//community">
							<xsl:text>new community(</xsl:text>
							<xsl:value-of select="id"/>
							<xsl:text>, &quot;</xsl:text>
							<xsl:value-of select="name"/>
							<xsl:text>&quot;, new Array(</xsl:text>
							<xsl:for-each select="contenttype[displayname=$contentlookup//item/display]">
								<xsl:text>new contenttype(</xsl:text>
								<xsl:value-of select="id"/>
								<xsl:text>,&quot;</xsl:text>
								<xsl:value-of select="displayname"/>
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
						<xsl:text>allcontenttypes = new Array(</xsl:text>
						<xsl:for-each select="$contentlookup//item">
							<xsl:text>&quot;</xsl:text>
							<xsl:value-of select="display"/>
							<xsl:text>&quot;</xsl:text>
							<xsl:if test="not(position() = last())">
								<xsl:text>,</xsl:text>
							</xsl:if>
						</xsl:for-each>
						<xsl:text>);</xsl:text>
						<xsl:text>allcontentids = new Array(</xsl:text>
						<xsl:for-each select="$contentlookup//item">
							<xsl:text>&quot;</xsl:text>
							<xsl:value-of select="id"/>
							<xsl:text>&quot;</xsl:text>
							<xsl:if test="not(position() = last())">
								<xsl:text>,</xsl:text>
							</xsl:if>
						</xsl:for-each>
						<xsl:text>);</xsl:text>
					</xsl:element>
					<!-- end XSL -->
				</xsl:if>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="Content-Type" content="text/html; UTF-8"/>
				<meta name="generator" content="Percussion XSpLit Version 3.5"/>
				<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Rhythmyx - Inline Content Search'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<script src="../sys_resources/js/formValidation.js" language="javascript">;</script>
				<script src="../sys_ceInlineSearch/searchinit.js" language="javascript">;</script>
				<script src="../sys_resources/js/calPopup.js" language="javascript"><![CDATA[;]]></script>
				<script id="clientEventHandlersJS" language="javascript" ><![CDATA[
               var fromSearch = 0; 
               var editorname = "";
               if(top.opener!=null && top.opener.INLINE_RETURN_PAGE!=null)
               {
                  editorname = "ektroneditor";
               }
               else
               {
                  editorname = "dhtmleditor";
               }
               function Search_onclick() 
               {
                  fromSearch = 0;
                  var str = "";
                  if(!dateValidate(document.updatesearch.sys_enddate.value))
                  {
                     return false;
                  }
                  if(!dateValidate(document.updatesearch.sys_startdate.value))
                  {
                     return false;
                  }
               
                  if(document.forms[0].sys_author.value!="")
                  {
                     document.forms[0].sys_author.value = "%" + document.forms[0].sys_author.value + "%";
                  }
                  if(document.forms[0].sys_contenttitle.value!="")
                  {
                     document.forms[0].sys_contenttitle.value="%"+document.forms[0].sys_contenttitle.value+"%";
                  }
                  for(var i=0; i<document.forms[0].length; i++)
                  {
                     if(document.forms[0][i].type == "text")
                     {
                        str += document.forms[0][i].name + "=" + document.forms[0][i].value + "&";      
                     }
                     else if(document.forms[0][i].type == "select-one")
                     {
                        str += document.forms[0][i].name + "=" + document.forms[0][i][document.forms[0][i].selectedIndex].value+ "&";
                     }
                  }
                  fromSearch = 1;
                  if(editorname == "ektroneditor")
                  {
                     document.updatesearch.action = top.opener.INLINE_RETURN_PAGE;
                     document.updatesearch.submit();
                  }
                  else
                  {
                     window.returnValue = str;
                     self.close();
                  }
               }
               
               function Reset_onclick() 
               {
                  document.forms[0].sys_contenttitle.value=""
                  document.forms[0].sys_contentid.value=""
                  document.forms[0].sys_author.value=""
                  document.forms[0].sys_contenttype.value=""
                  document.forms[0].sys_status.value=""
                  document.forms[0].sys_enddate.value=""
                  document.forms[0].sys_startdate.value=""
                  fromSearch = 0;
               }
               
               function Cancel_onclick() 
               {
                  if(fromSearch!=1)
                  {
                     window.returnValue = "cancel";
                     self.close();
                  }
               }
            ]]></script>
			</head>
			<body onload="javascript:self.focus();" onUnload="javascript:Cancel_onclick()">
				<!--   psx-docalias="CreateContentlookup" psx-docref="psx-contenttypesurl"   -->
				<br xsplit="yes"/>
				<form name="updatesearch" method="post">
					<input type="hidden" name="inlineslotid" value="{//inlineslotid}"/>
					<input type="hidden" name="inlinetext" value="{//inlinetext}"/>
					<input type="hidden" name="inlinetype" value="{//inlinetype}"/>
					<table align="center" width="75%" border="0" cellspacing="1" cellpadding="0" class="headercell">
						<input type="hidden" name="logincommunity" value="{//logincommunity}"/>
						<input type="hidden" name="searchdefcomm" value="{$searchdefcomm}"/>
						<tr class="headercell">
							<td colspan="2" align="left" class="headercellfont">
								<xsl:call-template name="getLocaleString">
									<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Search Parameters'"/>
									<xsl:with-param name="lang" select="$lang"/>
								</xsl:call-template>
							</td>
						</tr>
						<xsl:choose>
							<xsl:when test="$checkContentTypes=''">
								<tr class="datacell1">
									<td align="left" class="datacell1font">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@No variants are available for the Inline Slot. Please consult your Rhythmyx Administrator.'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</td>
								</tr>
								<tr class="datacell2">
									<td colspan="2" align="center">
										<br id="XSpLit"/>
                         <input type="button" name="Cancel" value="Close" language="javascript" onclick="return Cancel_onclick()">
											<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
										</input>
										<br id="XSpLit"/>&nbsp;
                    </td>
								</tr>
							</xsl:when>
							<xsl:otherwise>
								<tr class="datacell1">
									<td align="left" class="datacell1font">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Title'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>:
                     </td>
									<td class="datacell1font">
										<input size="15" type="text" name="sys_contenttitle">
											<xsl:attribute name="value"><xsl:value-of select="/*/contenttitle"/></xsl:attribute>
										</input>
									</td>
								</tr>
								<tr class="datacell1">
									<td align="left" class="datacell1font">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@ID'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>:
                     </td>
									<td class="datacell1font">
										<input size="15" type="text" name="sys_contentid">
											<xsl:attribute name="value"><xsl:value-of select="/*/contentid"/></xsl:attribute>
										</input>
									</td>
								</tr>
								<xsl:choose>
									<xsl:when test="//logincommunity!=0">
										<tr class="datacell1" border="0">
											<td align="left" class="datacell1font">
												<xsl:call-template name="getLocaleString">
													<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Community'"/>
													<xsl:with-param name="lang" select="$lang"/>
												</xsl:call-template>:
                           </td>
											<td align="left" class="datacell1font">
												<select name="sys_communityid" onchange="return community_onchange()">
													<option>Leave this for NS</option>
													<option>1</option>
													<option>2</option>
													<option>3</option>
													<option>4</option>
													<option>5</option>
												</select>
											</td>
										</tr>
										<tr class="datacell1" border="0">
											<td align="left" class="datacell1font">
												<xsl:call-template name="getLocaleString">
													<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Content Type'"/>
													<xsl:with-param name="lang" select="$lang"/>
												</xsl:call-template>:
                           </td>
											<td align="left" class="datacell1font">
												<select name="sys_contenttype">
													<option>Leave this for NS</option>
													<option>1</option>
													<option>2</option>
													<option>3</option>
													<option>4</option>
													<option>5</option>
												</select>
											</td>
										</tr>
									</xsl:when>
									<xsl:otherwise>
										<xsl:apply-templates select="document($CreateContentlookup)/*" mode="mode2">
											<xsl:with-param name="ctype" select="/*/contenttype"/>
										</xsl:apply-templates>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:apply-templates select="document($CreateStatuslookup)/*" mode="mode3">
									<xsl:with-param name="stype" select="/*/status"/>
								</xsl:apply-templates>
								<tr class="datacell2">
									<td align="left" class="datacell1font">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Author'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>:
                     </td>
									<td class="datacell1font">
										<input size="15" type="text" name="sys_author">
											<xsl:attribute name="value"><xsl:value-of select="/*/author"/></xsl:attribute>
										</input>
									</td>
								</tr>
								<tr class="datacell2">
									<td class="datacell1font" align="left">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Start Date Before'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>:
                     </td>
									<td class="datacell1font">
										<input type="text" name="sys_enddate" size="12">
											<xsl:attribute name="value"><xsl:value-of select="/*/enddate"/></xsl:attribute>
										</input>&nbsp;
                         <a href="#" onclick="showCalendar(document.forms[0].sys_enddate);">
											<img border="0" height="20" width="20" src="../sys_resources/images/cal.gif">
												<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch.alt@Calendar Pop-up'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
											</img>
										</a>
									</td>
								</tr>
								<tr class="datacell1">
									<td align="left" class="datacell1font">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Start Date After'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>:
                     </td>
									<td class="datacell1font">
										<input type="text" name="sys_startdate" size="12">
											<xsl:attribute name="value"><xsl:value-of select="/*/startdate"/></xsl:attribute>
										</input>&nbsp;
                         <a href="#" onclick="showCalendar(document.forms[0].sys_startdate);">
											<img border="0" height="20" width="20" src="../sys_resources/images/cal.gif">
												<xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch.alt@Calendar Pop-up'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
											</img>
										</a>
									</td>
								</tr>
								<tr class="datacell2">
									<td colspan="2" align="center">
										<br id="XSpLit"/>
										<input type="button" name="Search" value="Search" language="javascript" onclick="return Search_onclick()">
											<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Search'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
										</input>&nbsp;
                         <input type="button" name="Reset" value="Reset" language="javascript" onclick="return Reset_onclick()">
											<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Reset'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
										</input>&nbsp;
                         <input type="button" name="Cancel" value="Close" language="javascript" onclick="return Cancel_onclick()">
											<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
										</input>
										<br id="XSpLit"/>&nbsp;
                    </td>
								</tr>
							</xsl:otherwise>
						</xsl:choose>
					</table>
				</form>
				<xsl:if test="//logincommunity!=0 and not($checkContentTypes='')">
					<script language="javascript">
	            javascript:onFormLoad();
            </script>
				</xsl:if>
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
	<xsl:template match="item" mode="mode0">
		<option>
			<xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
			<xsl:value-of select="display"/>
		</option>
	</xsl:template>
	<xsl:template match="*" mode="mode2">
		<xsl:param name="ctype"/>
		<tr class="datacell1">
			<td class="datacell1font" align="left">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Content Type'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>:
         </td>
			<td class="datacell1font">
				<select name="sys_contenttype">
					<option value="">
						<xsl:if test="$ctype=''">
							<xsl:attribute name="selected"/>
						</xsl:if>
					</option>
					<xsl:for-each select="item">
						<option>
							<xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
							<xsl:if test="$ctype=id">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="display"/>
						</option>
					</xsl:for-each>
				</select>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="*" mode="mode3">
		<xsl:param name="stype"/>
		<tr class="datacell1">
			<td class="datacell1font" align="left">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.sys_ceInlineSearch.contentsearch@Status'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>:
         </td>
			<td class="datacell1font">
				<select name="sys_status">
					<option value="">
						<xsl:if test="$stype=''">
							<xsl:attribute name="selected"/>
						</xsl:if>
					</option>
					<xsl:for-each select="item">
						<option>
							<xsl:attribute name="value"><xsl:value-of select="name"/></xsl:attribute>
							<xsl:if test="$stype=name">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="displayname"/>
						</option>
					</xsl:for-each>
				</select>
			</td>
		</tr>
	</xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_ceInlineSearch.contentsearch@Rhythmyx - Inline Content Search">Title for the Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Search Parameters">Header for the inline search box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Title">Title label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@ID">ID label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Community">Community label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Content Type">Content Type label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Status">Status label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Author">Author label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Start Date Before">Start Date Before label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Start Date After">Start Date After label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch.alt@Calendar Pop-up">Alt text for calender image in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Search">Search button label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@Reset">Reset button label in Inline Content Search dialog box.</key>
		<key name="psx.sys_ceInlineSearch.contentsearch@No variants are available for the Inline Slot. Please consult your Rhythmyx Administrator.">Message appears when no variants are available for the selected inline slot.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
