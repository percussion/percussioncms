<%@ include file="/ui/pubruntime/PubRuntimeAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>

<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Rhythmyx - Job Publication Log" />
<layout:pubruntime>
	<jsp:body>	
		<rxcomp:menubar>
			<rxcomp:menuitem value="Done" action="#{sys_runtime_navigation.activeNode.perform}"/>
			<rxcomp:menuitem value="Help" 
				onclick="openHelpWindow('#{sys_runtime_navigation.jobPubLogHelpFile}')"/>
		</rxcomp:menubar>	
		<rxnav:runtimebreadcrumbs/>	
		<tr:panelFormLayout rows="2">
			<tr:inputText label="Job ID:" value="#{sys_runtime_navigation.jobId}" readOnly="true"/>
			<tr:inputText label="Edition:" value="#{sys_runtime_navigation.jobEditionData.name}" readOnly="true"/>
			<tr:inputText label="Start Time:" value="#{sys_runtime_navigation.jobEditionData.start}" readOnly="true"/>
			<tr:inputText label="Elapsed Time:" value="#{sys_runtime_navigation.jobEditionData.elapsed}" readOnly="true"/>
		</tr:panelFormLayout>
		<tr:panelTabbed >
			<tr:showDetailItem text="Items">
			
				<tr:table var="row" value="#{sys_runtime_navigation.jobPubLog.log}"
					rows="#{sys_runtime_navigation.jobPubLog.pageRows}" width="100%"
					rangeChangeListener="#{sys_runtime_navigation.jobPubLog.rowRangeChanged}" 
					inlineStyle="margin-right: 25px"
					rowBandingInterval="1">
					<tr:column sortable="true" sortProperty="contentId"
						headerText="Content Id[Rev]">
						<tr:commandLink text="#{row.itemStatus.contentId}[#{row.itemStatus.revisionId}]" 
							action="#{row.perform}" />
					</tr:column>
					<tr:column sortable="true" sortProperty="location"
						separateRows="true" headerText="Location/Site Folder">
						<tr:outputText value="#{row.itemStatus.location}" />
						<tr:outputText value="#{row.properties.siteFolder}" />
					</tr:column>   
					<tr:column sortable="true" sortProperty="elapsed"
						headerText="Elapsed Time">
						<tr:outputText value="#{row.properties.elapsed}" />
					</tr:column> 
					<tr:column sortable="true" sortProperty="operation"
						headerText="Operation">
						<tr:outputText value="#{row.properties.operation}" />
					</tr:column>
					<tr:column sortable="true" sortProperty="status" headerText="Status">
						<tr:outputText value="#{row.properties.status}" />
					</tr:column>
					<tr:column sortable="true" sortProperty="deliveryType" headerText="Delivery Type">
						<tr:outputText value="#{row.itemStatus.deliveryType}" />
					</tr:column>
					<tr:column headerText="Template">
						<tr:outputText value="#{row.properties.template}"/>
					</tr:column>
					
				</tr:table>
			</tr:showDetailItem>
			<tr:showDetailItem text="Tasks" rendered="#{! empty sys_runtime_navigation.jobPubLog.tasks}">         			
				<tr:table var="row" value="#{sys_runtime_navigation.jobPubLog.tasks}"
					rows="26" width="100%" inlineStyle="margin-right: 25px"
					rowBandingInterval="1">
					<tr:column sortable="true" sortProperty="referenceid"
						headerText="Reference Id">
						<tr:outputText value="#{row.referenceid}" />
					</tr:column>  
					<tr:column headerText="Task">
						<tr:outputText value="#{row.taskname}" />
					</tr:column>
					<tr:column headerText="Status">
						<tr:outputText value="#{row.status}" />
					</tr:column>
					<tr:column headerText="Elapsed">
						<tr:outputText value="#{row.elapsed}" />
					</tr:column>					
					<tr:column headerText="Message">
						<tr:outputText value="#{row.message}" />
					</tr:column>					
				</tr:table>
			</tr:showDetailItem>			
		</tr:panelTabbed>
	</jsp:body>
</layout:pubruntime>