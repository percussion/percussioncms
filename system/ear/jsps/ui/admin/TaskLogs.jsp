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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Task Logs"/>

<layout:admin>
  <jsp:body>
    <rxcomp:menubar>
      <rxcomp:menu label="Action">
         <rxcomp:menuitem value="Delete All Task Logs" 
            action="admin-delete-all-event-log-warning"/>
         <rxcomp:menuitem value="Delete Selected Task Logs" 
            action="#{sys_admin_navigation.currentNode.deleteLogs}"/>
      </rxcomp:menu>
      <rxcomp:menuitem value="Help" 
		   	onclick="openHelpWindow('#{sys_admin_navigation.currentNode.helpFile}')"/>
    </rxcomp:menubar>
    <rxnav:adminbreadcrumbs/>    
    <tr:table var="row" value="#{sys_admin_navigation.currentNode.eventLogs}" 
         rows="#{sys_admin_navigation.currentNode.pageRows}" 
         width="100%" rowBandingInterval="1" >
      <tr:column width="23px">
         <tr:selectBooleanCheckbox value="#{row.selected}" />
      </tr:column>         
      <tr:column sortable="true" sortProperty="taskName" headerText="Task Name">
         <tr:commandLink text="#{row.taskName}" action="#{row.showDetail}"/>
      </tr:column>
      <tr:column sortProperty="startTime" headerText="Start Time">
         <tr:outputText value="#{row.startTime}"/>
      </tr:column>
      <tr:column sortProperty="elapse" headerText="Elapsed (HH:mm:ss)">
         <tr:outputText value="#{row.elapsed}"/>
      </tr:column>
      <tr:column sortProperty="status" headerText="Status">
         <tr:outputText value="#{row.success ? 'Success' : 'Failed'}"/>
      </tr:column>
    </tr:table>
  </jsp:body>
</layout:admin>
