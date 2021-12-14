<%@page errorPage="/ui/error.jsp"  pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr" %>
<%@ taglib uri="http://myfaces.apache.org/trinidad/html" prefix="trh" %>
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Rhythmyx - Location Scheme Legacy Editor"/>
<layout:publishing>
  <jsp:body>
      <rxcomp:menubar>
         <rxcomp:menu label="Action">
            <rxcomp:menuitem value="Add Parameter..."
               action="#{sys_design_navigation.currentNode.scheme.createParameter}" />
            <rxcomp:menuitem value="Move Up Selected Parameter" 
               action="#{sys_design_navigation.currentNode.scheme.moveUpParameter}" />
            <rxcomp:menuitem value="Move Down Selected Parameter" 
               action="#{sys_design_navigation.currentNode.scheme.moveDownParameter}" />
            <rxcomp:menuitem value="Delete Selected Parameter" 
               action="#{sys_design_navigation.currentNode.scheme.removeParameter}"/>
         </rxcomp:menu>
      
         <rxcomp:menuitem value="Done" 
            action="#{sys_design_navigation.currentNode.scheme.done}" />
         <rxcomp:menuitem value="Cancel" immediate="true"
            action="#{sys_design_navigation.currentNode.scheme.cancel}" />        
         <rxcomp:menuitem value="Help" 
	  			onclick="openHelpWindow('#{sys_design_navigation.currentNode.scheme.helpFile}')"/>

      </rxcomp:menubar>
      <rxnav:editorbreadcrumbs/>
      <tr:panelFormLayout>
         <tr:inputText label="Name" 
            maximumLength="50"
            value="#{sys_design_navigation.currentNode.scheme.name}"
            validator="#{sys_design_navigation.currentNode.scheme.validate}"
				required="true">
            <f:validator validatorId="com.percussion.jsf.name"/>
         </tr:inputText>
         <tr:inputText label="Description" rows="4" columns="80"
            maximumLength="255"
            value="#{sys_design_navigation.currentNode.scheme.description}" />
         <tr:selectOneChoice label="Content Type"
         	id="locationSchemeContentType"
            value="#{sys_design_navigation.currentNode.scheme.contentType}"
            required="true"
            immediate="true"
            valueChangeListener="#{sys_design_navigation.currentNode.scheme.contentTypeChanged}"
            autoSubmit="true"
            >
            <f:selectItems
               value="#{sys_design_navigation.currentNode.scheme.contentTypes}" />
         </tr:selectOneChoice>
         <tr:selectOneChoice label="Template"
         	id="templateName"
            partialTriggers="locationSchemeContentType"
            value="#{sys_design_navigation.currentNode.scheme.template}"
            required="true"
            unselectedLabel=""
            >
            <f:selectItems
               value="#{sys_design_navigation.currentNode.scheme.templates}" />
         </tr:selectOneChoice>
         <tr:selectOneChoice label="Generator"
            value="#{sys_design_navigation.currentNode.scheme.generator}"
            required="true"
            unselectedLabel=""
            >
            <f:selectItems
               value="#{sys_design_navigation.currentNode.scheme.generators}" />
         </tr:selectOneChoice>
	      </tr:panelFormLayout>
         <tr:separator/>
			<tr:panelHeader styleClass="rxPanelHeader" text="Parameters">
				<tr:table var="row" 
					value="#{sys_design_navigation.currentNode.scheme.parameters}"
					rows="10" width="100%" rowBandingInterval="1" >
	            <tr:column width="30px">
	               <tr:selectBooleanRadio group="propertylist" 
	                     value="#{row.selected}" />
	            </tr:column>									
					<tr:column headerText="Name">
						<tr:outputText value="#{row.name}"/>
					</tr:column>
					<tr:column headerText="Type">					
			         <tr:selectOneChoice value="#{row.type}" required="true">
			         	<f:selectItem itemLabel="String" itemValue="String"/>
			         	<f:selectItem itemLabel="BackendColumn" itemValue="BackendColumn"/>
			         </tr:selectOneChoice>
					</tr:column>
					<tr:column headerText="Value">
						<tr:inputText value="#{row.value}" required="true"
						columns="80" maximumLength="2100"/>
					</tr:column>
				</tr:table>
			</tr:panelHeader>
  </jsp:body>
</layout:publishing>
