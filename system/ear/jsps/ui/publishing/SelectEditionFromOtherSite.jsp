<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Rhythmyx - Select Edition From Other Site" />
<layout:publishing>
	<jsp:body>
   <rxcomp:menubar>
         <rxcomp:menuitem value="Done" 
            action="#{sys_design_navigation.collectionNode.copyEditionFromOtherSite}" />
         <rxcomp:menuitem value="Cancel" 
            action="pub-design-edition-views" immediate="true"/>
         <rxcomp:menuitem value="Help" />
      </rxcomp:menubar>
     <rxnav:editorbreadcrumbs />
     <tr:panelHeader text="Select an Edition from other Sites" inlineStyle="margin-top: 10px"/>
     <tr:panelHorizontalLayout valign="top" halign="start" >
        <tr:inputText label="Filter:" value="#{sys_design_navigation.collectionNode.editionListFilter}" />
        <tr:commandButton text="Filter" />
        <tr:spacer width="40"/>
         <tr:selectBooleanCheckbox label="Copy Content Lists of the Selected Edition:"
            value="#{sys_design_navigation.collectionNode.deepClone}" />
        
     </tr:panelHorizontalLayout>
	  <tr:table var="row" value="#{sys_design_navigation.collectionNode.editionsFromOtherSites}" rows="15"  
			width="100%" rowBandingInterval="1" >
			<tr:column headerText="Sel">
				<tr:selectBooleanRadio group="selected" value="#{row.selected}" />
			</tr:column>		
			<tr:column sortable="true" sortProperty="name" headerText="Name">
				<tr:commandLink text="#{row.nameWithId}" />
			</tr:column>
			<tr:column sortable="true" sortProperty="comment" headerText="Description">
				<tr:commandLink text="#{row.comment}" />
			</tr:column>
			<tr:column sortable="true" sortProperty="editionType" headerText="Behavior">
				<tr:commandLink text="#{row.editionType}" />
			</tr:column>
			<tr:column sortable="true" sortProperty="siteName" headerText="Site">
				<tr:commandLink text="#{row.siteNameWithID}" />
			</tr:column>
     </tr:table>
   </jsp:body>
</layout:publishing>
