<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Delete Event Log Warning" />
<layout:publishing>
	<jsp:body>
   <rxcomp:menubar>
      <rxcomp:menuitem value="Yes" 
         action="#{sys_admin_navigation.activeNode.deleteAllLogs}" />
      <rxcomp:menuitem value="No" 
         action="#{sys_admin_navigation.activeNode.perform}" />
   </rxcomp:menubar>
   <rxnav:adminbreadcrumbs/>
   <tr:panelBox inlineStyle="width: 50%">
      <tr:outputText value="All task log entries will be deleted.
      Click 'Yes' to save the changes or click 'No' to cancel this action" />
   </tr:panelBox>
  </jsp:body>
</layout:publishing>
