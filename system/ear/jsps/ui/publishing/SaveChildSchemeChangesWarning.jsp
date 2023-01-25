<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Save Child Scheme Changes Warning Page" />
<layout:publishing>
	<jsp:body>
   <rxcomp:menubar>
      <rxcomp:menuitem value="Yes" 
         action="#{sys_design_navigation.currentNode.save}" />
      <rxcomp:menuitem value="No" 
         action="#{sys_design_navigation.currentNode.discardChanges}" />
   </rxcomp:menubar>
   <rxnav:listbreadcrumbs />
      <tr:panelBox inlineStyle="width: 50%">
         <tr:outputText value="Location Schemes may have been modified  
            in '#{sys_design_navigation.currentNode.title}' Context. 
		      Please click 'Yes' to save the changes or click 'No' to discard 
		      the changes." />
      </tr:panelBox>
  </jsp:body>
</layout:publishing>
