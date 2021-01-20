<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
   
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

<c:set var="page_title" scope="request" value="Scheduled Task List"/>

<layout:admin>
  <jsp:body>
    <rxcomp:menubar>
      <rxcomp:menu label="Action">
         <rxcomp:menuitem value="Create Task"
               action="#{sys_admin_navigation.collectionNode.createEvent}"/>
         <rxcomp:menuitem value="Copy Selected Task"
               action="#{sys_admin_navigation.collectionNode.copy}"/>
         <rxcomp:menuitem value="Edit Selected Task" 
            action="#{sys_admin_navigation.collectionNode.edit}"/>
         <rxcomp:menuitem value="Delete Selected Task" 
               action="#{sys_admin_navigation.collectionNode.delete}"/>
      </rxcomp:menu>
      <rxcomp:menuitem value="Help" 
		   	onclick="openHelpWindow('#{sys_admin_navigation.collectionNode.helpFile}')"/>
    </rxcomp:menubar>
    <rxnav:adminbreadcrumbs/>
    <tr:table var="row" value="#{sys_admin_navigation.list}" rows="25"  
        width="100%" rowBandingInterval="1" >
      <tr:column width="23px">
         <tr:selectBooleanRadio group="selectedrow" value="#{row.selectedRow}" />
      </tr:column>         
      <tr:column sortable="true" sortProperty="title" headerText="Name (id)">
         <tr:commandLink text="#{row.nameWithId}" action="#{row.perform}"/>
      </tr:column>
      <tr:column sortProperty="event.cronSpecification" headerText="Cron Specification">
         <tr:outputText value="#{row.event.cronSpecification}"/>
      </tr:column>
    </tr:table>
  </jsp:body>
</layout:admin>
