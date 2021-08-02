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
   <xsl:variable name="testcondition" select="'((not($related/../@relateditemid) and not($related/../@sys_activeitemid) and not($related/../@relateditemid and not($related/../@sys_activeitemid))) or $related/../@sys_activeitemid=$related/../@relateditemid)'"/>
   <!-- main template -->
   <xsl:template match="/">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <xsl:template match="@*[name()='psxedit']" mode="copy"/>
   <!-- copy any attribute or template -->
   <xsl:template match="@*|*" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <!-- locate the slot and insert the slot edit link -->
   <xsl:template match="xsl:variable[rxslot[xsl:copy-of[contains(@select,'$related/linkurl')]]]" mode="copy">
      <xsl:variable name="slotname">
         <xsl:choose>
            <!-- Nirvana way -->
            <xsl:when test="rxslot/@slotname">
               <xsl:value-of select="rxslot/@slotname"/>
            </xsl:when>
            <!-- Support the old way too -->
            <xsl:otherwise>
               <xsl:value-of select='substring-before(substring-after(rxslot/xsl:copy-of/@select, "&apos;"), "&apos;")'/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="editslot">
         <xsl:value-of select="rxslot/@psxeditslot"/>
      </xsl:variable>
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
      <xsl:if test="$slotname != '' and $editslot != 'no'">
         <xsl:element name="xsl:if">
            <xsl:attribute name="test"><xsl:value-of select="concat($testcondition, ' and $related/../@type!=&quot;1&quot;')"/></xsl:attribute>
            <div style="background-color:#ffffff;color: #000000;font-family: Arial;font-size: 10pt;font-weight: bold;">
               <xsl:element name="xsl:variable">
                  <xsl:attribute name="name">psxslotname</xsl:attribute>
                  <xsl:attribute name="select">'<xsl:value-of select="concat('psx.slot@',$slotname)"/>'</xsl:attribute>
               </xsl:element>
				<xsl:element name="xsl:variable">
					<xsl:attribute name="name">editauth</xsl:attribute>
					<xsl:attribute name="select">document(concat($related/infourls/@activeiteminfourl, '&amp;sys_activeitemid=',$related/../@relateditemid))/activeitem/@editauthorized</xsl:attribute>
				</xsl:element>
            	<xsl:element name="xsl:variable">
					<xsl:attribute name="name">contentstatus3</xsl:attribute>
					<xsl:attribute name="select">document(concat($related/infourls/@contentstatusurl,'&amp;sys_contentid=',$related/../@sys_contentid))/contentstatus</xsl:attribute>
				</xsl:element>
				<xsl:element name="xsl:variable">
					<xsl:attribute name="name">tmpcheckoutby</xsl:attribute>
					<xsl:attribute name="select">translate($contentstatus3/@checkoutusername, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')</xsl:attribute>
				</xsl:element>
				<xsl:element name="xsl:variable">
					<xsl:attribute name="name">tmpusername</xsl:attribute>
					<xsl:attribute name="select">translate($contentstatus3/@username, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')</xsl:attribute>
				</xsl:element>
               <xsl:element name="xsl:call-template">
                  <xsl:attribute name="name">getLocaleString</xsl:attribute>
                  <xsl:element name="xsl:with-param">
                     <xsl:attribute name="name">key</xsl:attribute>
                     <xsl:attribute name="select">$psxslotname</xsl:attribute>
                  </xsl:element>
                  <xsl:element name="xsl:with-param">
                     <xsl:attribute name="name">lang</xsl:attribute>
                     <xsl:attribute name="select">$lang</xsl:attribute>
                  </xsl:element>
               </xsl:element>
               <xsl:text>&nbsp;</xsl:text>
               <xsl:element name="xsl:apply-templates">
                  <xsl:attribute name="select">$slotactions</xsl:attribute>
                  <xsl:attribute name="mode">mainmenu</xsl:attribute>
                  <xsl:element name="xsl:with-param">
                     <xsl:attribute name="name">sessionid</xsl:attribute>
                     <xsl:attribute name="select">$related/../@pssessionid</xsl:attribute>
                  </xsl:element>
                  <xsl:element name="xsl:with-param">
                     <xsl:attribute name="name">actionsetid</xsl:attribute>
                     <xsl:attribute name="select">'<xsl:value-of select="$slotname"/>'</xsl:attribute>
                  </xsl:element>
                  <xsl:element name="xsl:with-param">
                     <xsl:attribute name="name">contentid</xsl:attribute>
                     <xsl:attribute name="select">$related/../@sys_contentid</xsl:attribute>
                  </xsl:element>
                  <xsl:element name="xsl:with-param">
                     <xsl:attribute name="name">revision</xsl:attribute>
                     <xsl:attribute name="select">document(concat($related/infourls/@contentstatusurl,'&amp;sys_contentid=',$related/../@sys_contentid))/contentstatus/@tiprevision</xsl:attribute>
                  </xsl:element>
                  <xsl:element name="xsl:with-param">
                     <xsl:attribute name="name">variantid</xsl:attribute>
                     <xsl:attribute name="select">@variantid</xsl:attribute>
                  </xsl:element>
               </xsl:element>
				<xsl:element name="xsl:if">
				<xsl:attribute name="test">(not($editauth) and $tmpcheckoutby = $tmpusername) or $editauth = &quot;yes&quot;</xsl:attribute>
               <a onmousedown="PSEnterTopItem('{$slotname}', event)" onmouseout="PSExitTopItem('{$slotname}')" href="javascript:void(0)">
                  <img src="../sys_resources/images/relatedcontent/slot.gif" alt="Actions: {$slotname}" border="0" align="absmiddle">
                     <xsl:element name="xsl:attribute">
                        <xsl:attribute name="name">alt</xsl:attribute>
                        <xsl:text>&nbsp;</xsl:text>
                        <xsl:element name="xsl:call-template">
                           <xsl:attribute name="name">getLocaleString</xsl:attribute>
                           <xsl:element name="xsl:with-param">
                              <xsl:attribute name="name">key</xsl:attribute>
                              <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Actions'</xsl:attribute>
                           </xsl:element>
                           <xsl:element name="xsl:with-param">
                              <xsl:attribute name="name">lang</xsl:attribute>
                              <xsl:attribute name="select">$lang</xsl:attribute>
                           </xsl:element>
                        </xsl:element>
                        <xsl:text>:&nbsp;</xsl:text>
                        <xsl:element name="xsl:call-template">
                           <xsl:attribute name="name">getLocaleString</xsl:attribute>
                           <xsl:element name="xsl:with-param">
                              <xsl:attribute name="name">key</xsl:attribute>
                              <xsl:attribute name="select">$psxslotname</xsl:attribute>
                           </xsl:element>
                           <xsl:element name="xsl:with-param">
                              <xsl:attribute name="name">lang</xsl:attribute>
                              <xsl:attribute name="select">$lang</xsl:attribute>
                           </xsl:element>
                        </xsl:element>
                     </xsl:element>
                  </img>
               </a>
				</xsl:element>	
            </div>
         </xsl:element>
      </xsl:if>
   </xsl:template>
   <!--*-->
   <!-- locate the snippet and insert the item specific links -->
   <!--*-->
   <xsl:template match="xsl:copy-of[contains(@select, 'document(') and contains(@select, 'Value/@current')and contains(@select, 'body')] | xsl:apply-templates[contains(@select, 'document(') and contains(@select, 'Value/@current')and contains(@select, 'body')]" mode="copy">
      <!-- add action links for the child items -->
      <xsl:if test="@select and @select != '' ">
         <xsl:element name="xsl:if">
            <xsl:attribute name="test"><xsl:value-of select="concat('@relateditemid and @relateditemid != &quot;&quot; and ', $testcondition)"/></xsl:attribute>
            <xsl:element name="xsl:variable">
               <xsl:attribute name="name">thename</xsl:attribute>
               <xsl:attribute name="select">document(concat($related/infourls/@activeiteminfourl, '&amp;sys_activeitemid=',@relateditemid))/activeitem/@title</xsl:attribute>
            </xsl:element>
            <a href="javascript:void(0)">
               <xsl:element name="xsl:attribute">
                  <xsl:attribute name="name">onclick</xsl:attribute>
                  <xsl:element name="xsl:value-of">
                     <xsl:attribute name="select">concat('PSActivateItem(',@relateditemid,'); return false')</xsl:attribute>
                  </xsl:element>
               </xsl:element>
               <img src="../sys_resources/images/plus.gif" border="0" align="absmiddle">
                  <xsl:element name="xsl:attribute">
                     <xsl:attribute name="name">alt</xsl:attribute>
                     <xsl:element name="xsl:call-template">
                        <xsl:attribute name="name">getLocaleString</xsl:attribute>
                        <xsl:element name="xsl:with-param">
                           <xsl:attribute name="name">key</xsl:attribute>
                           <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Activate'</xsl:attribute>
                        </xsl:element>
                        <xsl:element name="xsl:with-param">
                           <xsl:attribute name="name">lang</xsl:attribute>
                           <xsl:attribute name="select">$lang</xsl:attribute>
                        </xsl:element>
                     </xsl:element>
                     <xsl:text>&nbsp;-&nbsp;</xsl:text>
                     <xsl:element name="xsl:value-of">
                        <xsl:attribute name="select">$thename</xsl:attribute>
                     </xsl:element>
                  </xsl:element>
               </img>
            </a>
            <!-- 
				<xsl:element name="xsl:apply-templates">
					<xsl:attribute name="select">$itemactions</xsl:attribute>
					<xsl:attribute name="mode">mainmenu</xsl:attribute>
					<xsl:element name="xsl:with-param">
						<xsl:attribute name="name">sessionid</xsl:attribute>
						<xsl:attribute name="select">$related/../@pssessionid</xsl:attribute>
					</xsl:element>
					<xsl:element name="xsl:with-param">
						<xsl:attribute name="name">actionsetid</xsl:attribute>
						<xsl:attribute name="select">@relateditemid</xsl:attribute>
					</xsl:element>
					<xsl:element name="xsl:with-param">
						<xsl:attribute name="name">contentid</xsl:attribute>
						<xsl:attribute name="select">@contentid</xsl:attribute>
					</xsl:element>
					<xsl:element name="xsl:with-param">
						<xsl:attribute name="name">revision</xsl:attribute>
						<xsl:attribute name="select">document(concat($related/infourls/@contentstatusurl,'&amp;sys_contentid=',@contentid))/contentstatus/@tiprevision</xsl:attribute>
					</xsl:element>
					<xsl:element name="xsl:with-param">
						<xsl:attribute name="name">variantid</xsl:attribute>
						<xsl:attribute name="select">@variantid</xsl:attribute>
					</xsl:element>
				</xsl:element>
				<a class="PSmenuitem" href="javascript:void(0)">
					<xsl:element name="xsl:attribute">
						<xsl:attribute name="name">onmousedown</xsl:attribute>
						<xsl:element name="xsl:value-of">
							<xsl:attribute name="select">concat(&quot;PSEnterTopItem(&quot;, @relateditemid, &quot;, event)&quot;)</xsl:attribute>
						</xsl:element>
					</xsl:element>
					<xsl:element name="xsl:attribute">
						<xsl:attribute name="name">onmouseout</xsl:attribute>
						<xsl:element name="xsl:value-of">
							<xsl:attribute name="select">concat(&quot;PSExitTopItem(&quot;, @relateditemid, &quot;, event)&quot;)</xsl:attribute>
						</xsl:element>
					</xsl:element>
					<img src="../sys_resources/images/trianglegray.gif" border="0" align="absmiddle">
						<xsl:element name="xsl:attribute">
							<xsl:attribute name="name">alt</xsl:attribute>
							<xsl:text>Actions: </xsl:text>
							<xsl:element name="xsl:value-of">
								<xsl:attribute name="select">$thename</xsl:attribute>
							</xsl:element>
						</xsl:element>
					</img>
				</a>
-->
         </xsl:element>
      </xsl:if>
      <!-- copy the element and attributes -->
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <!-- change the included file 'sys_Slots.xsl' to 'edit/sys_Slots.xsl' -->
   <xsl:template match="xsl:include[contains(@href, 'sys_Slots.xsl')] | xsl:import[contains(@href, 'sys_Slots.xsl')]" mode="copy">
      <xsl:copy>
         <xsl:apply-templates mode="copy"/>
         <xsl:attribute name="href"><xsl:value-of select="concat(substring-before(@href, 'sys_Slots.xsl'), 'edit/sys_Slots.xsl')"/></xsl:attribute>
      </xsl:copy>
   </xsl:template>
   <!-- change the included file 'rx_Slots.xsl' to 'edit/rx_Slots.xsl' -->
   <xsl:template match="xsl:include[contains(@href, 'rx_Slots.xsl')]" mode="copy">
      <xsl:copy>
         <xsl:apply-templates mode="copy"/>
         <xsl:attribute name="href"><xsl:value-of select="concat(substring-before(@href, 'rx_Slots.xsl'), 'edit/rx_Slots.xsl')"/></xsl:attribute>
      </xsl:copy>
   </xsl:template>
   <!-- change the included file 'rx_Slots.xsl' to 'edit/rx_Slots.xsl' and add the reference to the stylesheet to generate the javascript -->
   <xsl:template match="xsl:import[contains(@href, 'rx_Slots.xsl')]" mode="copy">
      <xsl:copy>
         <xsl:apply-templates mode="copy"/>
         <xsl:attribute name="href"><xsl:value-of select="concat(substring-before(@href, 'rx_Slots.xsl'), 'edit/rx_Slots.xsl')"/></xsl:attribute>
      </xsl:copy>
      <xsl:element name="xsl:import">
         <xsl:attribute name="href"><xsl:value-of select="'file:sys_resources/stylesheets/assemblers/sys_popmenu.xsl'"/></xsl:attribute>
      </xsl:element>
      <xsl:element name="xsl:import">
         <xsl:attribute name="href"><xsl:value-of select="'file:sys_resources/stylesheets/assemblers/sys_wfActions.xsl'"/></xsl:attribute>
      </xsl:element>
      <xsl:element name="xsl:import">
         <xsl:attribute name="href"><xsl:value-of select="'file:sys_resources/stylesheets/assemblers/sys_breadcrumb.xsl'"/></xsl:attribute>
      </xsl:element>
      <xsl:element name="xsl:import">
         <xsl:attribute name="href"><xsl:value-of select="'file:sys_resources/stylesheets/sys_I18nUtils.xsl'"/></xsl:attribute>
      </xsl:element>
      <xsl:element name="xsl:import">
         <xsl:attribute name="href"><xsl:value-of select="'file:rx_resources/stylesheets/assemblers/rx_popmenu.xsl'"/></xsl:attribute>
      </xsl:element>
   </xsl:template>
   <!-- change the included/imported file 'sys_Globals.xsl' to 'edit/sys_Globals.xsl' -->
   <xsl:template match="xsl:include[contains(@href, 'sys_Globals.xsl')] | xsl:import[contains(@href, 'sys_Globals.xsl')]" mode="copy">
      <xsl:copy>
         <xsl:apply-templates mode="copy"/>
         <xsl:attribute name="href"><xsl:value-of select="concat(substring-before(@href, 'sys_Globals.xsl'), 'edit/sys_Globals.xsl')"/></xsl:attribute>
      </xsl:copy>
   </xsl:template>
   <!-- change the included/imported file 'rx_Globals.xsl' to 'edit/rx_Globals.xsl' -->
   <xsl:template match="xsl:include[contains(@href, 'rx_Globals.xsl')] | xsl:import[contains(@href, 'rx_Globals.xsl')]" mode="copy">
      <xsl:copy>
         <xsl:apply-templates mode="copy"/>
         <xsl:attribute name="href"><xsl:value-of select="concat(substring-before(@href, 'rx_Globals.xsl'), 'edit/rx_Globals.xsl')"/></xsl:attribute>
      </xsl:copy>
   </xsl:template>

   <!-- change the included/imported file 'sys_GlobalTemplates.xsl' to 'edit/sys_GlobalTemplates.xsl' -->
   <xsl:template match="xsl:include[contains(@href, 'sys_GlobalTemplates.xsl')] | xsl:import[contains(@href, 'sys_GlobalTemplates.xsl')]" mode="copy">
      <xsl:copy>
         <xsl:apply-templates mode="copy"/>
         <xsl:attribute name="href"><xsl:value-of select="concat(substring-before(@href, 'sys_GlobalTemplates.xsl'), 'edit/sys_GlobalTemplates.xsl')"/></xsl:attribute>
      </xsl:copy>
   </xsl:template>

   <!-- change the included/imported file 'rx_GlobalTemplates.xsl' to 'edit/rx_GlobalTemplates.xsl' -->
   <xsl:template match="xsl:include[contains(@href, 'rx_GlobalTemplates.xsl')] | xsl:import[contains(@href, 'rx_GlobalTemplates.xsl')]" mode="copy">
      <xsl:copy>
         <xsl:apply-templates mode="copy"/>
         <xsl:attribute name="href"><xsl:value-of select="concat(substring-before(@href, 'rx_GlobalTemplates.xsl'), 'edit/rx_GlobalTemplates.xsl')"/></xsl:attribute>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="xsl:variable[contains(@name, 'related')]" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
      <xsl:element name="xsl:variable">
         <xsl:attribute name="name">lang</xsl:attribute>
         <xsl:attribute name="select">/*/@xml:lang</xsl:attribute>
      </xsl:element>
   </xsl:template>
   <!-- add references to the popupmenu javascript and stylesheet files -->
   <xsl:template match="head" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
         <link rel="stylesheet" type="text/css" href="../sys_resources/css/popmenu.css"/>
         <script src="../sys_resources/js/globalErrorMessages.js">;</script>
         <xsl:element name="script">
         	<xsl:attribute name="type">text/javascript</xsl:attribute>
         	<xsl:attribute name="src">{concat('../rx_resources/js/',$lang,'/globalErrorMessages.js')}</xsl:attribute>
         	<xsl:text>;</xsl:text>
         </xsl:element>        
         <script language="javascript1.2" src="../sys_resources/js/browser.js">;</script>
         <script language="javascript" src="../sys_resources/js/href.js">;</script>
         <script language="javascript" src="../sys_resources/js/popmenu.js">;</script>
         <script language="javascript" src="../rx_resources/js/popmenu.js">;</script>
      </xsl:copy>
   </xsl:template>
   <!-- add references to the popupmenu javascript and stylesheet files -->
   <xsl:template match="body" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:element name="xsl:if">
            <xsl:attribute name="test">$related/../@outputformat=1</xsl:attribute>
            <xsl:element name="xsl:variable">
               <xsl:attribute name="name">contentstatus1</xsl:attribute>
               <xsl:attribute name="select">document(concat($related/infourls/@contentstatusurl,'&amp;sys_contentid=',$related/../@sys_contentid))/contentstatus</xsl:attribute>
            </xsl:element>
            <xsl:element name="xsl:variable">
               <xsl:attribute name="name">parentactions</xsl:attribute>
               <xsl:attribute name="select">document(concat($related/infourls/@actionlisturl, '&amp;sys_uicontext=Parent'))/*</xsl:attribute>
            </xsl:element>
            <xsl:element name="xsl:apply-templates">
               <xsl:attribute name="select">$parentactions</xsl:attribute>
               <xsl:attribute name="mode">mainmenu</xsl:attribute>
               <xsl:element name="xsl:with-param">
                  <xsl:attribute name="name">sessionid</xsl:attribute>
                  <xsl:attribute name="select">$related/../@pssessionid</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:with-param">
                  <xsl:attribute name="name">actionsetid</xsl:attribute>
                  <xsl:attribute name="select">'Parent'</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:with-param">
                  <xsl:attribute name="name">contentid</xsl:attribute>
                  <xsl:attribute name="select">$related/../@sys_contentid</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:with-param">
                  <xsl:attribute name="name">revision</xsl:attribute>
                  <xsl:attribute name="select">$contentstatus1/@tiprevision</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:with-param">
                  <xsl:attribute name="name">contentvalid</xsl:attribute>
                  <xsl:attribute name="select">$contentstatus1/@contentvalid</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:with-param">
                  <xsl:attribute name="name">itemcommunity</xsl:attribute>
                  <xsl:attribute name="select">$contentstatus1/@itemcommunity</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:with-param">
                  <xsl:attribute name="name">usercommunity</xsl:attribute>
                  <xsl:attribute name="select">$contentstatus1/@usercommunity</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:with-param">
                  <xsl:attribute name="name">variantid</xsl:attribute>
                  <xsl:attribute name="select">$related/../@sys_variantid</xsl:attribute>
               </xsl:element>
            </xsl:element>
            <!-- top floating navigation bar -->
            <xsl:element name="xsl:variable">
               <xsl:attribute name="name">previewMode</xsl:attribute>
               <xsl:attribute name="select">$related/../@sys_activeitemid and $related/../@sys_activeitemid = ''</xsl:attribute>
            </xsl:element>
            <div id="PSTopNavBar" style="position:absolute;left:0px;top:0px;z-index:500">
               <table class="PStopnavbar" width="100%" height="26" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                     <td width="1%" align="center">
                        <!-- parent menu item goes here -->
                        <xsl:element name="xsl:choose">
                           <xsl:element name="xsl:when">
                              <xsl:attribute name="test">not($previewMode) and not($related/../@sys_activeitemid)</xsl:attribute>
                              <a class="PSnavmenuitem" href="javascript:void(0)" onMouseDown="PSEnterTopItem('Parent', event)" onMouseOut="PSExitTopItem('Parent')">
                                 <img src="../sys_resources/images/triangleblue.gif" border="0" align="absmiddle">
                                    <xsl:element name="xsl:attribute">
                                       <xsl:attribute name="name">alt</xsl:attribute>
                                       <xsl:text>&nbsp;</xsl:text>
                                       <xsl:element name="xsl:call-template">
                                          <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                          <xsl:element name="xsl:with-param">
                                             <xsl:attribute name="name">key</xsl:attribute>
                                             <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Actions'</xsl:attribute>
                                          </xsl:element>
                                          <xsl:element name="xsl:with-param">
                                             <xsl:attribute name="name">lang</xsl:attribute>
                                             <xsl:attribute name="select">$lang</xsl:attribute>
                                          </xsl:element>
                                       </xsl:element>
                                       <xsl:text>:&nbsp;</xsl:text>
                                       <xsl:element name="xsl:value-of">
                                          <xsl:attribute name="select">$contentstatus1/@title</xsl:attribute>
                                       </xsl:element>
                                       <!-- the following two lines are to put a newline between the data pieces -->
                                       <xsl:element name="xsl:variable">
                                          <xsl:attribute name="name">UserAgent</xsl:attribute>
                                          <xsl:attribute name="select">$related/infourls/@UserAgent</xsl:attribute>
                                       </xsl:element>
                                       <xsl:element name="xsl:if">
                                          <xsl:attribute name="test">contains($UserAgent,'MSIE')</xsl:attribute>
                                          <xsl:text>&nbsp;
</xsl:text>
                                       </xsl:element>
                                       <xsl:element name="xsl:choose">
                                          <xsl:element name="xsl:when">
                                             <xsl:attribute name="test">$contentstatus1/@checkoutusername = ''</xsl:attribute>
                                             <xsl:text>&nbsp;</xsl:text>
                                             <xsl:element name="xsl:call-template">
                                                <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                                <xsl:element name="xsl:with-param">
                                                   <xsl:attribute name="name">key</xsl:attribute>
                                                   <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Not Checked Out'</xsl:attribute>
                                                </xsl:element>
                                                <xsl:element name="xsl:with-param">
                                                   <xsl:attribute name="name">lang</xsl:attribute>
                                                   <xsl:attribute name="select">$lang</xsl:attribute>
                                                </xsl:element>
                                             </xsl:element>
                                             <xsl:text>:&nbsp;</xsl:text>
                                          </xsl:element>
                                          <xsl:element name="xsl:when">
                                             <xsl:attribute name="test">translate($contentstatus1/@checkoutusername, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ') = translate($contentstatus1/@username, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')</xsl:attribute>
                                             <xsl:text>&nbsp;</xsl:text>
                                             <xsl:element name="xsl:call-template">
                                                <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                                <xsl:element name="xsl:with-param">
                                                   <xsl:attribute name="name">key</xsl:attribute>
                                                   <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Checked out by: me'</xsl:attribute>
                                                </xsl:element>
                                                <xsl:element name="xsl:with-param">
                                                   <xsl:attribute name="name">lang</xsl:attribute>
                                                   <xsl:attribute name="select">$lang</xsl:attribute>
                                                </xsl:element>
                                             </xsl:element>
                                             <xsl:text>&nbsp;</xsl:text>
                                          </xsl:element>
                                          <xsl:element name="xsl:otherwise">
                                             <xsl:text>&nbsp;</xsl:text>
                                             <xsl:element name="xsl:call-template">
                                                <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                                <xsl:element name="xsl:with-param">
                                                   <xsl:attribute name="name">key</xsl:attribute>
                                                   <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Checked out by'</xsl:attribute>
                                                </xsl:element>
                                                <xsl:element name="xsl:with-param">
                                                   <xsl:attribute name="name">lang</xsl:attribute>
                                                   <xsl:attribute name="select">$lang</xsl:attribute>
                                                </xsl:element>
                                             </xsl:element>
                                             <xsl:text>:&nbsp;</xsl:text>
                                             <xsl:element name="xsl:value-of">
                                                <xsl:attribute name="select">$contentstatus1/@checkoutusername</xsl:attribute>
                                             </xsl:element>
                                          </xsl:element>
                                       </xsl:element>
                                    </xsl:element>
                                 </img>
                              </a>
                           </xsl:element>
                           <xsl:element name="xsl:otherwise">
                              <img class="PSnavmenuitem" src="../sys_resources/images/spacer.gif" border="0" align="absmiddle" width="21" height="19"/>
                           </xsl:element>
                        </xsl:element>
                     </td>
                     <td align="left" nowrap="true">
                        <!-- bread crumb navigation goes here -->
                        <xsl:element name="xsl:if">
                           <xsl:attribute name="test">not($previewMode)</xsl:attribute>
                           <span class="PSbreadcrumb">&nbsp;</span>
                           <xsl:element name="xsl:choose">
                              <xsl:element name="xsl:when">
                                 <xsl:attribute name="test">not($related/../@sys_activeitemid)</xsl:attribute>
                                 <span class="PSbreadcrumb">
                                    <b>
                                       <xsl:element name="xsl:value-of">
                                          <xsl:attribute name="select">$contentstatus1/@title</xsl:attribute>
                                       </xsl:element>
                                    </b>
                                 </span>
                              </xsl:element>
                              <xsl:element name="xsl:otherwise">
                                 <a class="PSbreadcrumb" href="javascript:void(0)" onclick="PSActivateParent();return false;">
                                    <xsl:element name="xsl:value-of">
                                       <xsl:attribute name="select">$contentstatus1/@title</xsl:attribute>
                                    </xsl:element>
                                 </a>
                                 <xsl:element name="xsl:call-template">
                                    <xsl:attribute name="name">output-breadcrumb</xsl:attribute>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">activeiteminfourl</xsl:attribute>
                                       <xsl:attribute name="select">$related/infourls/@activeiteminfourl</xsl:attribute>
                                    </xsl:element>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">trail</xsl:attribute>
                                       <xsl:attribute name="select">$related/../@sys_trail</xsl:attribute>
                                    </xsl:element>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">list</xsl:attribute>
                                       <xsl:attribute name="select">$related/../@sys_trail</xsl:attribute>
                                    </xsl:element>
                                 </xsl:element>
                                 <span class="PSbreadcrumb">&nbsp;&gt;&nbsp;</span>
                                 <span class="PSbreadcrumb">
                                    <b>
                                       <xsl:element name="xsl:value-of">
                                          <xsl:attribute name="select">document(concat($related/infourls/@activeiteminfourl, '&amp;sys_activeitemid=',$related/../@sys_activeitemid))/activeitem/@title</xsl:attribute>
                                       </xsl:element>
                                    </b>
                                 </span>
                              </xsl:element>
                           </xsl:element>
                        </xsl:element>
								&nbsp;
							</td>
                     <td align="center" width="100%" nowrap="true">
                        <a class="PSnavmenuitem" title="Go to CMS">
                           <xsl:element name="xsl:attribute">
                              <xsl:attribute name="name">href</xsl:attribute>
                              <xsl:text>javascript:OpenCMS('../sys_cx/mainpage.html?sys_debug=');</xsl:text>
                           </xsl:element>
                           <xsl:element name="xsl:variable">
                              <xsl:attribute name="name">tmpuserstatus</xsl:attribute>
                              <xsl:attribute name="select">document($related/infourls/@userstatusurl)/UserStatus</xsl:attribute>
                           </xsl:element>
                           <img src="../sys_resources/images/relatedcontent/smalllogo.gif" border="0" align="absmiddle">
                              <xsl:element name="xsl:attribute">
                                 <xsl:attribute name="name">alt</xsl:attribute>
                                 <xsl:text>&nbsp;</xsl:text>
                                 <xsl:element name="xsl:call-template">
                                    <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">key</xsl:attribute>
                                       <xsl:attribute name="select">'psx.cas.assemblyedit.alt@User'</xsl:attribute>
                                    </xsl:element>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">lang</xsl:attribute>
                                       <xsl:attribute name="select">$lang</xsl:attribute>
                                    </xsl:element>
                                 </xsl:element>
                                 <xsl:text>:&nbsp;</xsl:text>
                                 <xsl:element name="xsl:value-of">
                                    <xsl:attribute name="select">$tmpuserstatus/UserName</xsl:attribute>
                                 </xsl:element>
                                 <!-- the following two lines are to put a newline between the data pieces -->
                                 <xsl:element name="xsl:variable">
                                    <xsl:attribute name="name">UserAgent</xsl:attribute>
                                    <xsl:attribute name="select">$related/infourls/@UserAgent</xsl:attribute>
                                 </xsl:element>
                                 <xsl:element name="xsl:if">
                                    <xsl:attribute name="test">contains($UserAgent,'MSIE')</xsl:attribute>
                                    <xsl:text>&nbsp;
</xsl:text>
                                 </xsl:element>
                                 <xsl:text>&nbsp;</xsl:text>
                                 <xsl:element name="xsl:call-template">
                                    <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">key</xsl:attribute>
                                       <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Role'</xsl:attribute>
                                    </xsl:element>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">lang</xsl:attribute>
                                       <xsl:attribute name="select">$lang</xsl:attribute>
                                    </xsl:element>
                                 </xsl:element>
                                 <xsl:text>:&nbsp;</xsl:text>
                                 <xsl:element name="xsl:for-each">
                                    <xsl:attribute name="select">$tmpuserstatus/UserRoleList/Role</xsl:attribute>
                                    <xsl:element name="xsl:call-template">
                                       <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                       <xsl:element name="xsl:with-param">
                                          <xsl:attribute name="name">key</xsl:attribute>
                                          <xsl:attribute name="select">concat('psx.role@',.)</xsl:attribute>
                                       </xsl:element>
                                       <xsl:element name="xsl:with-param">
                                          <xsl:attribute name="name">lang</xsl:attribute>
                                          <xsl:attribute name="select">$lang</xsl:attribute>
                                       </xsl:element>
                                    </xsl:element>
                                    <xsl:element name="xsl:if">
                                       <xsl:attribute name="test">position()!=last()</xsl:attribute>
                                       <xsl:text>,&nbsp;</xsl:text>
                                    </xsl:element>
                                 </xsl:element>
                              </xsl:element>
                           </img>
                           <span class="PSmenuitem">
                              <xsl:element name="xsl:call-template">
                                 <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                 <xsl:element name="xsl:with-param">
                                    <xsl:attribute name="name">key</xsl:attribute>
                                    <xsl:attribute name="select">'psx.cas.assemblyedit@Rhythmyx'</xsl:attribute>
                                 </xsl:element>
                                 <xsl:element name="xsl:with-param">
                                    <xsl:attribute name="name">lang</xsl:attribute>
                                    <xsl:attribute name="select">$lang</xsl:attribute>
                                 </xsl:element>
                              </xsl:element>
                           </span>
                        </a>
                     </td>
                     <td width="1%" align="right" nowrap="true">
                        <!-- preview / activate button goes here -->
                        <a class="PSnavmenuitem" href="javascript:void(0)">
                           <xsl:element name="xsl:attribute">
                              <xsl:attribute name="name">onclick</xsl:attribute>
                              <xsl:element name="xsl:value-of">
                                 <xsl:attribute name="select">concat('PSTogglePreview(',$previewMode,'); return false')</xsl:attribute>
                              </xsl:element>
                           </xsl:element>
                           <img src="../sys_resources/images/relatedcontent/toggle.gif" border="0" align="absmiddle" alt="Toggle Mode">
                              <xsl:element name="xsl:attribute">
                                 <xsl:attribute name="name">alt</xsl:attribute>
                                 <xsl:element name="xsl:call-template">
                                    <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">key</xsl:attribute>
                                       <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Toggle Mode'</xsl:attribute>
                                    </xsl:element>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">lang</xsl:attribute>
                                       <xsl:attribute name="select">$lang</xsl:attribute>
                                    </xsl:element>
                                 </xsl:element>
                              </xsl:element>
                           </img>
                           <xsl:element name="xsl:choose">
                              <xsl:element name="xsl:when">
                                 <xsl:attribute name="test">$previewMode</xsl:attribute>
                                 <xsl:element name="xsl:call-template">
                                    <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">key</xsl:attribute>
                                       <xsl:attribute name="select">'psx.cas.assemblyedit@Activate'</xsl:attribute>
                                    </xsl:element>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">lang</xsl:attribute>
                                       <xsl:attribute name="select">$lang</xsl:attribute>
                                    </xsl:element>
                                 </xsl:element>
                              </xsl:element>
                              <xsl:element name="xsl:otherwise">
                                 <xsl:element name="xsl:call-template">
                                    <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">key</xsl:attribute>
                                       <xsl:attribute name="select">'psx.cas.assemblyedit@Deactivate'</xsl:attribute>
                                    </xsl:element>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">lang</xsl:attribute>
                                       <xsl:attribute name="select">$lang</xsl:attribute>
                                    </xsl:element>
                                 </xsl:element>
                              </xsl:element>
                           </xsl:element>
                        </a>
                     </td>
                     <td width="50" align="center" nowrap="true">
		    
			
			<xsl:element name="script">
				<xsl:attribute name="language"><xsl:value-of select="'JavaScript1.2'"/></xsl:attribute>
				<xsl:text disable-output-escaping="yes"><![CDATA[<![CDATA[				   
				var _codebase = "]]></xsl:text><xsl:text disable-output-escaping="yes">]</xsl:text><xsl:text disable-output-escaping="yes">]></xsl:text>
				<xsl:element name="xsl:value-of">
					<xsl:attribute name="select">//@codebase</xsl:attribute>
				</xsl:element>
				<xsl:text disable-output-escaping="yes"><![CDATA[<![CDATA[";
				var _classid = "]]></xsl:text><xsl:text disable-output-escaping="yes">]</xsl:text><xsl:text disable-output-escaping="yes">]></xsl:text>
				<xsl:element name="xsl:value-of">
					<xsl:attribute name="select">//@classid</xsl:attribute>
				</xsl:element>
				<xsl:text disable-output-escaping="yes"><![CDATA[<![CDATA[";
				var _type = "]]></xsl:text><xsl:text disable-output-escaping="yes">]</xsl:text><xsl:text disable-output-escaping="yes">]></xsl:text>
				<xsl:element name="xsl:value-of">
					<xsl:attribute name="select">concat('application/x-java-applet;',//@version_type,'=',//@implementation_version)</xsl:attribute>
				</xsl:element>	<xsl:text disable-output-escaping="yes"><![CDATA[<![CDATA[";   
				var _pluginpage = "]]></xsl:text><xsl:text disable-output-escaping="yes">]</xsl:text><xsl:text disable-output-escaping="yes">]></xsl:text>
				<xsl:element name="xsl:value-of">
					<xsl:attribute name="select">concat('http://java.sun.com/products/plugin/',//@implementation_version,'/plugin-install.html')</xsl:attribute>
				</xsl:element>	<xsl:text disable-output-escaping="yes"><![CDATA[<![CDATA[";
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
				appletCaller.addParam("helpset_file", "../../Docs/Rhythmyx/Business_Users/Content_Explorer_Help.hs");
				appletCaller.addParam("helpId", "O3042");
				appletCaller.show();         
	           
                           

           ]]></xsl:text><xsl:text disable-output-escaping="yes">]</xsl:text><xsl:text disable-output-escaping="yes">]></xsl:text>
           </xsl:element>
	   
                        <a class="PSnavmenuitem" href="javascript:void(0)" onclick="_showHelp()">
                           <img src="../sys_resources/images/relatedcontent/questionmark.gif" border="0" align="absmiddle" alt="Help">
                              <xsl:element name="xsl:attribute">
                                 <xsl:attribute name="name">alt</xsl:attribute>
                                 <xsl:element name="xsl:call-template">
                                    <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">key</xsl:attribute>
                                       <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Help'</xsl:attribute>
                                    </xsl:element>
                                    <xsl:element name="xsl:with-param">
                                       <xsl:attribute name="name">lang</xsl:attribute>
                                       <xsl:attribute name="select">$lang</xsl:attribute>
                                    </xsl:element>
                                 </xsl:element>
                              </xsl:element>
                           </img>
                        </a>
                     </td>
                  </tr>
               </table>
            </div>
            <!-- this is a spacer div to move the whole page down below the top nav bar -->
            <!-- this is a spacer div to move the whole page down below the top nav bar -->
            <div id="PSTopBarSpacer" style="height:26px">
               <xsl:element name="xsl:variable">
                  <xsl:attribute name="name">UserAgent</xsl:attribute>
                  <xsl:attribute name="select">$related/infourls/@UserAgent</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:choose">
                  <xsl:element name="xsl:when">
                     <xsl:attribute name="test">contains($UserAgent,'MSIE')</xsl:attribute>
                     <xsl:text>&nbsp;</xsl:text>
                  </xsl:element>
                  <xsl:element name="xsl:otherwise">
                     <font color="#FFFFFF">.</font>
                  </xsl:element>
               </xsl:element>
            </div>
         </xsl:element>
         <xsl:element name="xsl:choose">
            <xsl:element name="xsl:when">
               <xsl:attribute name="test"><xsl:value-of select="concat('$related/../@sys_activeitemid and ', $testcondition)"/></xsl:attribute>
               <div class="PSactiveitemborder">
                  <div class="PSactiveitemborder2">
                     <!-- add action links for the the active item -->
                     <a href="javascript:void(0)" onclick="PSActivateBreadCrumbParentItem(); return false">
                        <img src="../sys_resources/images/minus.gif" border="0" align="absmiddle" alt="Activate Parent">
                           <xsl:element name="xsl:attribute">
                              <xsl:attribute name="name">alt</xsl:attribute>
                              <xsl:element name="xsl:call-template">
                                 <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                 <xsl:element name="xsl:with-param">
                                    <xsl:attribute name="name">key</xsl:attribute>
                                    <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Activate Parent'</xsl:attribute>
                                 </xsl:element>
                                 <xsl:element name="xsl:with-param">
                                    <xsl:attribute name="name">lang</xsl:attribute>
                                    <xsl:attribute name="select">$lang</xsl:attribute>
                                 </xsl:element>
                              </xsl:element>
                           </xsl:element>
                        </img>
                     </a>
							<xsl:element name="xsl:variable">
								<xsl:attribute name="name">tmpcontentdetails</xsl:attribute>
								<xsl:attribute name="select">document(concat($related/infourls/@activeiteminfourl, '&amp;sys_activeitemid=',$related/../@sys_activeitemid))</xsl:attribute>
							</xsl:element>
							<xsl:element name="xsl:variable">
								<xsl:attribute name="name">tmpcontentid</xsl:attribute>
								<xsl:attribute name="select">$tmpcontentdetails/activeitem/@contentid</xsl:attribute>
							</xsl:element>
							<xsl:element name="xsl:variable">
								<xsl:attribute name="name">tmpvariantid</xsl:attribute>
								<xsl:attribute name="select">$tmpcontentdetails/activeitem/@variantid</xsl:attribute>
							</xsl:element>
							<xsl:element name="xsl:variable">
								<xsl:attribute name="name">tmpeditauth</xsl:attribute>
								<xsl:attribute name="select">$tmpcontentdetails/activeitem/@parenteditauthorized</xsl:attribute>
							</xsl:element>
                     <xsl:element name="xsl:variable">
                        <xsl:attribute name="name">contentstatus2</xsl:attribute>
                        <xsl:attribute name="select">document(concat($related/infourls/@contentstatusurl,'&amp;sys_contentid=',$tmpcontentid))/contentstatus</xsl:attribute>
                     </xsl:element>
                     <xsl:element name="xsl:apply-templates">
                        <xsl:attribute name="select">$itemactions</xsl:attribute>
                        <xsl:attribute name="mode">mainmenu</xsl:attribute>
                        <xsl:element name="xsl:with-param">
                           <xsl:attribute name="name">sessionid</xsl:attribute>
                           <xsl:attribute name="select">$related/../@pssessionid</xsl:attribute>
                        </xsl:element>
                        <xsl:element name="xsl:with-param">
                           <xsl:attribute name="name">actionsetid</xsl:attribute>
                           <xsl:attribute name="select">$related/../@sys_activeitemid</xsl:attribute>
                        </xsl:element>
                        <xsl:element name="xsl:with-param">
                           <xsl:attribute name="name">contentid</xsl:attribute>
                           <xsl:attribute name="select">$tmpcontentid</xsl:attribute>
                        </xsl:element>
                        <xsl:element name="xsl:with-param">
                           <xsl:attribute name="name">revision</xsl:attribute>
                           <xsl:attribute name="select">$contentstatus2/@tiprevision</xsl:attribute>
                        </xsl:element>
		               <xsl:element name="xsl:with-param">
		                  <xsl:attribute name="name">contentvalid</xsl:attribute>
		                  <xsl:attribute name="select">$contentstatus2/@contentvalid</xsl:attribute>
		               </xsl:element>
		               <xsl:element name="xsl:with-param">
		                  <xsl:attribute name="name">itemcommunity</xsl:attribute>
		                  <xsl:attribute name="select">$contentstatus2/@itemcommunity</xsl:attribute>
		               </xsl:element>
		               <xsl:element name="xsl:with-param">
		                  <xsl:attribute name="name">usercommunity</xsl:attribute>
		                  <xsl:attribute name="select">$contentstatus2/@usercommunity</xsl:attribute>
		               </xsl:element>
                        <xsl:element name="xsl:with-param">
                           <xsl:attribute name="name">variantid</xsl:attribute>
                           <xsl:attribute name="select">$tmpvariantid</xsl:attribute>
                        </xsl:element>
								<xsl:element name="xsl:with-param">
									<xsl:attribute name="name">editauthorized</xsl:attribute>
									<xsl:attribute name="select">$tmpeditauth</xsl:attribute>
								</xsl:element>		
							</xsl:element>
                     <a href="javascript:void(0)">
                        <xsl:element name="xsl:attribute">
                           <xsl:attribute name="name">onmousedown</xsl:attribute>
                           <xsl:element name="xsl:value-of">
                              <xsl:attribute name="select">concat(&quot;PSEnterTopItem(&quot;, $related/../@sys_activeitemid, &quot;, event)&quot;)</xsl:attribute>
                           </xsl:element>
                        </xsl:element>
                        <xsl:element name="xsl:attribute">
                           <xsl:attribute name="name">onmouseout</xsl:attribute>
                           <xsl:element name="xsl:value-of">
                              <xsl:attribute name="select">concat(&quot;PSExitTopItem(&quot;, $related/../@sys_activeitemid, &quot;, event)&quot;)</xsl:attribute>
                           </xsl:element>
                        </xsl:element>
                        <img src="../sys_resources/images/triangleblue.gif" border="0" align="absmiddle">
                           <xsl:element name="xsl:attribute">
                              <xsl:attribute name="name">alt</xsl:attribute>
                              <xsl:text>&nbsp;</xsl:text>
                              <xsl:element name="xsl:call-template">
                                 <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                 <xsl:element name="xsl:with-param">
                                    <xsl:attribute name="name">key</xsl:attribute>
                                    <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Actions'</xsl:attribute>
                                 </xsl:element>
                                 <xsl:element name="xsl:with-param">
                                    <xsl:attribute name="name">lang</xsl:attribute>
                                    <xsl:attribute name="select">$lang</xsl:attribute>
                                 </xsl:element>
                              </xsl:element>
                              <xsl:text>:&nbsp;</xsl:text>
                              <xsl:element name="xsl:value-of">
                                 <xsl:attribute name="select">document(concat($related/infourls/@activeiteminfourl, '&amp;sys_activeitemid=',$related/../@sys_activeitemid))/activeitem/@title</xsl:attribute>
                              </xsl:element>
                              <!-- the following two lines are to put a newline between the data pieces -->
                              <xsl:element name="xsl:variable">
                                 <xsl:attribute name="name">UserAgent</xsl:attribute>
                                 <xsl:attribute name="select">$related/infourls/@UserAgent</xsl:attribute>
                              </xsl:element>
                              <xsl:element name="xsl:if">
                                 <xsl:attribute name="test">contains($UserAgent,'MSIE')</xsl:attribute>
                                 <xsl:text>&nbsp;
</xsl:text>
                              </xsl:element>
                              <xsl:element name="xsl:choose">
                                 <xsl:element name="xsl:when">
                                    <xsl:attribute name="test">$contentstatus2/@checkoutusername = ''</xsl:attribute>
                                    <xsl:text>&nbsp;</xsl:text>
                                    <xsl:element name="xsl:call-template">
                                       <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                       <xsl:element name="xsl:with-param">
                                          <xsl:attribute name="name">key</xsl:attribute>
                                          <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Not Checked Out'</xsl:attribute>
                                       </xsl:element>
                                       <xsl:element name="xsl:with-param">
                                          <xsl:attribute name="name">lang</xsl:attribute>
                                          <xsl:attribute name="select">$lang</xsl:attribute>
                                       </xsl:element>
                                    </xsl:element>
                                    <xsl:text>:&nbsp;</xsl:text>
                                 </xsl:element>
                                 <xsl:element name="xsl:when">
                                    <xsl:attribute name="test">translate($contentstatus2/@checkoutusername, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ') = translate($contentstatus2/@username, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')</xsl:attribute>
                                    <xsl:text>&nbsp;</xsl:text>
                                    <xsl:element name="xsl:call-template">
                                       <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                       <xsl:element name="xsl:with-param">
                                          <xsl:attribute name="name">key</xsl:attribute>
                                          <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Checked out by: me'</xsl:attribute>
                                       </xsl:element>
                                       <xsl:element name="xsl:with-param">
                                          <xsl:attribute name="name">lang</xsl:attribute>
                                          <xsl:attribute name="select">$lang</xsl:attribute>
                                       </xsl:element>
                                    </xsl:element>
                                    <xsl:text>&nbsp;</xsl:text>
                                 </xsl:element>
                                 <xsl:element name="xsl:otherwise">
                                    <xsl:text>&nbsp;</xsl:text>
                                    <xsl:element name="xsl:call-template">
                                       <xsl:attribute name="name">getLocaleString</xsl:attribute>
                                       <xsl:element name="xsl:with-param">
                                          <xsl:attribute name="name">key</xsl:attribute>
                                          <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Checked out by'</xsl:attribute>
                                       </xsl:element>
                                       <xsl:element name="xsl:with-param">
                                          <xsl:attribute name="name">lang</xsl:attribute>
                                          <xsl:attribute name="select">$lang</xsl:attribute>
                                       </xsl:element>
                                    </xsl:element>
                                    <xsl:text>:&nbsp;</xsl:text>
                                    <xsl:element name="xsl:value-of">
                                       <xsl:attribute name="select">$contentstatus2/@checkoutusername</xsl:attribute>
                                    </xsl:element>
                                 </xsl:element>
                              </xsl:element>
                           </xsl:element>
                        </img>
                     </a>
                     <xsl:apply-templates mode="copy"/>
                  </div>
               </div>
            </xsl:element>
            <xsl:element name="xsl:otherwise">
               <xsl:apply-templates mode="copy"/>
            </xsl:element>
         </xsl:element>
      </xsl:copy>
      <xsl:element name="xsl:if">
         <xsl:attribute name="test">not($related/../@relateditemid)</xsl:attribute>
         <!-- script for updating the floating top navigation bar -->
         <script language="JavaScript">

var navBarObj = PSgetObj("PSTopNavBar");

function PSFloatTopNavBar()
{
	if (navBarObj != null)
	{
		if (is_nav)
		{
			if (is_nav4)
				navBarObj.top = window.pageYOffset;
			else
				navBarObj.style.top = window.pageYOffset;
			
			setTimeout("PSFloatTopNavBar()",25);
		}
		else if (is_ie)
		{
			navBarObj.style.top = document.body.scrollTop;
			navBarObj.style.width = document.body.clientWidth;
		}
	}
}

if (is_nav)
	PSFloatTopNavBar();
else
{
	window.onscroll = PSFloatTopNavBar;
	window.onresize = PSFloatTopNavBar;
	PSFloatTopNavBar();
}

</script>
      </xsl:element>
   </xsl:template>
   <!-- Locate the field and add edit links -->
   <xsl:template match="span[@psxedit]" mode="copy">
      <xsl:element name="xsl:if">
         <xsl:attribute name="test"><xsl:value-of select="$testcondition"/></xsl:attribute>
         <xsl:element name="xsl:variable">
            <xsl:attribute name="name">contentid</xsl:attribute>
            <xsl:attribute name="select">$related/../@sys_contentid</xsl:attribute>
         </xsl:element>
         <xsl:element name="xsl:variable">
            <xsl:attribute name="name">contentstatus</xsl:attribute>
            <xsl:attribute name="select">document(concat($related/infourls/@contentstatusurl,'&amp;sys_contentid=',$contentid))/contentstatus</xsl:attribute>
         </xsl:element>
         <!-- if not checked out or checked out to me -->
         <xsl:element name="xsl:if">
            <xsl:attribute name="test">$contentstatus/@checkoutusername = '' or translate($contentstatus/@checkoutusername, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ') = translate($contentstatus/@username, 'abcdefghijklmnopqrstuvwxyz' , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')</xsl:attribute>
            <xsl:element name="xsl:variable">
               <xsl:attribute name="name">sessionid</xsl:attribute>
               <xsl:attribute name="select">$related/../@pssessionid</xsl:attribute>
            </xsl:element>
            <!-- add edit field link  if necessary -->
            <xsl:element name="xsl:variable">
               <xsl:attribute name="name">wf</xsl:attribute>
               <xsl:attribute name="select">$itemactions//@wfurlint</xsl:attribute>
            </xsl:element>
            <xsl:element name="xsl:if">
               <xsl:attribute name="test">$wf</xsl:attribute>
               <xsl:element name="xsl:variable">
                  <xsl:attribute name="name">tmp</xsl:attribute>
                  <xsl:attribute name="select">document(concat($wf, '&amp;sys_contentid=', $contentid))</xsl:attribute>
               </xsl:element>
               <xsl:element name="xsl:if">
                  <xsl:attribute name="test">$tmp//UserName/@assignmentType > 2 and $tmp//BasicInfo/@isPublic='n'</xsl:attribute>
                  <script>
							PSFieldEditSpan(true);
						</script>
                  <a href="javascript:void(0);">
                     <xsl:element name="xsl:attribute">
                        <xsl:attribute name="name">onclick</xsl:attribute>
                        <xsl:text>PSEditField('</xsl:text>
                        <xsl:element name="xsl:value-of">
                           <xsl:attribute name="select">'../sys_action/checkoutedit.xml'</xsl:attribute>
                        </xsl:element>
                        <xsl:text>','</xsl:text>
                        <xsl:element name="xsl:value-of">
                           <xsl:attribute name="select">$contentid</xsl:attribute>
                        </xsl:element>
                        <xsl:text>','</xsl:text>
                        <xsl:element name="xsl:value-of">
                           <xsl:attribute name="select">$contentstatus/@tiprevision</xsl:attribute>
                        </xsl:element>
                        <xsl:text>','</xsl:text>
                        <xsl:value-of select="@psxedit"/>
                        <xsl:text>')</xsl:text>
                     </xsl:element>
                     <img src="../sys_resources/images/pen.gif" alt="Edit - {@psxedit}" border="0" align="absmiddle">
                        <xsl:element name="xsl:attribute">
                           <xsl:attribute name="name">alt</xsl:attribute>
                           <xsl:element name="xsl:call-template">
                              <xsl:attribute name="name">getLocaleString</xsl:attribute>
                              <xsl:element name="xsl:with-param">
                                 <xsl:attribute name="name">key</xsl:attribute>
                                 <xsl:attribute name="select">'psx.cas.assemblyedit.alt@Edit'</xsl:attribute>
                              </xsl:element>
                              <xsl:element name="xsl:with-param">
                                 <xsl:attribute name="name">lang</xsl:attribute>
                                 <xsl:attribute name="select">$lang</xsl:attribute>
                              </xsl:element>
                           </xsl:element>
                           <xsl:text>&nbsp;-&nbsp;</xsl:text>
                           <xsl:value-of select="@psxedit"/>
                        </xsl:element>
                     </img>
                  </a>
                  <script>
							PSFieldEditSpan(false);
						</script>
               </xsl:element>
            </xsl:element>
         </xsl:element>
      </xsl:element>
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <!-- Locate the main template add insertaction link variables -->
   <xsl:template match="xsl:template[@match=&quot;/&quot;][1]" mode="copy">
      <xsl:element name="xsl:variable">
         <xsl:attribute name="name">slotactions</xsl:attribute>
         <xsl:attribute name="select">document(concat($related/infourls/@actionlisturl, '&amp;sys_uicontext=Slot'))/*</xsl:attribute>
      </xsl:element>
      <xsl:element name="xsl:variable">
         <xsl:attribute name="name">itemactions</xsl:attribute>
         <xsl:attribute name="select">document(concat($related/infourls/@actionlisturl, '&amp;sys_uicontext=Item'))/*</xsl:attribute>
      </xsl:element>
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <psxi18n:lookupkeys>
      <key name="psx.cas.assemblyedit.alt@Actions">Alt text for triangle image in active asemble.</key>
      <key name="psx.cas.assemblyedit.alt@Activate">Alt text for plus image in active assembly.</key>
      <key name="psx.cas.assemblyedit.alt@Not Checked Out">Alt text when item is not checked out in active assembly.</key>
      <key name="psx.cas.assemblyedit.alt@Checked out by: me">Alt text when item is checked out by login user in active assembly.</key>
      <key name="psx.cas.assemblyedit.alt@Checked out by">Alt text when item is checked out by some other user in active assembly.</key>
      <key name="psx.cas.assemblyedit.alt@User">Alt text showing user name.</key>
      <key name="psx.cas.assemblyedit.alt@Role">Alt text showing role name.</key>
      <key name="psx.cas.assemblyedit@Rhythmyx">Rhythmyx text that appears on active assembly menu bar.</key>
      <key name="psx.cas.assemblyedit.alt@Toggle Mode">Alt text for Toggle mode image.</key>
      <key name="psx.cas.assemblyedit@Activate">Activate link appears on active asembly menu bar, when the item is in deactivate mode.</key>
      <key name="psx.cas.assemblyedit@Deactivate">Deactivate link appears on active asembly menu bar, when the item is in activate mode.</key>
      <key name="psx.cas.assemblyedit.alt@Help">Alt text for help image in active assembly.</key>
      <key name="psx.cas.assemblyedit.alt@Activate Parent">Alt text for minus image in active assembly.</key>
      <key name="psx.cas.assemblyedit.alt@Edit">Alt text for edit(pen) image in active assembly.</key>
   </psxi18n:lookupkeys>
</xsl:stylesheet>
