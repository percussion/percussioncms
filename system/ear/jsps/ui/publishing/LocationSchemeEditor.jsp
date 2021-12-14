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

<c:set var="page_title" scope="request" value="Rhythmyx - Location Scheme Editor"/>
<layout:publishing>
  <jsp:body>
      <rxcomp:menubar>
         <rxcomp:menuitem value="#{sys_design_navigation.currentNode.scheme.new ? 'Add' : 'Done'}" 
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
         <tr:inputText label="JEXL Expression" rows="8" columns="80"
            maximumLength="2100"
            required="true"
            value="#{sys_design_navigation.currentNode.scheme.expression}" />
      </tr:panelFormLayout>
      <tr:separator/>
      <tr:showDetail disclosed="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.panelEmpty ? false : true}"
         immediate="true" 
         undisclosedText="Show Test Panel"
         disclosedText="Hide Test Panel" >
         <tr:panelFormLayout styleClass="pub-edit-detailbox">
	         <tr:selectOneChoice label="Site"
	            value="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.siteId}"
	            >
	            <f:selectItems
	               value="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.sites}" />
	         </tr:selectOneChoice>
            <tr:inputText label="Item Path" columns="80"
               value="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.itemPath}" />
            <tr:commandButton text="..."
               action="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.browseItem}" />
            <tr:inputText label="Extra Parameters" columns="80"
               value="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.extraParameters}" />
            <tr:separator/>
            <tr:commandButton text="Evaluate JEXL Expression"
               action="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.evaluateExpression}" />
            <tr:separator/>
            <tr:inputText label="Result" rows="4" columns="80"
               value="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.evaluateResult}" />            
            <tr:inputText label="Status" 
               value="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.evaluateStatus}" />
         </tr:panelFormLayout>
      </tr:showDetail>
      <tr:separator/>
      <tr:panelHorizontalLayout valign="center" halign="start"
         inlineStyle="padding: 5px 0px 5px 0px;">
	      <tr:inputText label="Filter" value="#{sys_design_navigation.currentNode.scheme.jexlFilter}"
	            onkeypress="if ((window.event && window.event.keyCode == 13) || (event && event.which == 13)) this.form.submit(); else return true;" />
	      <tr:commandButton text="Apply" />
	      <tr:commandButton text="Clear"
	            action="#{sys_design_navigation.currentNode.scheme.clearJexlFilter}" />
	      <tr:message messageType="info"
	            message="('#{empty sys_design_navigation.currentNode.scheme.jexlFilter ? '' : sys_design_navigation.currentNode.scheme.jexlFilter}' filter applied)" />
      </tr:panelHorizontalLayout>      
      <tr:panelHeader styleClass="rxPanelHeader" text="Predefined JEXL variables and methods">
         <tr:table var="row" immediate="true" 
            value="#{sys_design_navigation.currentNode.scheme.filteredJexlMethods}"
            rows="10" width="100%" rowBandingInterval="1" >
            <tr:column sortable="true" sortProperty="name" headerText="Method">
               <tr:outputText value="#{row.name}"/>
            </tr:column>
            <tr:column headerText="Description">
               <tr:outputText value="#{row.description}"/>
            </tr:column>
         </tr:table>
      </tr:panelHeader>
      
  </jsp:body>
</layout:publishing>
