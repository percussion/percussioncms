<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n" >
   <xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
   <xsl:variable name="lang" select="//@xml:lang"/>
   <xsl:output method="xml"/>
   <xsl:template match="/">
      <!-- 
         The following code creates a variable called searchdefcomm. 
         If the element BrowserUISettings/SearchSettings/@useCurrentCommunity exists in config.xml file 
         then it assigns its value to this variable otherwise a value of yes is assgined.
      -->
      <xsl:variable name="searchdefcommelem" select="document('../rxconfig/Server/config.xml')/*/BrowserUISettings/SearchSettings/@useCurrentCommunity"/>
      <xsl:variable name="searchdefcomm">
         <xsl:choose>
            <xsl:when test="$searchdefcommelem">
               <xsl:value-of select="$searchdefcommelem"/>
            </xsl:when>
            <xsl:otherwise>yes</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="pagename" select="/*/pagename"/>
      <xsl:variable name="statelookup" select="/*/statelookupurl"/>
      <xsl:variable name="logincommunity" select="/*/logincommunity"/>
      <html>
         <head>
            <title>
               <xsl:call-template name="getLocaleString">
                  <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Content Administar Search Box'"/>
                  <xsl:with-param name="lang" select="$lang"/>
               </xsl:call-template>
            </title>
         </head>
         <body>
            <!--
               The following code generates javascript array variables for community 
               and content type relation ships. When a community is selected in the community
               select box, then the content types select box should display allowed content types
               for the community. If nothing is selected then it should show all the content types.
               This code is needed only when the communities are enabled on the system which can be 
               decided by looking at logincommunity value. If logincommunity value is not equal 0 
               then communities are enabled on the system.
            -->
            <xsl:if test="$logincommunity!=0">
               <script language="javascript" ><![CDATA[
      				function contenttype(id, name, displayname)
      				{
      					 this.id = id;
      					 this.name = name;
      					 this.displayname= displayname;
      				}
      
      				function community(id, name, contenttypes)
      				{
      					 this.id = id;
      					 this.name = name;
      					 this.contenttypes = contenttypes;
      				}
      				]]></script>
               <xsl:variable name="contentlookup" select="document(//contentlookupurl)"/>
               <xsl:variable name="communitycontentlookup" select="document(//communitycontentlookupurl)"/>
               <script language="javascript" src="../sys_resources/js/globalErrorMessages.js">;</script>
               <script language="javascript" src="{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}">;</script>
               <xsl:element name="script">
                  <xsl:attribute name="language">javascript</xsl:attribute>
                  <xsl:text>communities = new Array(</xsl:text>
                  <xsl:for-each select="$communitycontentlookup//community">
                     <xsl:text>new community(</xsl:text>
                     <xsl:value-of select="id"/>
                     <xsl:text>, &quot;</xsl:text>
                     <xsl:value-of select="name"/>
                     <xsl:text>&quot;, new Array(</xsl:text>
                     <xsl:for-each select="contenttype">
                        <xsl:text>new contenttype(</xsl:text>
                        <xsl:value-of select="id"/>
                        <xsl:text>,&quot;</xsl:text>
                        <xsl:value-of select="name"/>
                        <xsl:text>&quot;,&quot;</xsl:text>
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
                     <xsl:value-of select="name"/>
                     <xsl:text>&quot;</xsl:text>
                     <xsl:if test="not(position() = last())">
                        <xsl:text>,</xsl:text>
                     </xsl:if>
                  </xsl:for-each>
                  <xsl:text>);</xsl:text>
                  <xsl:text>allcontenttypesdn = new Array(</xsl:text>
                  <xsl:for-each select="$contentlookup//item">
                     <xsl:text>&quot;</xsl:text>
                     <xsl:value-of select="displayname"/>
                     <xsl:text>&quot;</xsl:text>
                     <xsl:if test="not(position() = last())">
                        <xsl:text>,</xsl:text>
                     </xsl:if>
                  </xsl:for-each>
                  <xsl:text>);</xsl:text>
               </xsl:element>
            </xsl:if>
            <!-- Javascript array variables for communities and content types end here -->
            <!-- 
            Javascript function search_onclick will be called on submit.
            To support searches for strings with BACKENDCOLUMN LIKE psxparam in the mapper.
            We need to add % character before and after the actual value before we submit.
            The function also performs client side validation for dates.
            During customization this function can be modified if needed.
            -->
            <script language="javascript"><![CDATA[
               function search_onclick() {
                  if(document.contentsearch.title.value!="")
               	   document.contentsearch.title.value = '%' + document.contentsearch.title.value + '%';
                  if(document.contentsearch.author.value!="")
               	   document.contentsearch.author.value = '%' + document.contentsearch.author.value + '%';
               	if(!dateValidate(document.contentsearch.enddate.value)){
               		return false;
               	}
               	if(!dateValidate(document.contentsearch.startdate.value)){
               		return false;
               	}
               }
            ]]></script>
            <!-- calPopup javascript file consists of calender control functions.-->
            <script src="../sys_resources/js/calPopup.js" language="javascript">;</script>
            <!-- searchinitjavascript file consists of community aontent type select box related functions.-->
            <script src="../sys_cmpCaSearchBox/searchinit.js" language="javascript">;</script>
            <form name="contentsearch" action="../sys_caContentSearch/search.html" method="get">
               <input type="hidden" name="sys_componentname" value="ca_search">
                  <xsl:if test="$pagename='ca_purge_bystatus'">
                     <xsl:attribute name="value">ca_purge_bystatus</xsl:attribute>
                  </xsl:if>
               </input>
               <input type="hidden" name="searchdefcomm" value="{$searchdefcomm}"/>
               <input type="hidden" name="sys_sortparam" value="title"/>
               <input type="hidden" name="sys_pagename" value="{$pagename}"/>
               <input type="hidden" name="logincommunity" value="{$logincommunity}"/>
               <table width="225" cellpadding="0" cellspacing="0" border="0">
                  <tr class="datacell1" border="0">
                     <td align="left" class="outerboxcellfont" colspan="2">&nbsp;
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Search Existing Content'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                        <hr/>
                     </td>
                  </tr>
                  <!-- Title field starts here-->
                  <tr class="datacell1" border="0">
                     <td align="right" class="headercellfont" width="25%" nowrap="yes">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Title'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td align="left" class="datacell2font" width="75%"> 
			                 &nbsp;
			                 <input type="text" size="16" name="title" class="monospace"/>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td colspan="2" height="10" class="datacell1font"/>
                  </tr>
                  <!-- Title field ends here-->
                  <!-- Content ID field starts here-->
                  <tr class="datacell1" border="0">
                     <td align="right" class="headercellfont">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@ID'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td align="left" class="datacell2font"> 
		                    &nbsp;
		                    <input type="text" size="16" name="statusid" class="monospace"/>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td colspan="2" height="10" class="datacell1font"/>
                  </tr>
                  <!-- Content ID field ends here-->
                  <!-- Community and Content Type fields start here-->
                  <!-- 
                  The following condition has been added here to show community and corresponding 
                  content types select boxes when communities are enabled and only content types 
                  select box when communities are disabled. 
                  -->
                  <xsl:choose>
                     <xsl:when test="$logincommunity!=0">
                        <!-- 
                        When communities are enabled the values for communities and content types 
                        select boxes will be filled dynamically through javascript function onFormLoad().
                        -->
                        <tr class="datacell1" border="0">
                           <td align="right" class="headercellfont">
                              <xsl:call-template name="getLocaleString">
                                 <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Community'"/>
                                 <xsl:with-param name="lang" select="$lang"/>
                              </xsl:call-template>:
                           </td>
                           <td align="left" class="datacell2font">
                              &nbsp;
                              <select name="communityid" onchange="return community_onchange()">
                                 <option>--- Select from list ---</option>
                                 <option>1</option>
                                 <option>2</option>
                                 <option>3</option>
                                 <option>4</option>
                                 <option>5</option>
                              </select>
                           </td>
                        </tr>
                        <tr class="datacell1">
                           <td colspan="2" height="10" class="datacell1font"/>
                        </tr>
                        <tr class="datacell1" border="0">
                           <td align="right" class="headercellfont">
                              <xsl:call-template name="getLocaleString">
                                 <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Content Type'"/>
                                 <xsl:with-param name="lang" select="$lang"/>
                              </xsl:call-template>:
                           </td>
                           <td align="left" class="datacell2font">
                              &nbsp;
                              <select name="ctype">
                                 <option>--- Select from list ---</option>
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
                        <!--
                        This is communities disabled case.
                        -->
                        <tr class="datacell1" border="0">
                           <td align="right" class="headercellfont">
                              <xsl:call-template name="getLocaleString">
                                 <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Content Type'"/>
                                 <xsl:with-param name="lang" select="$lang"/>
                              </xsl:call-template>:
                           </td>
                           <td align="left" class="datacell2font">
                              <xsl:variable name="contentlookup" select="document(/*/contentlookupurl)/*"/>
		                            &nbsp;
		                            <select name="ctype">
                                 <option value=""/>
                                 <xsl:for-each select="$contentlookup/item">
                                    <option value="{name}">
                                       <xsl:value-of select="displayname"/>
                                    </option>
                                 </xsl:for-each>
                              </select>
                           </td>
                        </tr>
                     </xsl:otherwise>
                  </xsl:choose>
                  <tr class="datacell1">
                     <td colspan="2" height="10" class="datacell1font"/>
                  </tr>
                  <!-- Community and Content Type fields end here-->
                  <!-- Status field start here-->
                  <tr class="datacell1" border="0">
                     <td align="right" class="headercellfont">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Status'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td align="left" class="datacell2font"> 
		                    &nbsp;
		                    <select name="status">
                           <option value=""/>
                           <xsl:apply-templates select="document($statelookup)/*" mode="lookup"/>
                        </select>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td colspan="2" height="10" class="datacell1font"/>
                  </tr>
                  <!-- Status field end here-->
                  <!-- Author field start here-->
                  <tr class="datacell1" border="0">
                     <td align="right" class="headercellfont">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Author'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1font" align="left" width="100%"> 
		                   &nbsp;
		                   <input type="text" size="16" name="author" class="monospace"/>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td colspan="2" height="10" class="datacell1font"/>
                  </tr>
                  <!-- Author field end here-->
                  <!-- Start Date Before field start here-->
                  <tr class="datacell1" border="0">
                     <td align="right" class="headercellfont">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Start Date Before'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1font" align="left" width="100%"> 
		                   &nbsp;
		                   <input type="text" name="enddate" size="13" class="monospace"/>
		                   &nbsp;
		                   <a href="javascript:doNothing()" onclick="showCalendar(document.contentsearch.enddate);">
                           <img border="0" height="20" width="20" src="../sys_resources/images/cal.gif">
                              <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox.alt@Calendar Pop-up'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                           </img>
                        </a>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td colspan="2" height="10" class="datacell1font"/>
                  </tr>
                  <!-- Start Date Before field end here-->
                  <!-- Start Date After field start here-->
                  <tr class="datacell1" border="0">
                     <td align="right" class="headercellfont">
                        <xsl:call-template name="getLocaleString">
                           <xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Start Date After'"/>
                           <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>:
                     </td>
                     <td class="datacell1font" align="left"> 
		                   &nbsp;
		                   <input type="text" name="startdate" size="13" class="monospace"/>
		                   &nbsp; 
		                   <a href="javascript:doNothing()" onclick="showCalendar(document.contentsearch.startdate);">
                           <img border="0" height="20" width="20" src="../sys_resources/images/cal.gif">
                              <xsl:attribute name="alt"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox.alt@Calendar Pop-up'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                           </img>
                        </a>
                     </td>
                  </tr>
                  <tr class="datacell1">
                     <td colspan="2" height="10" class="datacell1font"/>
                  </tr>
                  <!-- Start Date After field end here-->
                  <tr class="datacell1">
                     <td align="right" class="datacell1font">&nbsp;</td>
                     <td align="left" class="headercell2font">
                        <br/>&nbsp;
		                   <input type="submit" name="Submit" value="Search" onclick="return search_onclick();">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Search'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>&nbsp;
                        <input type="reset" name="Reset" value="Reset">
                           <xsl:attribute name="value"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="'psx.sys_cmpCaSearchBox.searchbox@Reset'"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
                        </input>
                        <br/>
                     </td>
                  </tr>
               </table>
            </form>
            <!-- 
            onFormLoad function fills the data for communities and content type select boxes. 
            The functions are avaialable in searchinit.js file in the current application.
            The function will be avaialable only when communities are enabled.
            parseAndDisplayFormFiledsFromUrl function resides in formValidation.js file. 
            This function fills the form fields from the url. This function will be avaialable 
            only when the language is US English.
            -->
            <script language="javascript">
               <xsl:if test="$logincommunity!=0">
	               javascript:onFormLoad(); 
	            </xsl:if>
               <xsl:if test="$lang='en-us'">
	               parseAndDisplayFormFiledsFromUrl('contentsearch');
	            </xsl:if>
            </script>
         </body>
      </html>
   </xsl:template>
   <!--
      The following template fills the option tags for content types(communities disabled case) and status
      select boxes.
   -->
   <xsl:template match="*" mode="lookup">
      <xsl:for-each select="item">
         <option value="{name}">
            <xsl:value-of select="displayname"/>
         </option>
      </xsl:for-each>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Content Administrator Search Box">Title for the Content Administrator Search Box component page.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Search Existing Content">Header text for search box in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Title">Title label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@ID">ID label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Community">Community label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Content Type">Content Type label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Status">Status label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Author">Author label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Start Date Before">Start Date Before label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Start Date After">Start Date After label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox.alt@Calendar Pop-up">Alt text for calender image in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Search">Search button label in Content Administrator Search Box component.</key>
      <key name="psx.sys_cmpCaSearchBox.searchbox@Reset">Reset button label in Content Administrator Search Box component.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
