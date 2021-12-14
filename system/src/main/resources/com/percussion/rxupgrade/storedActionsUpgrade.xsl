<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!--
      Stylesheet for upgrading the
      "rxconfig/Server/requestHandlers/storedActions.xml" file.
      Currently it adds the "checkin" and "checkout" actions if the
      corresponding "ActionSet" element is missing.
   -->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output doctype-system="sys_StoredActions.dtd"/>
   <!-- main template -->
   <xsl:template match="/">
      <xsl:apply-templates select="." mode="copy"/>
   </xsl:template>
   <!-- copy any attribute or template -->
   <xsl:template match="@*|*|comment()" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="StoredActions" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
         <xsl:if test="not(ActionSet[@name='checkin'])">
            <ActionSet name="checkin">
               <!-- checks in -->
               <Action name="checkin" ignoreError="yes">
                  <PSXParam name="sys_command">
                     <DataLocator>
                        <PSXTextLiteral id="0">
                           <text>workflow</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction">
                     <DataLocator>
                        <PSXTextLiteral id="1">
                           <text>checkin</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction1">
                     <DataLocator>
                        <PSXSingleHtmlParameter id="4">
                           <name>WFAction</name>
                        </PSXSingleHtmlParameter>
                     </DataLocator>
                  </PSXParam>
               </Action>
               <PSXUrlRequest>
                  <PSXExtensionCall id="10">
                     <name>Java/global/percussion/generic/sys_MakeAbsLink</name>
                     <PSXExtensionParamValue id="11">
                        <value>
                           <PSXTextLiteral id="12">
                              <text>../sys_uiSupport/redirect.html</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXUrlRequest>
            </ActionSet>
         </xsl:if>
         <xsl:if test="not(ActionSet[@name='checkout'])">
            <ActionSet name="checkout">
               <!-- checks out -->
               <Action name="checkout" ignoreError="yes">
                  <PSXParam name="sys_command">
                     <DataLocator>
                        <PSXTextLiteral id="0">
                           <text>workflow</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction">
                     <DataLocator>
                        <PSXTextLiteral id="1">
                           <text>checkout</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
               </Action>
               <PSXUrlRequest>
                  <PSXExtensionCall id="10">
                     <name>Java/global/percussion/generic/sys_MakeAbsLink</name>
                     <PSXExtensionParamValue id="11">
                        <value>
                           <PSXTextLiteral id="12">
                              <text>../sys_uiSupport/redirect.html</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXUrlRequest>
            </ActionSet>
         </xsl:if>
         <xsl:if test="not(ActionSet[@name='checkoutedit'])">
            <ActionSet name="checkoutedit">
               <!-- checks out, then edits a content item.  will not fail if item is already checked out -->
               <Action name="checkout" ignoreError="yes">
                  <PSXParam name="sys_command">
                     <DataLocator>
                        <PSXTextLiteral id="0">
                           <text>workflow</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction">
                     <DataLocator>
                        <PSXTextLiteral id="1">
                           <text>checkout</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
               </Action>
               <PSXUrlRequest>
                  <PSXExtensionCall id="1">
                     <name>Java/global/percussion/generic/sys_MakeAbsLink</name>
                     <PSXExtensionParamValue id="2">
                        <value>
                           <PSXTextLiteral id="3">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="4">
                        <value>
                           <PSXTextLiteral id="5">
                              <text>sys_command</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="6">
                        <value>
                           <PSXTextLiteral id="7">
                              <text>edit</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="8">
                        <value>
                           <PSXTextLiteral id="9">
                              <text>sys_contentid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="10">
                        <value>
                           <PSXSingleHtmlParameter id="11">
                              <name>sys_contentid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="12">
                        <value>
                           <PSXTextLiteral id="13">
                              <text>sys_revision</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="14">
                        <value>
                           <PSXSingleHtmlParameter id="15">
                              <name>sys_revision</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="16">
                        <value>
                           <PSXTextLiteral id="17">
                              <text>sys_view</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="18">
                        <value>
                           <PSXSingleHtmlParameter id="19">
                              <name>sys_view</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="20">
                        <value>
                           <PSXTextLiteral id="21">
                              <text>sys_pageid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="22">
                        <value>
                           <PSXTextLiteral id="23">
                              <text>0</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="7">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>refreshHint</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="8">
                        <value>
                           <PSXSingleHtmlParameter id="0">
                              <name>refreshHint</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXUrlRequest>
            </ActionSet>
         </xsl:if>
         <xsl:if test="not(ActionSet[@name='checkintransition'])">
            <ActionSet name="checkintransition">
               <!-- checks in, then transitions a content item using the transition specified in the parameter "WFAction" -->
               <Action name="checkin" ignoreError="yes">
                  <PSXParam name="sys_command">
                     <DataLocator>
                        <PSXTextLiteral id="0">
                           <text>workflow</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction">
                     <DataLocator>
                        <PSXTextLiteral id="1">
                           <text>checkin</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction1">
                     <DataLocator>
                        <PSXSingleHtmlParameter id="4">
                           <name>WFAction</name>
                        </PSXSingleHtmlParameter>
                     </DataLocator>
                  </PSXParam>
               </Action>
               <PSXUrlRequest>
                  <PSXExtensionCall id="10">
                     <name>Java/global/percussion/generic/sys_MakeAbsLink</name>
                     <PSXExtensionParamValue id="11">
                        <value>
                           <PSXTextLiteral id="12">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="13">
                        <value>
                           <PSXTextLiteral id="14">
                              <text>sys_command</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="15">
                        <value>
                           <PSXTextLiteral id="16">
                              <text>workflow</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="17">
                        <value>
                           <PSXTextLiteral id="18">
                              <text>sys_contentid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="19">
                        <value>
                           <PSXSingleHtmlParameter id="20">
                              <name>sys_contentid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="21">
                        <value>
                           <PSXTextLiteral id="22">
                              <text>sys_revision</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="23">
                        <value>
                           <PSXSingleHtmlParameter id="24">
                              <name>sys_revision</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="25">
                        <value>
                           <PSXTextLiteral id="26">
                              <text>sys_transitionid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="27">
                        <value>
                           <PSXSingleHtmlParameter id="28">
                              <name>sys_transitionid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="29">
                        <value>
                           <PSXTextLiteral id="30">
                              <text>WFAction</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="31">
                        <value>
                           <PSXSingleHtmlParameter id="32">
                              <name>WFAction1</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="33">
                        <value>
                           <PSXTextLiteral id="34">
                              <text>psredirect</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="35">
                        <value>
                           <PSXSingleHtmlParameter id="36">
                              <name>psredirect</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="37">
                        <value>
                           <PSXTextLiteral id="38">
                              <text>commenttext</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="39">
                        <value>
                           <PSXSingleHtmlParameter id="40">
                              <name>commenttext</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="41">
                        <value>
                           <PSXTextLiteral id="42">
                              <text>sys_wfAdhocUserList</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="43">
                        <value>
                           <PSXSingleHtmlParameter id="44">
                              <name>sys_wfAdhocUserList</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXUrlRequest>
            </ActionSet>
         </xsl:if>
         <xsl:if test="not(ActionSet[@name='transitcheckoutedit'])">
            <!-- 
            An action set introduced for quick edit. The current item will be 
            transitioned using the workflow transition specified in parameter
            'WFAction', then it is checked-out and opened in edit mode.
         
            WFAction - the transition (the trigger name) used to transit the
               item.
            sys_contentid - the content id of the item to act on.
            sys_revision - the revision of the item to act on.
            sys_view - the view used to open the item in edit mode.
         -->
            <ActionSet name="transitcheckoutedit">
               <Action name="transit" ignoreError="no">
                  <PSXParam name="sys_command">
                     <DataLocator>
                        <PSXTextLiteral id="0">
                           <text>workflow</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
               </Action>
               <Action name="checkout" ignoreError="no">
                  <PSXParam name="sys_command">
                     <DataLocator>
                        <PSXTextLiteral id="0">
                           <text>workflow</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction">
                     <DataLocator>
                        <PSXTextLiteral id="1">
                           <text>checkout</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
               </Action>
               <PSXUrlRequest>
                  <PSXExtensionCall id="1">
                     <name>Java/global/percussion/generic/sys_MakeAbsLink</name>
                     <PSXExtensionParamValue id="0">
                        <value>
                           <PSXTextLiteral id="0">
                              <text/>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="1">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>sys_command</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="2">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>edit</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="3">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>sys_contentid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="4">
                        <value>
                           <PSXSingleHtmlParameter id="0">
                              <name>sys_contentid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="5">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>sys_revision</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="6">
                        <value>
                           <PSXSingleHtmlParameter id="0">
                              <name>sys_revision</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="7">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>sys_view</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="8">
                        <value>
                           <PSXSingleHtmlParameter id="0">
                              <name>sys_view</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="9">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>sys_pageid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="10">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>0</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="7">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>target</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="8">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>_blank</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="7">
                        <value>
                           <PSXTextLiteral id="0">
                              <text>refreshHint</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="8">
                        <value>
                           <PSXSingleHtmlParameter id="0">
                              <name>refreshHint</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXUrlRequest>
            </ActionSet>
         </xsl:if>
         <xsl:if test="not(ActionSet[@name='checkoutaapage'])">
            <!-- 
            An action set introduced for new style of active assembly for pages in that we actually checkout the item if not checked out and then open the page for active assembly. The trick is that we always have to open the tip revision instead of current revision. As we now checkout the page implicitly before active assembly we have to resolve the revision dynamically and then modify the active assembly page url. This is what exactly this action set does. The parameters:
            sys_command is hardcoded to be 'workflow'
            WFAction - hardcoded to 'checkout'
            We generate the redirect url based in the following parameters
            sys_assemblyurl - Base url for the document assembly, caller sends this as HTML parameter.
            sys_contentid - the content id of the item for active assembly, caller sends this as HTML parameter.
            sys_revision - the revision of the item for active assembly. By now workflow exit would already have put the right revision value for this parameter.
            sys_variantid - variantid of the page for active assembly, caller sends this as HTML parameter.
            sys_context - context hardcoded to preview context, i.e. 0.
            sys_authtype - authtype hardcoded to all content, i.e. 0.
         -->
            <ActionSet name="checkoutaapage">
               <Action ignoreError="yes" name="checkout">
                  <PSXParam name="sys_command">
                     <DataLocator>
                        <PSXTextLiteral id="0">
                           <text>workflow</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction">
                     <DataLocator>
                        <PSXTextLiteral id="1">
                           <text>checkout</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
               </Action>
               <PSXUrlRequest>
                  <PSXExtensionCall id="1">
                     <name>Java/global/percussion/generic/sys_MakeAbsLink</name>
                     <PSXExtensionParamValue id="2">
                        <value>
                           <PSXSingleHtmlParameter id="11">
                              <name>sys_assemblyurl</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="4">
                        <value>
                           <PSXTextLiteral id="5">
                              <text>sys_command</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="6">
                        <value>
                           <PSXTextLiteral id="7">
                              <text>editrc</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="8">
                        <value>
                           <PSXTextLiteral id="9">
                              <text>sys_contentid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="10">
                        <value>
                           <PSXSingleHtmlParameter id="11">
                              <name>sys_contentid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="12">
                        <value>
                           <PSXTextLiteral id="13">
                              <text>sys_revision</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="14">
                        <value>
                           <PSXSingleHtmlParameter id="11">
                              <name>sys_revision</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="16">
                        <value>
                           <PSXTextLiteral id="17">
                              <text>sys_variantid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="18">
                        <value>
                           <PSXSingleHtmlParameter id="19">
                              <name>sys_variantid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="20">
                        <value>
                           <PSXTextLiteral id="21">
                              <text>sys_context</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="22">
                        <value>
                           <PSXTextLiteral id="23">
                              <text>0</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="20">
                        <value>
                           <PSXTextLiteral id="21">
                              <text>sys_authtype</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="22">
                        <value>
                           <PSXTextLiteral id="23">
                              <text>0</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="20">
                        <value>
                           <PSXTextLiteral id="21">
                              <text>sys_siteid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="18">
                        <value>
                           <PSXSingleHtmlParameter id="19">
                              <name>sys_siteid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="24">
                        <value>
                           <PSXTextLiteral id="25">
                              <text>parentPage</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="26">
                        <value>
                           <PSXTextLiteral id="27">
                              <text>yes</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
		            <PSXExtensionParamValue id="28">
		               <value>
		                  <PSXTextLiteral id="29">
		                     <text>sys_folderid</text>
		                  </PSXTextLiteral>
		               </value>
		            </PSXExtensionParamValue>
		            <PSXExtensionParamValue id="30">
		               <value>
		                  <PSXSingleHtmlParameter id="31">
		                     <name>sys_folderid</name>
		                  </PSXSingleHtmlParameter>
		               </value>
		            </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXUrlRequest>
            </ActionSet>
         </xsl:if>
         <xsl:if test="not(ActionSet[@name='checkoutaadoc'])">
            <!-- 
            An action set introduced for new style of active assembly for documents in that we actually checkout the item if not checked out and then open the page for active assembly. The trick is that we alway have to open the tip revision instead of current revision. As we now checkout the document implicitly before active assembly we have to resolve the revision dynamically and then modify the active assembly document url. This is what exactly this action set does. The parameters:
            sys_command is hardcoded to be 'workflow'
            WFAction - hardcoded to 'checkout'
            We generate the redirect url based in the following parameters
            sys_assemblyurl - Base url for the document assembly, caller sends this as HTML parameter.
            sys_contentid - the content id of the item for active assembly, caller sends this as HTML parameter.
            sys_revision - the revision of the item for active assembly. By now workflow exit would already have put the right revision value for this parameter.
            sys_variantid - variantid of the page for active assembly, caller sends this as HTML parameter.
         -->
            <ActionSet name="checkoutaadoc">
               <Action ignoreError="yes" name="checkout">
                  <PSXParam name="sys_command">
                     <DataLocator>
                        <PSXTextLiteral id="0">
                           <text>workflow</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
                  <PSXParam name="WFAction">
                     <DataLocator>
                        <PSXTextLiteral id="1">
                           <text>checkout</text>
                        </PSXTextLiteral>
                     </DataLocator>
                  </PSXParam>
               </Action>
               <PSXUrlRequest>
                  <PSXExtensionCall id="1">
                     <name>Java/global/percussion/generic/sys_MakeAbsLink</name>
                     <PSXExtensionParamValue id="2">
                        <value>
                           <PSXSingleHtmlParameter id="11">
                              <name>sys_assemblyurl</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="8">
                        <value>
                           <PSXTextLiteral id="9">
                              <text>sys_contentid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="10">
                        <value>
                           <PSXSingleHtmlParameter id="11">
                              <name>sys_contentid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="12">
                        <value>
                           <PSXTextLiteral id="13">
                              <text>sys_revision</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="14">
                        <value>
                           <PSXSingleHtmlParameter id="11">
                              <name>sys_revision</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="16">
                        <value>
                           <PSXTextLiteral id="17">
                              <text>sys_variantid</text>
                           </PSXTextLiteral>
                        </value>
                     </PSXExtensionParamValue>
                     <PSXExtensionParamValue id="18">
                        <value>
                           <PSXSingleHtmlParameter id="19">
                              <name>sys_variantid</name>
                           </PSXSingleHtmlParameter>
                        </value>
                     </PSXExtensionParamValue>
                  </PSXExtensionCall>
               </PSXUrlRequest>
            </ActionSet>
         </xsl:if>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="ActionSet[@name='checkoutedit']/PSXUrlRequest/PSXExtensionCall | ActionSet[@name='transitcheckoutedit']/PSXUrlRequest/PSXExtensionCall" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
         <xsl:if test="not(PSXExtensionParamValue/value/PSXTextLiteral[text='refreshHint'])">
            <PSXExtensionParamValue id="7">
               <value>
                  <PSXTextLiteral id="0">
                     <text>refreshHint</text>
                  </PSXTextLiteral>
               </value>
            </PSXExtensionParamValue>
            <PSXExtensionParamValue id="8">
               <value>
                  <PSXSingleHtmlParameter id="0">
                     <name>refreshHint</name>
                  </PSXSingleHtmlParameter>
               </value>
            </PSXExtensionParamValue>
         </xsl:if>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="ActionSet[@name='checkoutaapage']/PSXUrlRequest/PSXExtensionCall" mode="copy">
      <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:apply-templates mode="copy"/>
         <xsl:if test="not(PSXExtensionParamValue/value/PSXTextLiteral[text='sys_siteid'])">
            <PSXExtensionParamValue id="20">
               <value>
                  <PSXTextLiteral id="21">
                     <text>sys_siteid</text>
                  </PSXTextLiteral>
               </value>
            </PSXExtensionParamValue>
            <PSXExtensionParamValue id="18">
               <value>
                  <PSXSingleHtmlParameter id="19">
                     <name>sys_siteid</name>
                  </PSXSingleHtmlParameter>
               </value>
            </PSXExtensionParamValue>
         </xsl:if>
		 <xsl:if test="not(PSXExtensionParamValue/value/PSXTextLiteral[text='parentPage'])">
            <PSXExtensionParamValue id="24">
               <value>
                  <PSXTextLiteral id="25">
                     <text>parentPage</text>
                  </PSXTextLiteral>
               </value>
            </PSXExtensionParamValue>
            <PSXExtensionParamValue id="26">
               <value>
                  <PSXTextLiteral id="27">
                     <text>yes</text>
                  </PSXTextLiteral>
               </value>
            </PSXExtensionParamValue>
		 </xsl:if>
         <xsl:if test="not(PSXExtensionParamValue/value/PSXTextLiteral[text='sys_folderid'])">
            <PSXExtensionParamValue id="20">
               <value>
                  <PSXTextLiteral id="21">
                     <text>sys_folderid</text>
                  </PSXTextLiteral>
               </value>
            </PSXExtensionParamValue>
            <PSXExtensionParamValue id="18">
               <value>
                  <PSXSingleHtmlParameter id="19">
                     <name>sys_folderid</name>
                  </PSXSingleHtmlParameter>
               </value>
            </PSXExtensionParamValue>
         </xsl:if>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
