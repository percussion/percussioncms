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

<c:set var="page_title" scope="request" value="Rhythmyx - Content List Editor"/>
<layout:publishing>
  <jsp:body>
      <rxcomp:menubar>
         <rxcomp:menuitem value="Save" 
            action="#{sys_design_navigation.currentNode.save}" />
         <rxcomp:menuitem value="Cancel" immediate="true"
            action="#{sys_design_navigation.currentNode.cancel}" /> 
			<rxcomp:menuitem value="Help" 
				onclick="openHelpWindow('#{sys_design_navigation.currentNode.helpFile}')"/>
      </rxcomp:menubar>
      <rxnav:editorbreadcrumbs/>
      <tr:panelFormLayout>
         <tr:inputText label="Name" 
            maximumLength="100"
            value="#{sys_design_navigation.currentNode.contentList.name}"
            validator="#{sys_design_unique_name_validator.validate}"
			required="true">
            <f:validator validatorId="com.percussion.jsf.name"/>
         </tr:inputText>
         <tr:inputText label="Description" rows="4" columns="80"
            maximumLength="255"
            value="#{sys_design_navigation.currentNode.contentList.description}" />
         <tr:inputText label="URL" columns="80"
            maximumLength="2100"
            rendered="#{sys_design_navigation.currentNode.legacy}"
            value="#{sys_design_navigation.currentNode.contentList.url}" />
         <tr:selectBooleanCheckbox label="Incremental" 
            rendered="#{! sys_design_navigation.currentNode.legacy}"
            value="#{sys_design_navigation.currentNode.incremental}"/>
         <tr:selectOneChoice label="Delivery Type"
            rendered="#{! sys_design_navigation.currentNode.legacy}"
            value="#{sys_design_navigation.currentNode.urlParams.sys_deliverytype}">
             <f:selectItems value="#{sys_design_navigation.currentNode.deliveryTypes}" />
         </tr:selectOneChoice>
         <tr:selectOneChoice label="Item Filter"
            rendered="#{! sys_design_navigation.currentNode.legacy}"
            unselectedLabel="" 
            value="#{sys_design_navigation.currentNode.filterName}">
             <f:selectItems value="#{sys_design_navigation.currentNode.pubui.filters}"/>
         </tr:selectOneChoice>  
         <tr:selectOneChoice label="Generator"
            rendered="#{! sys_design_navigation.currentNode.legacy}"
            onchange="submit()"
            unselectedLabel=""
            valueChangeListener="#{sys_design_navigation.currentNode.selectValueChanged}"
            value="#{sys_design_navigation.currentNode.generator}">
            <f:selectItems value="#{sys_design_navigation.currentNode.pubui.generators}"/>
         </tr:selectOneChoice>   
         <tr:table rows="5" width="100%"
            rendered="#{(! sys_design_navigation.currentNode.legacy) && (! empty sys_design_navigation.currentNode.generator) && (! empty sys_design_navigation.currentNode.generatorArguments)}"
            value="#{sys_design_navigation.currentNode.generatorArguments}" var="arg">
            <tr:column headerText="Name">
               <tr:outputText value="#{arg.name}"/>
            </tr:column>
            <tr:column headerText="Value" separateRows="true">
           <tr:inputText value="#{arg.value}" columns="80" maximumLength="4000"/>
           <tr:outputText value="#{arg.description}"/>
            </tr:column>
         </tr:table>  
         <tr:selectOneChoice label="Template Expander"
            rendered="#{! sys_design_navigation.currentNode.legacy}"
            onchange="submit()"
            unselectedLabel=""
            valueChangeListener="#{sys_design_navigation.currentNode.selectValueChanged}"
            value="#{sys_design_navigation.currentNode.expander}">
            <f:selectItems value="#{sys_design_navigation.currentNode.pubui.templateExpanders}"/>
         </tr:selectOneChoice>   
         <tr:table rows="5" width="100%"
            rendered="#{(! sys_design_navigation.currentNode.legacy) && (! empty sys_design_navigation.currentNode.expander) && (! empty sys_design_navigation.currentNode.expanderArguments)}"
            value="#{sys_design_navigation.currentNode.expanderArguments}" var="arg">
            <tr:column headerText="Name">
               <tr:outputText value="#{arg.name}"/>
            </tr:column>
            <tr:column headerText="Value" separateRows="true">
           <tr:inputText value="#{arg.value}" columns="80" maximumLength="4000" />
           <tr:outputText value="#{arg.description}"/>
            </tr:column>
         </tr:table>    
      </tr:panelFormLayout>
     <tr:showDetail disclosed="#{! empty sys_design_navigation.currentNode.urlExtraParams}" id="disclose_details" immediate="true"
         rendered="#{! sys_design_navigation.currentNode.legacy}"
         undisclosedText="Show Additional Details"
         disclosedText="Hide Additional Details" >
         <tr:panelFormLayout styleClass="pub-edit-detailbox">
            <tr:inputText label="Base Resource Path" 
               value="#{sys_design_navigation.currentNode.urlBase}" columns="50" 
               maximumLength="1100" />
            <tr:inputText label="Extra Parameters" 
               value="#{sys_design_navigation.currentNode.urlExtraParams}" 
               columns="80" maximumLength="1000" />               
         </tr:panelFormLayout>
      </tr:showDetail>
  </jsp:body>
</layout:publishing>
