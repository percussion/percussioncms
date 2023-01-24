<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
   


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
