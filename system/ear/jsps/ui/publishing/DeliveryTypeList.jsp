<%@ include file="/ui/publishing/PubDesignAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Delivery Type View" />
<layout:publishing>
   <jsp:body>
	<rxcomp:menubar>
		<rxcomp:menu label="Action">
			<rxcomp:menuitem value="Create Delivery Type"
               action="#{sys_design_navigation.collectionNode.create}" />
         <rxcomp:menuitem value="Edit Selected Delivery Type"
               action="#{sys_design_navigation.collectionNode.edit}" />
         <rxcomp:menuitem value="Delete Selected Delivery Type"
               action="#{sys_design_navigation.collectionNode.delete}" />              		
		</rxcomp:menu>
		<rxcomp:menuitem value="Help" 
		   		onclick="openHelpWindow('#{sys_design_navigation.collectionNode.helpFile}')"/>
	</rxcomp:menubar>
	<rxnav:listbreadcrumbs />
	<tr:table var="row" value="#{sys_design_navigation.list}" 
			rows="#{sys_design_navigation.collectionNode.pageRows}"
         width="100%" rowBandingInterval="1">
		<tr:column width="23px">
			<tr:selectBooleanRadio group="selectedrow" value="#{row.selectedRow}" />
		</tr:column>		
		<tr:column sortable="true" sortProperty="nameWithId" headerText="Name (Id)">
			<tr:commandLink text="#{row.nameWithId}" action="#{row.perform}" />
		</tr:column>
		<tr:column headerText="Description">
			<tr:outputText value="#{row.description}" />
		</tr:column>        	
	</tr:table>
 </jsp:body>
</layout:publishing>
