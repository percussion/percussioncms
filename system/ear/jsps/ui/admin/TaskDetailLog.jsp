<%@ page errorPage="/ui/error.jsp"
   import="com.percussion.server.*,com.percussion.design.objectstore.PSAclEntry"
   pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
   


<c:set var="page_title" scope="request" value="Task Detail Log"/>

<%
IPSRequestContext reqCtx = (IPSRequestContext) request
   .getAttribute("RX_REQUEST_CONTEXT");
PSServer.checkAccessLevel(reqCtx.getSecurityToken(),
   PSAclEntry.SACE_ADMINISTER_SERVER);
%>

<layout:admin>
   <jsp:body>   
      <rxcomp:menubar>
         <rxcomp:menuitem value="Done" action="#{sys_admin_navigation.currentNode.perform}"/>
			<rxcomp:menuitem value="Help" 
				onclick="openHelpWindow('#{sys_admin_navigation.currentNode.detailLog.helpFile}')"/>                  
      </rxcomp:menubar>   
      <rxnav:adminbreadcrumbs/>   

      <tr:panelFormLayout>
         <tr:inputText label="Task Name:"
               value="#{sys_admin_navigation.currentNode.detailLog.taskName}"/>
         <tr:inputText label="Start Time:"
            value="#{sys_admin_navigation.currentNode.detailLog.startTime}"/>
         <tr:inputText label="Elapsed (HH:mm:ss):"
               value="#{sys_admin_navigation.currentNode.detailLog.elapsed}"/>
         <tr:inputText label="Status:"
               value="#{sys_admin_navigation.currentNode.detailLog.success ? 'Success' : 'Failed'}"/>
         <tr:inputText label="Server:"
               value="#{sys_admin_navigation.currentNode.detailLog.server}"/>
         <tr:inputText label="Detail Message"
	         	rows="4" columns="80" readOnly="true"
	            value="#{sys_admin_navigation.currentNode.detailLog.detailMessage}" />
      </tr:panelFormLayout>
   </jsp:body>
</layout:admin>
