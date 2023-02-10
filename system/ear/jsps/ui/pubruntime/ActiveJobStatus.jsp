<%@ include file="/ui/pubruntime/PubRuntimeAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Runtime Edition List" />

<layout:pubruntime>
	<jsp:body>
		<rxcomp:menubar>
			<rxcomp:menuitem value="Help" 
				onclick="openHelpWindow('#{sys_runtime_navigation.startingNode.helpFile}')"/>
		</rxcomp:menubar>	
		<rxnav:runtimebreadcrumbs/>
      <tr:poll interval="6000" id="pollJobProgress" />
		<tr:panelHeader styleClass="rxPanelHeader" text="Running Editions">
         <tr:table var="entry" width="100%" rowBandingInterval="1" rows="25"
            partialTriggers="pollJobProgress" 
            value="#{sys_runtime_navigation.startingNode.activeJobStatus}">
            <tr:column sortable="true" sortProperty="editionNameWithId" headerText="Edition">
               <tr:commandLink text="#{entry.editionNameWithId}"
                  action="#{entry.statusEntry.viewLog}"/>
            </tr:column>
            <tr:column sortable="true" sortProperty="editionBehavior" headerText="Behavior">
               <tr:outputText value="#{entry.editionBehavior}"/>
            </tr:column>
            <tr:column sortable="true" sortProperty="status" headerText="Status">
            	<tr:panelHorizontalLayout>
	               <tr:image source="#{entry.statusImage}"/>
	               <tr:outputText value="#{entry.status}"/>
               </tr:panelHorizontalLayout>
            </tr:column>
            <tr:column sortable="true" sortProperty="progress"  headerText="Progress">
               <rxcomp:progressBar percent="#{entry.progress}" 
                  rendered="#{! entry.isTerminal}"/>
            </tr:column>
            <tr:column sortable="true" sortProperty="terminated"  width="23px">
               <tr:commandLink rendered="#{! entry.isTerminal}"
				      action="#{entry.statusEntry.stopJob}">
				      <tr:image source="../../sys_resources/images/stop.png" shortDesc="Stop"/>
               </tr:commandLink>
            </tr:column>
         </tr:table>
      </tr:panelHeader>
	</jsp:body>
</layout:pubruntime>
