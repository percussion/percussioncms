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

<c:set var="page_title" scope="request" value="Rhythmyx - Edition Editor"/>

<layout:publishing>
  <jsp:body>
	  	<rxcomp:menubar>
			<rxcomp:menu label="Action">
				<rxcomp:menuitem value="Add Content List Association..." 
				  action="#{sys_design_navigation.currentNode.addContentList}" />	
				<rxcomp:menuitem value="Edit Selected Content List Association..."
				   action="#{sys_design_navigation.currentNode.editContentList}" />
				<rxcomp:menuitem value="Move Selected Row Up" 
				   action="#{sys_design_navigation.currentNode.moveSelectedUp}" />
				<rxcomp:menuitem value="Move Selected Row Down" 
				   action="#{sys_design_navigation.currentNode.moveSelectedDown}" />
				<rxcomp:menuitem value="Delete Selected Row" 
				  action="#{sys_design_navigation.currentNode.removeSelected}" />			
				<rxcomp:menuitem value="Add Pre Task" immediate="true"
					action="#{sys_design_navigation.currentNode.addPreTask}" />
				<rxcomp:menuitem value="Add Post Task" immediate="true"
					action="#{sys_design_navigation.currentNode.addPostTask}" />
			</rxcomp:menu>
			<rxcomp:menuitem value="Save" 
				action="#{sys_design_navigation.currentNode.save}" />
			<rxcomp:menuitem value="Cancel" immediate="true"
				action="#{sys_design_navigation.currentNode.cancel}" />			
			<rxcomp:menuitem value="Help" 
		   		onclick="openHelpWindow('#{sys_design_navigation.currentNode.helpFile}')"/>
		</rxcomp:menubar>
		<rxnav:editorbreadcrumbs/>
		<tr:panelFormLayout>
			<tr:inputText label="Name" 
				maximumLength="100"
				value="#{sys_design_navigation.currentNode.name}"
				validator="#{sys_design_unique_name_validator.validate}"
				required="true">
				<f:validator validatorId="com.percussion.jsf.name"/>
			</tr:inputText>
			<tr:inputText label="Description" rows="4" columns="80"
				maximumLength="255"
				value="#{sys_design_navigation.currentNode.description}" />
         <tr:selectOneChoice label="Priority"
            value="#{sys_design_navigation.currentNode.priority}">
				<f:selectItem itemValue="5" itemLabel="Highest" />
				<f:selectItem itemValue="4" itemLabel="High" />
				<f:selectItem itemValue="3" itemLabel="Medium" />
				<f:selectItem itemValue="2" itemLabel="Low" />
				<f:selectItem itemValue="1" itemLabel="Lowest" />
         </tr:selectOneChoice>
			<tr:separator/>
			<tr:selectOneRadio label="Behavior"
				value="#{sys_design_navigation.currentNode.editionType}">
				<tr:selectItem value="2" label="Publish" />
				<tr:selectItem value="5" label="Unpublish Then Publish" />
			</tr:selectOneRadio>
		</tr:panelFormLayout>
		<tr:panelHeader styleClass="rxPanelHeader" text="Content Lists" inlineStyle="margin-top: 10px">
			<tr:table var="var" immediate="true" 
				value="#{sys_design_navigation.currentNode.contentLists}"
				rows="15" width="100%" rowBandingInterval="1" >
				<tr:column width="23px">
					<tr:selectBooleanRadio group="selectOne" value="#{var.selected}" />
				</tr:column>
				<tr:column headerText="Name (Id)">
					<tr:outputText value="#{var.contentListNameWithId}"/>
				</tr:column>
				<tr:column headerText="Delivery Context">
					<tr:outputText value="#{var.deliverycontext}"/>
				</tr:column>
				<tr:column headerText="Assembly Context">
					<tr:outputText value="#{var.assemblycontext}"/>
				</tr:column>
            <tr:column headerText="Preview">            
	            <tr:goLink onclick="previewContentList('#{var.clistURL}')">
	              <tr:image source="../../sys_resources/images/preview.gif" shortDesc="Preview"/>
            </tr:goLink>
            </tr:column>
			</tr:table>
		</tr:panelHeader>
		<tr:panelHeader styleClass="rxPanelHeader" text="Pre Tasks" inlineStyle="margin-top: 10px" 
			rendered="#{sys_design_navigation.currentNode.hasPreTasks}">
			<tr:table var="entry" width="100%" immediate="true"
				value="#{sys_design_navigation.currentNode.preTasks}"
				rows="15" rowBandingInterval="1" >
				<tr:column width="23px">				
						<tr:selectBooleanRadio group="selectOne" 
							value="#{entry.selected}" />
				</tr:column>
				<tr:column headerText="Continue on Failure" width="100px">
						<tr:selectBooleanCheckbox
							value="#{entry.task.continueOnFailure}" />
				</tr:column>
				<tr:column headerText="Extension" separateRows="true">
					<tr:selectOneChoice onchange="submit()"
						value="#{entry.extensionName}" required="true">
						<f:selectItems value="#{sys_design_navigation.currentNode.taskExtensionChoices}" />
					</tr:selectOneChoice>	
					<tr:table var="parameter" value="#{entry.params}" width="100%"
						rendered="#{! empty entry.params}">
						<tr:column>
							<tr:outputText value="#{parameter.name}" />
						</tr:column>
						<tr:column separateRows="true">
							<tr:inputText value="#{parameter.value}" columns="80" 
							maximumLength="2048"/>
							<tr:outputText value="#{parameter.description}" />
						</tr:column>
					</tr:table>		
				</tr:column>
			</tr:table>
		</tr:panelHeader>
		<tr:panelHeader styleClass="rxPanelHeader" text="Post Tasks" inlineStyle="margin-top: 10px" 
			rendered="#{sys_design_navigation.currentNode.hasPostTasks}">
			<tr:table var="entry" width="100%" immediate="true"
				value="#{sys_design_navigation.currentNode.postTasks}"
				rows="15" rowBandingInterval="1" >
				<tr:column width="23px">				
						<tr:selectBooleanRadio group="selectOne" 
							value="#{entry.selected}" />
				</tr:column>
				<tr:column headerText="Continue on Failure" width="100px">
						<tr:selectBooleanCheckbox
							value="#{entry.task.continueOnFailure}" />
				</tr:column>
				<tr:column headerText="Extension" separateRows="true">
					<tr:selectOneChoice onchange="submit()"
						value="#{entry.extensionName}"  required="true">
						<f:selectItems value="#{sys_design_navigation.currentNode.taskExtensionChoices}" />
					</tr:selectOneChoice>	
					<tr:table var="parameter" value="#{entry.params}" width="100%"
						rendered="#{! empty entry.params}">
						<tr:column>
							<tr:outputText value="#{parameter.name}" />
						</tr:column>
						<tr:column separateRows="true">
							<tr:inputText value="#{parameter.value}" columns="80" 
							maximumLength="2048"/>
							<tr:outputText value="#{parameter.description}" />
						</tr:column>
					</tr:table>		
				</tr:column>
			</tr:table>
		</tr:panelHeader>
  </jsp:body>
</layout:publishing>
