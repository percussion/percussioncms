<%@page errorPage="/ui/error.jsp"  pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad/html" prefix="trh"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
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

<%
   /* ph: I use the horizontal panel on the selectBoolean checkbox so it aligns
   with the Filter Content Lists label. */
%>

<c:set var="page_title" scope="request" value="Rhythmyx - Edition Editor" />
<layout:publishing>
	<jsp:body>
      <rxcomp:menubar>
         <rxcomp:menuitem value="#{sys_design_navigation.currentNode.eclist.new ? 'Add' : 'Done'}" 
            action="#{sys_design_navigation.currentNode.eclist.handleAssociation}" />
         <rxcomp:menuitem value="Cancel" 
            action="done" immediate="true"/>
			<rxcomp:menuitem value="Help" 
				onclick="openHelpWindow('#{sys_design_navigation.currentNode.eclist.helpFile}')"/>
         
      </rxcomp:menubar>
      <rxnav:editorbreadcrumbs />
      <tr:panelFormLayout>
         <tr:panelHorizontalLayout valign="center" halign="start" >
            <tr:inputText id="filteredText" label="Filter Content List" value="#{sys_design_navigation.currentNode.eclist.contentListFilter}" 
               valueChangeListener="#{sys_design_navigation.currentNode.eclist.contentListNameFilterChanged}"
               onkeypress="if ((window.event && window.event.keyCode == 13) || (event && event.which == 13)) submit(); else return true;" 
            />
            <tr:commandButton text="Apply" />
            <tr:commandButton text="Clear" action="#{sys_design_navigation.currentNode.eclist.clearFilter}" immediate="true" />
            <tr:message messageType="info" message="('#{empty sys_design_navigation.currentNode.eclist.contentListFilter ? '' : sys_design_navigation.currentNode.eclist.contentListFilter}' filter applied)"/>
         </tr:panelHorizontalLayout>
         <tr:panelHorizontalLayout valign="top" halign="start" >
            <tr:selectBooleanCheckbox label="Limit to Site and Unassigned"
               id="limitToSiteUnassigned"
               autoSubmit="true"
               valueChangeListener="#{sys_design_navigation.currentNode.eclist.contentListLimitFilterChanged}"
               value="#{sys_design_navigation.currentNode.eclist.limitContentListsToSite}" />
         </tr:panelHorizontalLayout>
            
			<tr:table var="row" value="#{sys_design_navigation.currentNode.eclist.candidateContentLists}" 
					rows="#{sys_design_navigation.currentNode.eclist.pageRows}"
					id="selectCList" 
					partialTriggers="limitToSiteUnassigned"
					width="100%" rowBandingInterval="1">
		         
				<tr:column width="23px">				
					<tr:selectBooleanCheckbox value="#{row.selected}" />
				</tr:column>
				<tr:column headerText="Content List Name">
					<tr:commandLink text="#{row.clist.name}" />
				</tr:column>
				<tr:column headerText="Description">
					<tr:commandLink text="#{row.clist.description}" />
				</tr:column>
			</tr:table>
			<tr:message rendered="#{sys_design_navigation.currentNode.eclist.selectWarning || (empty sys_design_navigation.currentNode.eclist.candidateContentLists)}" 
			messageType="error" message="Select Content Lists" />

         <tr:separator/>
         
         <tr:panelHorizontalLayout valign="center" halign="start" >
	         <tr:selectOneChoice label="Delivery Context"
	            value="#{sys_design_navigation.currentNode.eclist.deliveryContext}"
				   required="true">
	            <f:selectItems 
	               value="#{sys_design_navigation.currentNode.eclist.candidateContexts}" />
	         </tr:selectOneChoice>                              
         </tr:panelHorizontalLayout>
         
         <tr:panelHorizontalLayout valign="center" halign="start" >
	         <tr:selectOneChoice label="Assembly Context"
	            value="#{sys_design_navigation.currentNode.eclist.assemblyContext}"
	            unselectedLabel="">
	            <f:selectItems 
	               value="#{sys_design_navigation.currentNode.eclist.candidateContexts}" />
	         </tr:selectOneChoice> 
         </tr:panelHorizontalLayout>

         <tr:panelHorizontalLayout valign="center" halign="start" >
         	<tr:outputLabel value="Authtype" for="selectAuthType"/>
         	<tr:spacer width="48" height="1"/>
	         <tr:selectOneChoice 
	         	id="selectAuthType"
	            value="#{sys_design_navigation.currentNode.eclist.authType}"
	            unselectedLabel="">
	            <f:selectItems 
	               value="#{sys_design_navigation.currentNode.eclist.candidateAuthTypes}" />
	         </tr:selectOneChoice>
         </tr:panelHorizontalLayout>
        	<tr:message messageType="info" message="(this is ignored if 'Item Filter' of the Content List is defined)" />
      </tr:panelFormLayout>     
  </jsp:body>
</layout:publishing>
