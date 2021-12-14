<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
	
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Rhythmyx - Runtime Edition View" />
<c:set var="page_onload" scope="request" 
	value="PS.editionrt.startEditionUpdateTimer()" />
<c:set var="page_script" scope="request">
		<script src="/Rhythmyx/sys_resources/js/yui/yahoo/yahoo-min.js"></script>
		<script src="/Rhythmyx/sys_resources/js/yui/event/event-min.js"></script>
		<script src="/Rhythmyx/sys_resources/js/yui/connection/connection-min.js"></script>
		<script src="/Rhythmyx/sys_resources/js/yui/dom/dom-min.js"></script>
		<script src="/Rhythmyx/sys_resources/js/publishing.js"></script>
</c:set>

<layout:pubruntime>
	<jsp:body>			
		<rxcomp:menubar>
			<rxcomp:menu label="Action">
				<rxcomp:menuitem value="Archive Selected Log" 
					action="#{sys_runtime_navigation.currentNode.archiveSelected}" />	
            <rxcomp:menuitem value="Delete Selected Log" 
               action="#{sys_runtime_navigation.currentNode.purgeSelected}" /> 
			</rxcomp:menu>
			<rxcomp:menuitem value="Start" id="start_button"
				action="#{sys_runtime_navigation.currentNode.start}" />
			<rxcomp:menuitem value="Stop" id="stop_button"
				action="#{sys_runtime_navigation.currentNode.stop}" />			
			<rxcomp:menuitem value="Help" 
				onclick="openHelpWindow('#{sys_runtime_navigation.currentNode.helpFile}')"/>
		</rxcomp:menubar>
		<rxnav:runtimebreadcrumbs/>
		<tr:panelBox
			rendered="#{! empty sys_runtime_navigation.currentNode.archiveLocation}">
         <tr:panelHorizontalLayout>
				<tr:inputText readOnly="true" value="The archived log is in the following location:  " />
				<tr:goLink text="#{sys_runtime_navigation.currentNode.archiveLocation}"
				   rendered="#{sys_runtime_navigation.currentNode.remoteViewableArchive}" 
					targetFrame="_blank"
					destination="#{sys_runtime_navigation.currentNode.archiveLink}" />
				<tr:inputText readOnly="true"  
				   rendered="#{! sys_runtime_navigation.currentNode.remoteViewableArchive}"
				   value="#{sys_runtime_navigation.currentNode.archiveLink}" />
         </tr:panelHorizontalLayout>
		</tr:panelBox>
		<tr:inputHidden id="edition_id" 
			value="#{sys_runtime_navigation.currentNode.editionId}"/>
		<tr:panelFormLayout id="job_panel" >
			<tr:panelLabelAndMessage label="Job ID">
				<tr:outputText value="#{sys_runtime_navigation.currentNode.jobId}" />
			</tr:panelLabelAndMessage>
			<tr:panelLabelAndMessage label="Percent Finished">
				<f:verbatim>
					<div id="status_frame" 
						style="width: 400px; height 12px; border: 1px solid black">
						<div id="_status_progress" 
								style="width: 0px; height: 15px; background-color: blue"></div>
					</div>
				</f:verbatim>
			</tr:panelLabelAndMessage>
			<tr:panelLabelAndMessage label="Start Time">
				<tr:outputText id="_start_time" value="" inlineStyle="font-size: smaller"/>
			</tr:panelLabelAndMessage>
			<tr:panelLabelAndMessage label="Elapsed">
				<tr:outputText id="_elapsed_time" value="" inlineStyle="font-size: smaller"/>
			</tr:panelLabelAndMessage>
			<tr:panelLabelAndMessage label="Current State">
				<tr:outputText id="_status_state" value="" inlineStyle="font-size: smaller"/>
			</tr:panelLabelAndMessage>
			<tr:panelLabelAndMessage label="Queued">
				<tr:outputText id="_status_queued" value="" inlineStyle="font-size: smaller"/>
			</tr:panelLabelAndMessage>
         <tr:panelLabelAndMessage label="Prepared for Delivery">
            <tr:outputText id="_status_prepared" value="" inlineStyle="font-size: smaller"/>
         </tr:panelLabelAndMessage>
			<tr:panelLabelAndMessage label="Delivered">
				<tr:outputText id="_status_delivered" value="" inlineStyle="font-size: smaller"/>
			</tr:panelLabelAndMessage>
			<tr:panelLabelAndMessage label="Failed">
				<tr:outputText id="_status_failed" value="" inlineStyle="font-size: smaller"/>
			</tr:panelLabelAndMessage>
		</tr:panelFormLayout>
		<tr:panelHeader  styleClass="rxPanelHeader" text="Logs">
			<tr:table var="entry" width="100%" rowBandingInterval="1" 
				rows="#{sys_runtime_navigation.currentNode.pageRows}" 
				selectedRowKeys="#{sys_runtime_navigation.currentNode.selectedRowKeys}"
			   rowSelection="multiple" 
				value="#{sys_runtime_navigation.currentNode.processedStatusLogs}">
				<tr:column headerText="Job ID" sortable="true" sortProperty="statusid">
					<tr:commandLink text="#{entry.statusid}" 
						action="#{entry.statusentry.perform}"/>
				</tr:column>
				<tr:column headerText="Start Time" sortable="true" sortProperty="startTime">
					<tr:outputText value="#{entry.start}"/>
				</tr:column>
				<tr:column headerText="Elapsed (HH:mm:ss)" sortable="true" sortProperty="elapsed">
					<tr:outputText value="#{entry.elapsed}"/>
				</tr:column>
				<tr:column headerText="Status" width="23px" sortable="true" sortProperty="statusDesc">
			      <tr:image source="#{entry.statusImage}" shortDesc="#{entry.statusDesc}"/>
				</tr:column>
				<tr:column headerText="Delivered" sortable="true" sortProperty="delivered">
					<tr:outputText value="#{entry.delivered}"/>
				</tr:column>
				<tr:column headerText="Removed" sortable="true" sortProperty="removed">
					<tr:outputText value="#{entry.removed}"/>
				</tr:column>
				<tr:column headerText="Failures" sortable="true" sortProperty="failures">
					<tr:outputText value="#{entry.failures}"/>
				</tr:column>
			</tr:table>
		</tr:panelHeader>
	</jsp:body>
</layout:pubruntime>
