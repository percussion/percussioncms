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
<!--
	This is the main stylesheet for rendering the search dialog box confirming to the SearchQueryDef.dtd.  The logic is similar to that of content editor. It has 
   This stylesheet renders search results conforming to the SearchResults.dtd.
   It has four main parts.
   1. Full Text Query part will be displayed only if the FullTextSearchSettings element exist in the xml document.
2. Search Fields part renders the search fields.
3. Result setting Part renders the display format and max results.
4. Buttons renders the buttons based on the type of search.
-->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
                xmlns:psxi18n="com.percussion.i18n" extension-element-prefixes="psxi18n"
                exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<xsl:import href="file:sys_searchSupport/sys_searchTemplates.xsl"/>
	<xsl:import href="file:sys_searchSupport/sys_searchJSGenerator.xsl"/>
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>
	<xsl:variable name="lang" select="//@xml:lang"/>
	<xsl:variable name="isFullTextSearch" select="/*/FullTextSearchSettings"/>
   <xsl:variable name="generationMode" select="/*/ExtraSettings/DisplayField/Control[@paramName='genMode']/Value"/>
	<xsl:variable name="isSimpleSearch">
		<xsl:choose>
			<xsl:when test="//@searchMode='simple'">yes</xsl:when>
			<xsl:otherwise>no</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="formname" select="'searchQuery'"/>
	<!--Initial Match-->
	<xsl:template match="/">
	   <xsl:choose>
	      <xsl:when test="$generationMode and $generationMode='aaJS'">
	         <xsl:apply-templates select="SearchQueryDef" mode="aaJS"/>
	      </xsl:when>
	      <xsl:otherwise>
	         <xsl:apply-templates select="SearchQueryDef" mode="html"/>
	      </xsl:otherwise>
	   </xsl:choose>
	</xsl:template>
   <xsl:template match="SearchQueryDef" mode="aaJS">
      var psSearch = new function(){};
      <xsl:apply-templates select="/*/ResultSettings/DisplayField/Control[@name='sys_MaxNumberEditBox']" mode="maxNumberJS"/>
      <xsl:apply-templates select="KeywordDependencies" mode="kwjs1"/>
      <xsl:apply-templates select="KeywordDependencies" mode="kwjs2"/>
   </xsl:template>
   <!--Match on root -->
	<xsl:template match="SearchQueryDef" mode="html">
		<html>
			<head>
				<!-- CSS Include section -->
				<link rel="stylesheet" type="text/css" href="../sys_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="../rx_resources/css/templates.css"/>
				<link rel="stylesheet" type="text/css" href="{concat('../rx_resources/css/',$lang,'/templates.css')}"/>
				<!-- Script Include section -->
				<script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
				<script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
				<script src="../sys_resources/js/popmenu.js">;</script>
				<script language="javascript1.2" src="../sys_resources/js/browser.js">;</script>
				<script src="../sys_resources/js/href.js">;</script>
				<script src="../sys_resources/js/calPopup.js">;</script>
			   <xsl:if test="not($generationMode and $generationMode='aaHTML')">
   			   <xsl:call-template name="genericJavascript"/>
			      <xsl:call-template name="progressWindowScript"/>
			      <xsl:call-template name="validateQueryScript"/>
			      <xsl:apply-templates select="/*/ResultSettings/DisplayField/Control[@name='sys_MaxNumberEditBox']" mode="maxNumberJS"/>
			      <xsl:apply-templates select="/*/SearchFields/DisplayField/Control[@name='sys_MaxNumberEditBox']" mode="maxNumberJS"/>
			      <xsl:apply-templates select="KeywordDependencies" mode="kwjs1"/>
			   </xsl:if>
				<!-- Title of the page -->
				<title>
					<xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Search Query'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template>
					<xsl:variable name="inlinetype" select="ExtraSettings/DisplayField/Control[@paramName='inlinetype']/Value"/>
					<xsl:choose>
						<xsl:when test="$inlinetype='rxhyperlink'">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@ - Inline Link'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:when test="$inlinetype='rximage'">
							<xsl:call-template name="getLocaleString">
								<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@ - Inline Image'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:when test="$inlinetype='rxvariant'">
							<xsl:call-template name="getLocaleString">	
								<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@ - Inline Variant'"/>
								<xsl:with-param name="lang" select="$lang"/>
							</xsl:call-template>
						</xsl:when>
					</xsl:choose>
				</title>
			</head>
			<body class="backgroundcolor" topmargin="5" leftmargin="5">
			   <xsl:if test="not($generationMode and $generationMode='aaHTML')">
			      <xsl:attribute name="onload">searchTextAreaChangeHandler(event, this); window.scrollTo(0,0);</xsl:attribute>
			   </xsl:if>
			   <form  encType="multipart/form-data" method="post" name="{$formname}" id="{$formname}">
				   <xsl:if test="not($generationMode and $generationMode='aaHTML')">
				      <xsl:attribute name="onsubmit">return updateSearchMode();</xsl:attribute>
				      <xsl:attribute name="action">getResults.html</xsl:attribute>
				   </xsl:if>
					<input type="hidden" name="sys_searchMode" value="{//@searchMode}"/>
					<table width="100%" border="0" cellpadding="0" cellspacing="0" class="outerboxcell">
						<xsl:choose>
							<!--If full text search then apply FullTextSearchSettings, ResultSettings and SearchFields templates-->
							<xsl:when test="$isFullTextSearch">
								<xsl:apply-templates select="FullTextSearchSettings/Simple"/>
								<xsl:apply-templates select="ResultSettings"/>
							   <xsl:choose>
							      <xsl:when test="$generationMode and $generationMode='aaHTML'">
                              <tr>
                                 <td align="right" colspan="2">
                                    <table>
                                       <tr>
                                          <td width="100%" align="right">
                                             <button style="border: 1px solid black;" dojoType="Button" id="ps.search.advanced"><img src="../sys_resources/images/aa/ChevronsDown16.gif" width="16" height="16" title="Advanced" alt="Advanced"/></button>
                                             <button style="border: 1px solid black;" dojoType="Button" id="ps.search.simple"><img src="../sys_resources/images/aa/ChevronsUp16.gif" width="16" height="16" title="Simple" alt="Simple"/></button>
                                          </td>
                                          <td width="15"><img src="../sys_resources/images/spacer.gif"/></td>
                                       </tr>
                                    </table>
                                 </td>
                              </tr>
							      </xsl:when>
							      <xsl:otherwise>
							         <xsl:call-template name="Buttons">
							            <xsl:with-param name="buttonPosition" select="'1'"/>
							         </xsl:call-template>
							      </xsl:otherwise>
							   </xsl:choose>
   							<xsl:apply-templates select="SearchFields"/>
							   <xsl:if test="not($generationMode and $generationMode='aaHTML')">
   								<xsl:call-template name="Buttons">
   									<xsl:with-param name="buttonPosition" select="'2'"/>
   								</xsl:call-template>
							   </xsl:if>
						   </xsl:when>
							<!--Otherwise apply SearchFields and ResultSettings templates-->
							<xsl:otherwise>
								<xsl:apply-templates select="SearchFields"/>
								<xsl:apply-templates select="ResultSettings"/>
							   <xsl:if test="not($generationMode and $generationMode='aaHTML')">
							      <xsl:call-template name="Buttons">
	   								<xsl:with-param name="buttonPosition" select="'1'"/>
   								</xsl:call-template>
							   </xsl:if>
							</xsl:otherwise>
						</xsl:choose>
						<!-- Apply Extrasettings and Buttons templates irrespective of the search mode-->
						<xsl:apply-templates select="ExtraSettings"/>
					</table>
				   <xsl:if test="not($generationMode and $generationMode='aaHTML')">
				      <xsl:call-template name="helpScript"/>
				      <xsl:call-template name="ftScript"/>
				      <xsl:apply-templates select="KeywordDependencies" mode="kwjs2"/>
				   </xsl:if>
			      <xsl:if test="$generationMode and $generationMode='aaHTML'">
                  	<input type="hidden" name="includeSites" value="no"/>
			         <input type="hidden" name="includeFolders" value="no"/>
			      </xsl:if>
			   </form>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="FullTextSearchSettings/Simple">
		<tr class="headercell">
			<td colspan="2" class="outerboxcellfont">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Simple'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</td>
		</tr>
		<xsl:apply-templates select="DisplayField"/>
	</xsl:template>
	<xsl:template match="ResultSettings">
		<tr class="headercell">
			<td colspan="2" class="outerboxcellfont">
				<xsl:call-template name="getLocaleString">
					<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Result Settings'"/>
					<xsl:with-param name="lang" select="$lang"/>
				</xsl:call-template>
			</td>
		</tr>
	   <xsl:choose>
	      <xsl:when test="$generationMode and $generationMode='aaHTML'">
	         <xsl:apply-templates select="DisplayField[not(Control/@paramName = 'sys_displayformatid')]"/>
	      </xsl:when>
	      <xsl:otherwise>
	         <xsl:apply-templates select="DisplayField"/>
	      </xsl:otherwise>
	   </xsl:choose>
	</xsl:template>
	<xsl:template match="SearchFields">
		<tr class="headercell">
			<td colspan="2" class="outerboxcellfont">
				<div id="advancedfields">
					<table width="100%" border="0" cellpadding="0" cellspacing="0" class="outerboxcell">
						<tr class="headercell">
							<td colspan="2" class="outerboxcellfont">
								<xsl:choose>
									<xsl:when test="$isFullTextSearch">
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Advanced'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:call-template name="getLocaleString">
											<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Search Parameters'"/>
											<xsl:with-param name="lang" select="$lang"/>
										</xsl:call-template>
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
						<xsl:apply-templates select="DisplayField"/>
						<xsl:if test="$isFullTextSearch">
							<tr class="headercell">
								<td colspan="2" class="outerboxcellfont">&nbsp;
							</td>
							</tr>
							<xsl:apply-templates select="../FullTextSearchSettings/Advanced/DisplayField"/>
						</xsl:if>
					</table>
				</div>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="ExtraSettings">
		<xsl:apply-templates select="DisplayField"/>
	</xsl:template>
	<xsl:template name="Buttons">
		<xsl:param name="buttonPosition"/>
		<tr class="headercell">
			<td colspan="2" class="outerboxcellfont">
				<div id="{concat('buttons',$buttonPosition)}">
					<table width="100%" border="0" cellpadding="0" cellspacing="0" class="outerboxcell">
						<tr class="headercell">
							<td colspan="2" class="outerboxcellfont">
								<img src="../sys_resources/images/spacer.gif" height="2"/>
							</td>
						</tr>
						<tr class="headercell">
							<td colspan="2" align="center">
								<input type="button" name="search" id="search" onclick="validateForm()">
									<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Search'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
									<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery.mnemonic.Search@S'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
								</input>&#160;
				<input type="button" name="close" onclick="javascript:window.close()">
									<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Close'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
									<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Close@C'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
								</input>&#160;
				<input type="button" name="help" onclick="invokeHelp();">
				  			<xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic@Help'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
							<xsl:attribute name="accesskey"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.generic.mnemonic.Help@H'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
				</input>&#160;
				<xsl:if test="$isFullTextSearch">
									<input type="button" name="advmode" onclick="javascript:hideAdvanced()">
										<xsl:attribute name="value"><xsl:choose><xsl:when test="$buttonPosition='1'"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Advanced'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:when><xsl:otherwise><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Simple'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:otherwise></xsl:choose></xsl:attribute>
									</input>&#160;
				</xsl:if>
							</td>
						</tr>
					</table>
				</div>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="DisplayField[@displayType='sys_hidden']">
		<xsl:apply-templates select="Control" mode="psxcontrol"/>
	</xsl:template>
	<xsl:template match="DisplayField[@displayType='sys_normal']">
		<xsl:comment>Normal Control</xsl:comment>
		<tr class="datacell1">
			<td class="controlname" width="25%">
				<!-- display an asterisk for required fields -->
				<xsl:if test="Control/@isRequired='yes'">*&#160;</xsl:if>
            <label for="{Control/@paramName}" accesskey="{Control/@accessKey}">
               <xsl:value-of select="DisplayLabel"/>
            </label>
			</td>
			<td>
				<xsl:apply-templates select="Control" mode="psxcontrol"/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="DisplayField">
		<tr>
			<td>
				<b>unmatched display field type: '<xsl:copy-of select="@displayType"/>'</b>
				<br id="Rhythmyx"/>
				<xsl:comment>Unmatched display field</xsl:comment>
				<xsl:copy-of select="."/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="Control[@paramName='sys_fulltextquery' ]" mode="psxcontrol" priority="20">
		<textarea name="{@paramName}" id="searchfor" wrap="soft">
         <xsl:if test="not($generationMode and $generationMode='aaHTML')">
            <xsl:attribute name="onkeypress">searchTextAreaChangeHandler(event, this);</xsl:attribute>
            <xsl:attribute name="onblur">searchTextAreaChangeHandler(event, this);</xsl:attribute>
         </xsl:if>
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'sys_TextArea'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
			<xsl:value-of select="Value"/>
		</textarea>
	</xsl:template>
   <xsl:template name="genericJavascript">
      <!-- Javascript function to hide or show the Advanced section -->
         <script>
					var psSearch = new function(){};
					var searchMode = '<xsl:value-of select="@searchMode"/>';
					var canSubmit = true;
					var isFtsSearch = false;
					<xsl:if test="$isFullTextSearch">
					   isFtsSearch = true;
					</xsl:if>
         
					<![CDATA[
					if(searchMode == "simple")
					   canSubmit = false;
					   
					
					function hideAdvanced()
					{
						if(searchMode ==  "advanced")
						{
							PSHideObj("advancedfields");
							PSShowObj("buttons1");
							PSHideObj("buttons2");
							searchMode = "simple";
						}
						else
						{
							PSShowObj("advancedfields");
							PSHideObj("buttons1");
							PSShowObj("buttons2");
							searchMode = "advanced";
						}
						var theButton = document.getElementById("search");
						var searchForValue = "";
						if(document.searchQuery.sys_fulltextquery)
							searchForValue = document.searchQuery.sys_fulltextquery.value;
						theButton.disabled = searchMode == "simple" && 
						   (searchForValue.length < 1)
                                                canSubmit = !theButton.disabled;
					        window.scrollTo(0,0);
					}
					
					function updateSearchMode()
					{
						
						if(!canSubmit)
						{
						   if(is_safari)
						      noSearchForTextMsg();
						   return false;
						}
						document.searchQuery.sys_searchMode.value = searchMode;
						return true;
					}
					
					function searchTextAreaChangeHandler(e, obj)
					{
					   if(!document.searchQuery.sys_fulltextquery)
					   {
						   return;
					   }
					   
					   var searchForValue = document.searchQuery.sys_fulltextquery.value;
					   var theButton = document.getElementById("search");
					   var code = -1;
					   var charInBuff = 0;
					   var willClear = false;
					  
					   if(e.type == "keypress")
					   {
					      if(is_ie)
					         code = e.keyCode;
					      else					      
					         code = e.which;
					         
					      if(code == undefined)
					         code == -1;
					    					    
					      
					      if((code > 32 && code < 127))
					         charInBuff = 1;				      
					   }
					   
					   theButton.disabled = searchMode == "simple" && 
					      (searchForValue.length + charInBuff < 1);
                                           canSubmit = !theButton.disabled
					}
					
					function noSearchForTextMsg()
					{
					   alert(LocalizedMessage('noSearchForTextMsg'));
					}
					]]>
         </script>
   </xsl:template>
   <xsl:template name="progressWindowScript">
      <script>
					function displayProgressWindow()
					{
					<![CDATA[
					  var winHeight = 150;
					  var winWidth = 200;
					  var winL = (screen.width - winWidth) / 2;
					  var winT = (screen.height - winHeight) / 2;
					  var winAttribs = "left=" + winL + "; top=" + winT + "; height=" + winHeight + "; width=" + winWidth + "; scrollbars=0; toolbar=0; status=0; resizable=0"
					  var progressWindow = window.open("", 'progressWin', winAttribs);
					  progressWindow.resizeTo(winWidth, winHeight);					  
					  progressWindow.moveTo(winL, winT);
					  var doc = progressWindow.document;
					  var msg = "]]><xsl:call-template name="getLocaleString">
						<xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@Searching. Please wait...'"/>
						<xsl:with-param name="lang" select="$lang"/>
					</xsl:call-template><![CDATA[";
					  doc.open();
					  doc.writeln("<html><head>");
					  doc.writeln("<title>" + msg + "</title></head><body bgcolor='#CEDDFF' onblur='self.focus()'>");
					  doc.writeln("<table width='100%' height='100%' cellspacing='0'><tr><td valign='middle' align='center'>");
					  doc.writeln("<p>" + msg + "</p>");
					  doc.writeln("<img src='../sys_resources/images/wait.gif' border='1'>");
					  doc.writeln("</td></tr></table></body></html>");
					  doc.close();
					  					  
					]]>}					
      </script>
   </xsl:template>
    <xsl:template name="validateQueryScript">
      <script>
               function validateForm()
               {
                  if (!validateQueryForSynonymExp())
                     return;
                  if (!validateQuery())
                     return;

                  document.searchQuery.submit();
               }

               function validateQuery()
               {
                  if (!document.searchQuery.sys_fulltextquery)
                     return true;

                  var searchForValue = document.searchQuery.sys_fulltextquery.value;
                  var firstChar = searchForValue.charAt(0);
                  if (firstChar == "*" || firstChar == "?")
                  {
                     var msg = "<xsl:call-template name="getLocaleString">
                                <xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@The following characters are not allowed as the first character of a full text search query: *, ?'"/>
                                <xsl:with-param name="lang" select="$lang"/>
                               </xsl:call-template>";
                     alert(msg);
                     return false;
                  }
                  else
                  {
                     displayProgressWindow();
                     return true;
                  }
               }

               function validateQueryForSynonymExp()
               {
               <![CDATA[
                  if (!document.searchQuery.sys_synonymexpansion)
                     return true;

                  var synonymExp = document.searchQuery.sys_synonymexpansion.checked;
                  if (synonymExp)
                  {                   
                     var searchForValue = document.searchQuery.sys_fulltextquery.value;
                     var spChars = "";
                     var specialChars = new Array("+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\\");
                     for (var i = 0; i < specialChars.length; i++)
                     {
                        var spChar = specialChars[i];
                        if (searchForValue.indexOf(spChar) != -1)
                        {
                           if (spChars.length == 0)
                              spChars = spChar;
                           else
                              spChars += ", " + spChar;
                        }
                     }
               
                     if (spChars.length > 0)
                     {
                        var msg = "]]><xsl:call-template name="getLocaleString">
                                      <xsl:with-param name="key" select="'psx.sys_searchSupport.getQuery@The following characters are not allowed as part of a full text search query when synonym expansion is enabled:'"/>
                                      <xsl:with-param name="lang" select="$lang"/>
                                     </xsl:call-template><![CDATA[";
                        alert(msg + " " + spChars);
                        return false;
                     }
                  }

                  return true;
               ]]>}
       </script>
    </xsl:template>
   <xsl:template name="helpScript">
      <xsl:variable name="helpIcon" select="concat('../rx_resources/images/',$lang,'/help_icon.gif')"/>
      <xsl:variable name="helpAlt">
         <xsl:call-template name="getLocaleString">
            <xsl:with-param name="key" select="'psx.sys_cmpHelp.help.alt@Help'"/>
            <xsl:with-param name="lang" select="$lang"/>
         </xsl:call-template>
      </xsl:variable>
      <script language="javaScript1.2" src="../sys_resources/js/browser.js">;</script>
      <script language="JavaScript1.2">
         var helpSetFile = "../../Docs/Rhythmyx/Business_Users/Content_Explorer_Help.hs";
         var helpIcon = "<xsl:value-of select="$helpIcon"/>";
         var helpAlt = "<xsl:value-of select="$helpAlt"/>";
      </script>
      <script language="JavaScript1.2"><![CDATA[  
					   
					var FtsAdvancedHelpTopicId = "O12349";
					var FtsSimpleHelpTopicId = "O12347";
					var DBAdvancedHelpTopicId = "O8925";
					var DBSimpleHelpTopicId = "O8926";
					function invokeHelp()
					{
					   if(isFtsSearch)
					   {
					      if(searchMode == "simple")
					      {
					         _showHelp(FtsSimpleHelpTopicId);
					      }
					      else
					      {
					         _showHelp(FtsAdvancedHelpTopicId);
					      }
					   }
					   else
					   {
					      if(searchMode == "simple")
					      {
					         _showHelp(DBSimpleHelpTopicId);
					      }
					      else
					      {
					         _showHelp(DBAdvancedHelpTopicId);
					      }					   
					   }
					}
					

        var _codebase = "]]><xsl:value-of select="//@codebase"/><![CDATA[";
        var _classid = "]]><xsl:value-of select="//@classid"/><![CDATA[";
        var _type = "]]><xsl:value-of select="concat('application/x-java-applet;',//@version_type,'=',//@implementation_version)"/><![CDATA[";
        var _pluginpage = "]]><xsl:value-of select="concat('http://java.sun.com/products/plugin/',//@implementation_version,'/plugin-install.html')"/><![CDATA[";
        
        var appletCaller = new AppletCaller();
        
        appletCaller.addParam("name", "help");
        appletCaller.addParam("id", "help");
        appletCaller.addParam("width", "0");
        appletCaller.addParam("height", "0");
        appletCaller.addParam("align", "baseline");
        appletCaller.addParam("codebase", "../sys_resources/AppletJars");
        appletCaller.addParam("archive", "help.jar,jh.jar");
        appletCaller.addParam("code", "com.percussion.tools.help.PSHelpApplet");
        appletCaller.addParam("MAYSCRIPT", "true");
        appletCaller.addParam("classid", _classid);
        appletCaller.addParam("codebaseattr", _codebase);
        appletCaller.addParam("type", _type);
        appletCaller.addParam("scriptable", "true");
        appletCaller.addParam("pluginspage", _pluginpage);
        appletCaller.addParam("helpset_file", helpSetFile);
        appletCaller.show();         
        ]]></script>
   </xsl:template>
   <xsl:template match="Control[@name='sys_MaxNumberEditBox']" mode="maxNumberJS">
      <xsl:call-template name="StartScript"/>
         psSearch.modifyMaxNumber = function()
         {
            document.<xsl:value-of select="$formname"/>.<xsl:value-of select="@paramName"/>.value = -1;
         }
      <xsl:call-template name="EndScript"/>
   </xsl:template>
   <xsl:template name="ftScript">
      <!-- If the search mode is simple initially call the hideAdvanced function to hide the Advanced Section -->
					<xsl:if test="$isFullTextSearch">
						<script>
						if(searchMode ==  "simple")
						{
							PSHideObj("advancedfields");
							PSHideObj("buttons2");
						}
						else
						{
							PSHideObj("buttons1");
						}
					</script>
					</xsl:if>
   </xsl:template>
   <psxi18n:lookupkeys>
		<key name="psx.sys_searchSupport.getQuery@Search Query">Title for the search dialog box.</key>
		<key name="psx.sys_searchSupport.getQuery@ - Inline Link">Append link title text for the search dialog box.</key>
		<key name="psx.sys_searchSupport.getQuery@ - Inline Image">Append image title text for the search dialog box.</key>
		<key name="psx.sys_searchSupport.getQuery@ - Inline Variant">Append variant title text for the search dialog box.</key>
		<key name="psx.sys_searchSupport.getQuery.mnemonic.Search@S">Mnemonic key for search button.</key>
		<key name="psx.sys_searchSupport.getQuery@Simple">Label for the simple section of search box</key>
		<key name="psx.sys_searchSupport.getQuery@Advanced">Label for the advanced section of search box</key>
		<key name="psx.sys_searchSupport.getQuery.mnemonic.Mode@M">Mnemonic key for changing the search dialog from advanced to simple and simple to advanced button.</key>
		<key name="psx.sys_searchSupport.getQuery@Result Settings">Label for the result settings section of search box</key>
		<key name="psx.sys_searchSupport.getQuery@Search Parameters">Label for the search parameters section of search box.</key>
		<key name="psx.sys_searchSupport.getQuery@Search Parameters">Label for the search parameters section of search box.</key>
		<key name="psx.sys_searchSupport.getQuery@Searching. Please wait...">Progress screen message</key>
        <key name="psx.sys_searchSupport.getQuery@The following characters are not allowed as the first character of a full text search query: *, ?">Invalid first character message for fts query</key>
	    <key name="psx.sys_searchSupport.getQuery@The following characters are not allowed as part of a full text search query when synonym expansion is enabled:">Invalid character message for fts query with synonym expansion</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
