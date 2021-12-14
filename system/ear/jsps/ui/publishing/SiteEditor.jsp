<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Rhythmyx - Site Editor"/>
<layout:publishing>
  <jsp:body>
	  	<rxcomp:menubar>
			<rxcomp:menu label="Action">
				<rxcomp:menuitem value="Add Context Variable..." 
               action="#{sys_design_navigation.currentNode.addContextVariable}" />	
				<rxcomp:menuitem value="Delete Selected Context Variable"
				   action="#{sys_design_navigation.currentNode.removeContextVariable}" />			
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
				value="#{sys_design_navigation.currentNode.name}"
				validator="#{sys_design_unique_name_validator.validate}"
				maximumLength="100"
				required="true">
				<f:validator validatorId="com.percussion.jsf.name"/>
			</tr:inputText>
			<tr:inputText label="Description" rows="4" columns="78"
				maximumLength="255"
				value="#{sys_design_navigation.currentNode.description}" />
			<tr:inputText label="Rhythmyx Path" columns="80"
            converter="sys_normalize_path"
            validator="#{sys_path_validator.validate}"
				maximumLength="2100"
				value="#{sys_design_navigation.currentNode.folderRootPath}" />
			<tr:commandButton text="..."
				action="#{sys_design_navigation.currentNode.browsePath}" />
			<tr:selectOneChoice label="Global Template" 
				value="#{sys_design_navigation.currentNode.globalTemplate}">
				<f:selectItems 
					value="#{sys_design_navigation.currentNode.globalTemplates}" />
			</tr:selectOneChoice>
			<tr:inputText label="Published URL" columns="80"
				maximumLength="2100"
				value="#{sys_design_navigation.currentNode.baseUrl}" />
			<tr:inputText label="Published Path" columns="80"
				maximumLength="128"
				value="#{sys_design_navigation.currentNode.rootPath}" />
		</tr:panelFormLayout>
		<tr:separator/>
		<tr:showDetail disclosed="false" id="disclose_details" immediate="true"
			undisclosedText="Show Site Details"
			disclosedText="Hide Site Details" >
			<tr:panelFormLayout styleClass="pub-edit-detailbox">
				<tr:inputText label="FTP Server IP Address" 
					value="#{sys_design_navigation.currentNode.ftpServer}" />
				<tr:inputText label="FTP Server Port" 
					value="#{sys_design_navigation.currentNode.ftpPort}">
					<f:validateLongRange minimum="1" maximum="65535" />
				</tr:inputText>
				<tr:inputText label="User Name" 
					value="#{sys_design_navigation.currentNode.ftpUser}" />
				<tr:inputText label="Password" secret="true"
					value="#{sys_design_navigation.currentNode.ftpPassword}" />
				<tr:selectOneChoice label="Private Key" 
					value="#{sys_design_navigation.currentNode.privateKey}">
					<f:selectItems 
						value="#{sys_design_navigation.currentNode.privateKeys}" />
				</tr:selectOneChoice>
				<tr:inputText label="Unpublish Flags"
					maximumLength="50" required="true"
					value="#{sys_design_navigation.currentNode.unpublishedFlags}" />
				<tr:inputText label="Nav Theme" 
					value="#{sys_design_navigation.currentNode.navTheme}" />
				<tr:inputText label="Allowed Namespaces" columns="80"
					value="#{sys_design_navigation.currentNode.allowedNamespaces}" />
			</tr:panelFormLayout>
		</tr:showDetail>
		<tr:separator/>
		<tr:panelHeader styleClass="rxPanelHeader" text="Context Variables">
			<tr:table var="prop" immediate="true" 
				value="#{sys_design_navigation.currentNode.siteProperties}"
				rows="25" width="100%" rowBandingInterval="1" >
            <tr:column width="30px">
               <tr:selectBooleanRadio group="propertylist" 
                     value="#{prop.selected}" />
            </tr:column>				
				<tr:column sortable="true" sortProperty="name" headerText="Name (Id)">
					<tr:outputText value="#{prop.nameWithId}"/>
				</tr:column>
				<tr:column sortable="true" sortProperty="contextName" headerText="Context (Id)">
					<tr:outputText value="#{prop.contextNameWithId}"/>
				</tr:column>
				<tr:column sortable="true" sortProperty="value" headerText="Value">
					<tr:inputText value="#{prop.value}" required="true"
					columns="50" maximumLength="255"/>
				</tr:column>
			</tr:table>
		</tr:panelHeader>
  </jsp:body>
</layout:publishing>
