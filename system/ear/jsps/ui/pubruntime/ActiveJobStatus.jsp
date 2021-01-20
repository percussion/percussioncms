<%@ include file="/ui/pubruntime/PubRuntimeAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>

<%--
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
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Rhythmyx - Runtime Edition List" />

<layout:pubruntime>
	<jsp:body>
		<rxcomp:menubar>
			<rxcomp:menuitem value="Help" 
				onclick="openHelpWindow('#{sys_runtime_navigation.startingNode.helpFile}')"/>
		</rxcomp:menubar>	
		<rxnav:runtimebreadcrumbs/>
      <tr:poll interval="6000" id="pollJobProgress" />
		<tr:panelHeader styleClass="rxPanelHeader" text="Running Editions">
         <tr:table var="entry" width="100%" rowBandingInterval="1" rows="25"
            partialTriggers="pollJobProgress" 
            value="#{sys_runtime_navigation.startingNode.activeJobStatus}">
            <tr:column headerText="Edition">
               <tr:commandLink text="#{entry.editionNameWithId}"
                  action="#{entry.statusEntry.viewLog}"/>
            </tr:column>
            <tr:column headerText="Behavior">
               <tr:outputText value="#{entry.editionBehavior}"/>
            </tr:column>
            <tr:column headerText="Status">
            	<tr:panelHorizontalLayout>
	               <tr:image source="#{entry.statusImage}"/>
	               <tr:outputText value="#{entry.status}"/>
               </tr:panelHorizontalLayout>
            </tr:column>
            <tr:column headerText="Progress">
               <rxcomp:progressBar percent="#{entry.progress}" 
                  rendered="#{! entry.isTerminal}"/>
            </tr:column>
            <tr:column width="23px">
               <tr:commandLink rendered="#{! entry.isTerminal}"
				      action="#{entry.statusEntry.stopJob}">
				      <tr:image source="../../sys_resources/images/stop.png" shortDesc="Stop"/>
               </tr:commandLink>
            </tr:column>
         </tr:table>
      </tr:panelHeader>
	</jsp:body>
</layout:pubruntime>
