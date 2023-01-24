<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad/html" prefix="trh"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Select Edition From Other Site" />
<layout:publishing>
	<jsp:body>
   <rxcomp:menubar>
         <rxcomp:menuitem value="Done" 
            action="#{sys_design_navigation.collectionNode.copyEditionFromOtherSite}" />
         <rxcomp:menuitem value="Cancel" 
            action="pub-design-edition-views" immediate="true"/>
         <rxcomp:menuitem value="Help" />
      </rxcomp:menubar>
     <rxnav:editorbreadcrumbs />
     <tr:panelHeader text="Select an Edition from other Sites" inlineStyle="margin-top: 10px"/>
     <tr:panelHorizontalLayout valign="top" halign="start" >
        <tr:inputText label="Filter:" value="#{sys_design_navigation.collectionNode.editionListFilter}" />
        <tr:commandButton text="Filter" />
        <tr:spacer width="40"/>
         <tr:selectBooleanCheckbox label="Copy Content Lists of the Selected Edition:"
            value="#{sys_design_navigation.collectionNode.deepClone}" />
        
     </tr:panelHorizontalLayout>
	  <tr:table var="row" value="#{sys_design_navigation.collectionNode.editionsFromOtherSites}" rows="15"  
			width="100%" rowBandingInterval="1" >
			<tr:column headerText="Sel">
				<tr:selectBooleanRadio group="selected" value="#{row.selected}" />
			</tr:column>		
			<tr:column sortable="true" sortProperty="name" headerText="Name">
				<tr:commandLink text="#{row.nameWithId}" />
			</tr:column>
			<tr:column sortable="true" sortProperty="comment" headerText="Description">
				<tr:commandLink text="#{row.comment}" />
			</tr:column>
			<tr:column sortable="true" sortProperty="editionType" headerText="Behavior">
				<tr:commandLink text="#{row.editionType}" />
			</tr:column>
			<tr:column sortable="true" sortProperty="siteName" headerText="Site">
				<tr:commandLink text="#{row.siteNameWithID}" />
			</tr:column>
     </tr:table>
   </jsp:body>
</layout:publishing>
