<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
   


<c:set var="page_title" scope="request" value="Task Notification List"/>

<layout:admin>
  <jsp:body>
    <rxcomp:menubar>
      <rxcomp:menu label="Action">
         <rxcomp:menuitem value="Create Task Notification"
               action="#{sys_admin_navigation.collectionNode.createNotification}"/>
         <rxcomp:menuitem value="Edit Selected Task Notification" 
            action="#{sys_admin_navigation.collectionNode.edit}"/>
         <rxcomp:menuitem value="Delete Selected Task Notification" 
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
    </tr:table>
  </jsp:body>
</layout:admin>
