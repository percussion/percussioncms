<?xml version="1.0" encoding="UTF-8"?>
<!--
   This stylesheet renders search results conforming to the SearchResults.dtd.
   The rendered page has 3 parts: 
   
   1. - the table header, which displays the column headers for all 
      non-category columns.
      
   2. - all search results in document order. Category columns are rendered in
      a tree like manner according to the category level attribute.
      
   3. - the page actions. All page actions are rendered as buttons at the 
      bottom of the page. The actions are rendered in document order.
-->
<!DOCTYPE xsl:stylesheet [
	<!ENTITY % HTMLlat1 SYSTEM "/Rhythmyx/DTD/HTMLlat1x.ent">
	%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "/Rhythmyx/DTD/HTMLsymbolx.ent">
	%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "/Rhythmyx/DTD/HTMLspecialx.ent">
	%HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>
	<xsl:variable name="lang" select="/*/@xml:lang"/>
	<!--
      Depending on the selection mode, all items are rendered as checkboxes 
      for selection mode 'multiple' and radion buttons for selection mode
      'single'.
   -->
	<xsl:variable name="selectionmode" select="/SearchResults/@selectionMode"/>
	<!--
      The tab size for category indentations. Defaults to 10 and can be 
      overridden with the document attribute 'tabsize'.
   -->
	<xsl:variable name="tabsize">
		<xsl:choose>
			<xsl:when test="/SearchResults/@tabsize">
				<xsl:value-of select="/SearchResults/@tabsize"/>
			</xsl:when>
			<xsl:otherwise>10</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:template match="/">
		<xsl:apply-templates select="SearchResults"/>
	</xsl:template>
	<xsl:template match="SearchResults">
		<html>
			<head>
				<meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
				<script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
				<script language="javascript"><![CDATA[
				/**
				* If this page was opened for multiple selection, this script 
				* submits the selected links and all hidden input values.
				*/
				function onClickLinkToSlot()
				{
					var checkBoxArr = getSelectedCheckbox(document.searchresults.conidvarid);                     
					if (checkBoxArr.length == 0)
					{
						alert(LocalizedMessage("select_one_item_before_inserting"));
						return;
					}
					document.searchresults.sys_slotid.value = document.searchresults.slotId.value;
					window.opener.name="rceditor";
					document.searchresults.target = "rceditor";
					var caller = window.opener.location.href;
					document.searchresults.sys_activeitemid.value = parseParam("sys_activeitemid", 	caller); 
					document.searchresults.sys_contentid.value = parseParam("sys_contentid", caller); 
					document.searchresults.sys_revision.value = parseParam("sys_revision", caller); 
					document.searchresults.sys_authtype.value = parseParam("sys_authtype", caller); 
					document.searchresults.sys_context.value = parseParam("sys_context", caller); 
					document.searchresults.sys_siteid.value = parseParam("sys_siteid", caller); 
					document.searchresults.sys_folderid.value = parseParam("sys_folderid", caller);
					document.searchresults.httpcaller.value = caller;
					setTimeout("document.searchresults.submit()",200);
					setTimeout("self.close()",200);
				}
				
				/**
				* If this page was opened for single selection, this script 
				* submits the selected link and all hidden input values.
				*/
				function onClickCreateLink()
				{
					var selValue = getSelectedRadioValue(document.searchresults.inlineitem);
					if (selValue == "")
					{
						alert(LocalizedMessage("select_one_item_before_inserting"));
						return;
					}
					document.searchresults.sys_dependentid.value = (selValue.split(";"))[0];
					document.searchresults.sys_dependentvariantid.value = (selValue.split(";"))[1];
					document.searchresults.sys_siteid.value = (selValue.split(";"))[2];
					document.searchresults.sys_folderid.value = (selValue.split(";"))[3];
					var index = getSelectedRadio(document.searchresults.inlineitem);
					document.searchresults.urlstring.value = eval("urlstring_" + (index+1));
					setTimeout("document.searchresults.submit()",200);
				}
            
				/**
				* Redo the request that produced the current search results.
				*/
				function onClickSearchAgain()
				{
					window.history.back();
				}
				
				/**
				* Close the current browser window.
				*/
				function onClickCancel()
				{
					self.close();
				}
				function closeProgressWindow()
				{				   
				   var closeWin = window.open("", 'progressWin','width=0;height=0;scrollbars=0;toolbar=0;status=0');
				   closeWin.close();				
             	}

				function getSelectedRadio(buttonGroup) 
				{
					// returns the array number of the selected radio button or -1 if no button is selected
					if (buttonGroup[0]) 
					{ // if the button group is an array (one button is not an array)
						for (var i=0; i<buttonGroup.length; i++) 
						{
							if (buttonGroup[i].checked) 
							{
								return i;
							}
						}
					} 
					else
					{
						if (buttonGroup.checked) 
						{ 
							return 0; 
						} // if the one button is checked, return zero
					}
					// if we get to this point, no radio button is selected
					return -1;
				} // Ends the "getSelectedRadio" function
				
				function getSelectedRadioValue(buttonGroup) 
				{
					// returns the value of the selected radio button or "" if no button is selected
					var i = getSelectedRadio(buttonGroup);
					if (i == -1) 
					{
						return "";
					} 
					else
					{
						if (buttonGroup[i]) 
						{ // Make sure the button group is an array (not just one button)
							return buttonGroup[i].value;
						}
						else
						{ // The button group is just the one button, and it is checked
							return buttonGroup.value;
						}
					}
				} // Ends the "getSelectedRadioValue" function

				function getSelectedCheckbox(buttonGroup) 
				{
					// Go through all the check boxes. return an array of all the ones
					// that are selected (their position numbers). if no boxes were checked,
					// returned array will be empty (length will be zero)
					var retArr = new Array();
					var lastElement = 0;
					if (buttonGroup[0])
					{ // if the button group is an array (one check box is not an array)
						for (var i=0; i<buttonGroup.length; i++)
						{
							if (buttonGroup[i].checked) 
							{
								retArr.length = lastElement;
								retArr[lastElement] = i;
								lastElement++;
							}
						}
					} 
					else 
					{ // There is only one check box (it's not an array)
						if (buttonGroup.checked) 
						{ // if the one check box is checked
							retArr.length = lastElement;
							retArr[lastElement] = 0; // return zero as the only array value
						}
					}
					return retArr;
				} // Ends the "getSelectedCheckbox" function

				function parseParam(param, href)
				{
					var value = "";
					if(param == null || param=="")
						return value;
					var index = href.indexOf(param);
					if(index == -1)
						return value;
					value = href.substring(index+param.length+1);
					index = value.indexOf("&");
					if(index == -1)
						return value;
					value = value.substring(0, index);
						return value;
				}
				
               ]]></script>
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_searchSupport.getResults@Search Results'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
				</title>
			</head>
			<body onload="javascript:self.focus();" class="backgroundcolor">
				<form name="searchresults" method="post" accept-charset="UTF-8" encType="multipart/form-data">
					<xsl:choose>
						<xsl:when test="$selectionmode='single'">
							<xsl:attribute name="action">../sys_ceInlineSearch/returnvariant.html</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="action">../sys_rcSupport/updaterelateditems.html</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					<!--The following hidden parameter is needed by the update resource to update the related content-->
					<input type="hidden" name="sys_command" value="update"/>
					<!-- Add selectionmode dependent hidden params -->
					<xsl:choose>
						<xsl:when test="$selectionmode='single'">
							<input type="hidden" name="sys_dependentid"/>
							<input type="hidden" name="sys_dependentvariantid"/>
							<input type="hidden" name="urlstring"/>
							<input type="hidden" name="sys_siteid"/>
							<input type="hidden" name="sys_folderid"/>
						</xsl:when>
						<xsl:otherwise>
							<input type="hidden" name="sys_slotid"/>
							<input type="hidden" name="sys_context" value="0"/>
							<input type="hidden" name="sys_authtype" value="0"/>
							<input type="hidden" name="httpcaller"/>
							<input type="hidden" name="sys_siteid"/>
							<input type="hidden" name="sys_folderid"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:for-each select="/SearchResults/PassThroughParameters/Parameter">
						<input type="hidden" name="{@name}" value="{Value}"/>
					</xsl:for-each>
					<div align="center">
						<table border="0" width="100%" cellpadding="4" cellspacing="0">
							<tbody>
								<tr> 
									<td valign="top" class="headercell">
										<table border="0" width="100%" cellpadding="2" cellspacing="0">
											<tbody class="datacell1">
												<tr class="headercell">
													<td width="10" class="headercellfont" align="left">&nbsp;&nbsp;&nbsp;&nbsp;</td>
													<td width="10" class="headercellfont" align="left">&nbsp;&nbsp;&nbsp;&nbsp;</td>
													<xsl:apply-templates select="Header"/>
													
												</tr>
												<xsl:choose>
													<xsl:when test="not(Results)">
														<tr class="datacellnoentriesfound">
															<td width="10" class="headercellfont" align="left">&nbsp;&nbsp;&nbsp;&nbsp;</td>
															<td width="10" class="headercellfont" align="left">&nbsp;&nbsp;&nbsp;&nbsp;</td>
															<td width="100%" align="center" colspan="9">
																			<xsl:call-template name="getLocaleString">
																				<xsl:with-param name="key" select="'psx.generic@No entries found'"/>
																				<xsl:with-param name="lang" select="$lang"/>
																			</xsl:call-template>.
                                                   </td>
																	
														</tr>
													</xsl:when>
													<xsl:otherwise>
														<xsl:apply-templates select="Results"/>
													</xsl:otherwise>
												</xsl:choose>
											</tbody>
										</table>
									</td>
								</tr>
								<xsl:if test="$selectionmode='single'">
								<tr class="headercell">
								  <td align="center"><span class="headercellfont">Link Title: </span><input name="linktitle" value="" type="text" size="60"/></td>
								</tr>
								</xsl:if>
								<tr class="headercell">
									<xsl:apply-templates select="Actions"/>
								</tr>								
								
							</tbody>
						</table>
					</div>
				</form>
			<script language="javascript1.2">
				closeProgressWindow();
			</script>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="Header">		
			<td>&nbsp;</td>
			<xsl:apply-templates select="HeaderColumn"/>		
	</xsl:template>
	<xsl:template match="HeaderColumn">
		<xsl:if test="./@isCategory='no'">
			<td class="headercellfont" align="left" valign="top">
				<xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
				<xsl:if test="./@sorted='ascending'">
					<img src="../sys_resources/images/up.gif"/>
				</xsl:if>
				<xsl:if test="./@sorted='descending'">
					<img src="../sys_resources/images/down.gif"/>
				</xsl:if>
            <xsl:value-of select="./@label"/>
			</td>
		</xsl:if>
	</xsl:template>
	<xsl:template match="Results">
		<xsl:apply-templates select="Row"/>
	</xsl:template>
	<xsl:template match="Row">
		<xsl:apply-templates mode="categoryColumns" select="Categories/Column"/>
		<tr class="datacell1">
			<td class="datacell1">&nbsp;</td>
			<td class="datacell1">&nbsp;</td>
			
						<td align="left">
							<xsl:choose>
								<xsl:when test="$selectionmode='single'">
									<input type="radio" name="inlineitem">
										<xsl:attribute name="value"><xsl:value-of select="concat(Properties/Property[@name='sys_contentid'],';',Properties/Property[@name='sys_variantid'],';',Properties/Property[@name='sys_siteid'],';',Properties/Property[@name='sys_folderid'])"/></xsl:attribute>
									</input>
									<xsl:element name="script">
										<xsl:attribute name="language">javascript</xsl:attribute>
										<xsl:text>var urlstring_</xsl:text>
										<xsl:value-of select="position()"/>
										<xsl:text> = "</xsl:text>
										<xsl:choose>
											<xsl:when test="//PassThroughParameters/Parameter[@name='inlinetype']/Value='rxvariant'">
												<xsl:value-of select="Properties/Property[@name='sys_assemblyUrlInt']"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="Properties/Property[@name='sys_assemblyUrl']"/>
											</xsl:otherwise>
										</xsl:choose>
										<xsl:text>";</xsl:text>
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<input type="checkbox" name="conidvarid">
										<xsl:attribute name="value"><xsl:value-of select="concat(Properties/Property[@name='sys_contentid'],';',Properties/Property[@name='sys_variantid'],';',Properties/Property[@name='sys_siteid'],';',Properties/Property[@name='sys_folderid'])"/></xsl:attribute>
									</input>
								</xsl:otherwise>
							</xsl:choose>
						</td>
						<xsl:apply-templates mode="columns" select="Column"/>
					
		</tr>
	</xsl:template>
	<xsl:template match="Column" mode="categoryColumns">
		<xsl:apply-templates mode="categoryColumn" select="."/>
	</xsl:template>
	<xsl:template match="Column" mode="columns">
		<xsl:choose>
			<xsl:when test="./JavaScript">
				<xsl:apply-templates mode="linkColumn" select=".">
					<xsl:with-param name="columnIndex" select="position()"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="column" select=".">
					<xsl:with-param name="columnIndex" select="position()"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="Column" mode="categoryColumn">
		<xsl:param name="categoryLevel" select="./@categoryLevel"/>
		<tr class="datacell1">
			<td colspan="9" class="headercellfont" align="left">
				<img src="../sys_resources/images/spacer.gif">
					<xsl:attribute name="width"><xsl:value-of select="$categoryLevel * $tabsize"/></xsl:attribute>
				</img>
				<xsl:value-of select="./Value"/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="Column" mode="linkColumn">
		<xsl:param name="columnIndex"/>
		<xsl:variable name="width" select="/*/Header/HeaderColumn[@isCategory = 'no'][position()=$columnIndex]/@width"/>
		<td class="datacell1font" align="left">
			<xsl:if test="$width">
				<xsl:attribute name="width"><xsl:value-of select="$width"/></xsl:attribute>
			</xsl:if>
			<a>
				<xsl:attribute name="href">javascript:{}</xsl:attribute>
				<xsl:attribute name="onclick"><xsl:value-of select="./JavaScript"/></xsl:attribute>
				<xsl:value-of select="./Value"/>
			</a>
		</td>
	</xsl:template>
	<xsl:template match="Column" mode="column">
		<xsl:param name="columnIndex"/>
		<xsl:choose>
			<xsl:when test="/*/Header/HeaderColumn[@isCategory='no'][position()=$columnIndex]/@type='Image'">
				<xsl:apply-templates mode="imageColumn" select=".">
					<xsl:with-param name="columnIndex" select="$columnIndex"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="textColumn" select=".">
					<xsl:with-param name="columnIndex" select="$columnIndex"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="Column" mode="imageColumn">
		<xsl:param name="columnIndex"/>
		<xsl:variable name="width" select="/*/Header/HeaderColumn[@isCategory = 'no'][position()=$columnIndex]/@width"/>
		<td class="datacell1font" align="left">
			<xsl:if test="$width">
				<xsl:attribute name="width"><xsl:value-of select="$width"/></xsl:attribute>
			</xsl:if>
			<img>
				<xsl:attribute name="src"><xsl:value-of select="./Value"/></xsl:attribute>
			</img>
		</td>
	</xsl:template>
	<xsl:template match="Column" mode="textColumn">
		<xsl:param name="columnIndex"/>
		<xsl:variable name="width" select="/*/Header/HeaderColumn[@isCategory = 'no'][position()=$columnIndex]/@width"/>
		<td class="datacell1font" align="left">
			<xsl:if test="$width">
				<xsl:attribute name="width"><xsl:value-of select="$width"/></xsl:attribute>
			</xsl:if>
			<xsl:value-of select="./Value"/>
		</td>
	</xsl:template>
	<xsl:template match="Actions">
		<td align="middle">
			<xsl:apply-templates select="Action"/>
		</td>
	</xsl:template>
	<xsl:template match="Action">
		<input type="button">
			<xsl:attribute name="name"><xsl:value-of select="./@name"/></xsl:attribute>
			<xsl:attribute name="value"><xsl:value-of select="./@label"/></xsl:attribute>
			<xsl:attribute name="onclick"><xsl:value-of select="./JavaScript"/></xsl:attribute>
		</input>
      &nbsp; &nbsp;
   </xsl:template>
	<psxi18n:lookupkeys>
		<key name="psx.sys_searchSupport.getResults@Search Results">Title for the search results page.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
