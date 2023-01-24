<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8"
   contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Edition List View" />
<layout:publishing>
   <jsp:body>
	<rxcomp:menubar>
		<rxcomp:menu label="Action">
			<rxcomp:menuitem value="Create Edition"
               action="#{sys_design_navigation.collectionNode.createEdition}" />
         <rxcomp:menuitem value="Edit Selected Edition"
               action="#{sys_design_navigation.collectionNode.edit}" />
			<rxcomp:menuitem value="Copy Selected Edition"
               action="#{sys_design_navigation.collectionNode.copy}" />
			<rxcomp:menuitem value="Copy Edition From Other Site"
               action="#{sys_design_navigation.collectionNode.selectEditionFromOtherSite}" />
         <rxcomp:menuitem value="Delete Selected Edition"
               action="#{sys_design_navigation.collectionNode.delete}" />              		
		</rxcomp:menu>
		<rxcomp:menuitem value="Help" 
		   		onclick="openHelpWindow('#{sys_design_navigation.collectionNode.helpFile}')"/>
	</rxcomp:menubar>
	<rxnav:listbreadcrumbs />
   <tr:panelHorizontalLayout valign="center" halign="start">
      <tr:inputText label="Filter" value="#{sys_design_navigation.filter}"
            onkeypress="if ((window.event && window.event.keyCode == 13) || (event && event.which == 13)) this.form.submit(); else return true;" />
      <tr:commandButton text="Apply" />
      <tr:commandButton text="Clear"
            action="#{sys_design_navigation.clearFilter}" />
      <tr:message messageType="info"
            message="('#{empty sys_design_navigation.filter ? '' : sys_design_navigation.filter}' filter applied)" />
   </tr:panelHorizontalLayout>
	<tr:table var="row" value="#{sys_design_navigation.list}" 
		   binding="#{sys_design_navigation.collectionNode.table}" 
			rows="#{sys_design_navigation.collectionNode.pageRows}"
         width="100%" rowBandingInterval="1">
		<tr:column width="23px">
			<tr:selectBooleanRadio group="selectedrow" value="#{row.selectedRow}" />
		</tr:column>		
		<tr:column sortable="true" sortProperty="nameWithId" headerText="Name (Id)">
			<tr:commandLink text="#{row.nameWithId}" action="#{row.perform}" />
		</tr:column>
		<tr:column headerText="Description">
			<tr:outputText value="#{row.properties.description}" />
		</tr:column>        	
		<tr:column headerText="Behavior">
			<tr:outputText value="#{row.properties.type}" />
		</tr:column>
	</tr:table>
 </jsp:body>
</layout:publishing>
