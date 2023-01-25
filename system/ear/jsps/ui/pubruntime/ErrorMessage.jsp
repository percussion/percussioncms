<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Error Message Page" />
<layout:pubruntime>
	<jsp:body>
   <rxcomp:menubar>
      <rxcomp:menuitem value="OK" 
         action="#{sys_runtime_navigation.currentNode.perform}" />
   </rxcomp:menubar>
   <rxnav:runtimebreadcrumbs />
	      <tr:panelBox inlineStyle="width: 50%">
	         <tr:outputText value="#{sys_runtime_navigation.currentNode.errorMessage}." />
	      </tr:panelBox>
         <tr:inputText readOnly="true" value="Click on 'OK' to return to the previous page." />
  </jsp:body>
</layout:pubruntime>
