<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
   prefix="rxcomp"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Delete Location Scheme" />
<layout:publishing>
   <jsp:body>
   <rxcomp:menubar>
      <rxcomp:menuitem value="Delete" 
         action="#{sys_design_navigation.currentNode.deleteSchemeCompletion}" />
      <rxcomp:menuitem value="Cancel" 
         action="#{sys_design_navigation.currentNode.cancelSchemeAction}" />
   </rxcomp:menubar>
   <rxnav:listbreadcrumbs />
      <tr:panelBox inlineStyle="width: 50%">
         <tr:outputText  
               value="The Location Scheme '#{sys_design_navigation.currentNode.selectedScheme.nameWithId}' 
 will be permanently deleted from Context '#{sys_design_navigation.currentNode.title}'.
 Click on 'Delete' to do this or cancel to return to the previous page." />
      </tr:panelBox>
  </jsp:body>
</layout:publishing>
