<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Acknowledge Delete Site Item Page" />
<layout:pubruntime>
	<jsp:body>
   <rxcomp:menubar>
      <rxcomp:menuitem value="Continue" 
         action="#{sys_runtime_navigation.currentNode.deleteSiteItems}" />
      <rxcomp:menuitem value="Cancel" 
         action="#{sys_runtime_navigation.currentNode.perform}" />
   </rxcomp:menubar>
   <rxnav:runtimebreadcrumbs />
      <tr:panelBox inlineStyle="width: 75%">
         <tr:outputText value="All Site Item database entries 
         for the '#{sys_runtime_navigation.activeNode.siteName}' site will be deleted. 
         This action must be followed with a full publishing for the site. 
         The Unpublishing and Incremental publishing may not work as expected
         until the full publishing is done. See online help for more information." />
      </tr:panelBox>
      <tr:inputText readOnly="true" value="Click 'Continue' to confirm the delete action
         or click 'Cancel' to cancel this action." />
  </jsp:body>
</layout:pubruntime>
