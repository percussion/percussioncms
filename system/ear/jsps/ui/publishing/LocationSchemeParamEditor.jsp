<%@page errorPage="/ui/error.jsp"  pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr" %>
<%@ taglib uri="http://myfaces.apache.org/trinidad/html" prefix="trh" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
   prefix="rxcomp"%>
   


<c:set var="page_title" scope="request" value="Rhythmyx - Location Scheme Parameter Editor"/>
<layout:publishing>
  <jsp:body>
      <rxcomp:menubar>
         <rxcomp:menuitem value="Done" 
            action="#{sys_design_navigation.currentNode.scheme.addCreatedParameter}" />
         <rxcomp:menuitem value="Cancel" immediate="true"
            action="#{sys_design_navigation.currentNode.scheme.perform}" />        
         <rxcomp:menuitem value="Help" 
	  			onclick="openHelpWindow('#{sys_design_navigation.currentNode.scheme.createdParameter.helpFile}')"/>
      </rxcomp:menubar>
      <rxnav:editorbreadcrumbs/>
      <tr:panelFormLayout>
         <tr:inputText label="Name" 
            maximumLength="50"
            value="#{sys_design_navigation.currentNode.scheme.createdParameter.name}"
				required="true">
            <f:validator validatorId="com.percussion.jsf.name"/>
         </tr:inputText>
         <tr:selectOneChoice label="Type"
            value="#{sys_design_navigation.currentNode.scheme.createdParameter.type}" 
            required="true">
            <f:selectItem itemLabel="String" itemValue="String"/>
            <f:selectItem itemLabel="BackendColumn" itemValue="BackendColumn"/>
         </tr:selectOneChoice>
         <tr:inputText label="Value" 
            value="#{sys_design_navigation.currentNode.scheme.createdParameter.value}" 
            required="true"
            columns="80" maximumLength="2100"/>
	      </tr:panelFormLayout>
  </jsp:body>
</layout:publishing>
