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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see https://www.gnu.org/licenses/
  -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:t="http://myfaces.apache.org/tomahawk"
	xmlns:tr="http://myfaces.apache.org/trinidad"
	xmlns:trh="http://myfaces.apache.org/trinidad/html"
	xmlns:rxb="urn:jsptagdir:/WEB-INF/tags/banner"
	version="1.2">
	<f:view>
		<tr:document styleClass="backgroundcolor" 
			title="#{page_title}" onload="#{page_onload}" >
			<f:facet name="metaContainer">
				<f:verbatim>
					<meta http-equiv="pragma" content="no-cache" />
					<meta http-equiv="cache-control" content="no-cache" />
					<meta http-equiv="expires" content="0" />
					<link rel="stylesheet" type="text/css"
						href="/Rhythmyx/sys_resources/css/menu.css" />
					<script type="text/javascript" src="/Rhythmyx/sys_resources/js/jsf/Utils.js">;</script>
					<jsp:directive.include file="/ui/header.jsp" />
					${page_script}
					<script type="text/javascript">
					
					   /*
					    * This is a hack to get around an IE7 bug where the table
					    * was not resizing on tree collapse
					    */
					   function forcePanelCollapse()
					   {
						var tree = document.getElementById("rxNavTree");
						var theTd = tree.parentNode.parentNode.previousSibling;
						try
						{
						   if(theTd.style.height == "2px")
						   {
						      theTd.style.height = "1px";
						   }
						   else
						   {
                                                      theTd.style.height = "2px";
						   }
						}
						catch(ignore){}
					   }

					   function forcePanelCollapseDelayed()
					   {
                                              if(psJsfUtil.isExplorer6())
					         setTimeout("psJsfUtil.navTreeNodeIE6Fix()", 120);
					      setTimeout("forcePanelCollapse()", 250);
					   }
					   
					   function openHelpWindow(helpUrl)
					   {
					      var hwin = window.open('../../Docs/Rhythmyx/Rhythmyx_Publishing_Runtime_Help/index.htm?toc.htm?' + helpUrl,"HelpWindow");
					      hwin.focus();
					   }

					</script>
				</f:verbatim>
			</f:facet>
			<tr:form rendered="true" id="pubruntime">
				<f:verbatim>
					<rxb:banner/>
				</f:verbatim>
				<h:panelGrid style="width: 100%" columns="2" rowClasses="pub-runtime-layout" 
					columnClasses="pub-runtime-nav,pub-runtime-content">
					<tr:panelBox styleClass="pub-runtime-nav" >
						<tr:navigationTree var="node" id="rxNavTree" onclick="forcePanelCollapseDelayed();"
							disclosedRowKeys="#{sys_runtime_navigation.disclosedRows}"
							value="#{sys_runtime_navigation.tree}">
							<f:facet name="nodeStamp">
							  <h:panelGroup>
                        <tr:commandNavigationItem 
                           action="#{node.perform}" styleClass="#{node.navLinkClass}"
                           rendered="#{node.enabled}" text="#{node.label}"
                           shortDesc="#{node.title}" selected="#{node.selected}" />
                        <tr:outputText 
                           styleClass="#{node.navLinkClass}"
                           rendered="#{!node.enabled}" value="#{node.label}" />
							  </h:panelGroup>
							</f:facet>
						</tr:navigationTree>
					</tr:panelBox>
					<t:div style="pub-runtime-content">
						<jsp:doBody />
					</t:div>
				</h:panelGrid>
			</tr:form>
		</tr:document>
	</f:view>
</jsp:root>