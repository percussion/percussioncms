<%@ include file="/ui/publishing/PubDesignAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
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

<%
   /* ph: It seems the onkeypress attribute in the Filter control below should 
    not be needed, but I can't figure out how to make trinidad submit on Enter.  */
%>

<c:set var="page_title" scope="request" value="Rhythmyx - Site List View" />

<layout:publishing>
   <jsp:body>
	<rxcomp:menubar>
		<rxcomp:menu label="Action">
			<rxcomp:menuitem value="Create Site"
               action="#{sys_design_navigation.collectionNode.createSite}" />
			<rxcomp:menuitem value="Edit Selected Site"
               action="#{sys_design_navigation.collectionNode.edit}" />
		   <rxcomp:menuitem value="Copy Selected Site"
               action="#{sys_design_navigation.collectionNode.copy}" />  
			<rxcomp:menuitem value="Delete Selected Site"
               action="#{sys_design_navigation.collectionNode.delete}" />
		</rxcomp:menu>
		<rxcomp:menuitem value="Help" 
		   		onclick="openHelpWindow('#{sys_design_navigation.collectionNode.helpFile}')"/>
	</rxcomp:menubar>
	<rxnav:listbreadcrumbs />

   <tr:panelHorizontalLayout valign="center" halign="start"
         inlineStyle="padding: 5px 0px 5px 0px;">
      <tr:inputText label="Filter" value="#{sys_design_navigation.filter}"
            onkeypress="if ((window.event && window.event.keyCode == 13) || (event && event.which == 13)) this.form.submit(); else return true;" />
      <tr:commandButton text="Apply" />
      <tr:commandButton text="Clear"
            action="#{sys_design_navigation.clearFilter}" />
      <tr:message messageType="info"
            message="('#{empty sys_design_navigation.filter ? '' : sys_design_navigation.filter}' filter applied)" />
   </tr:panelHorizontalLayout>
	<tr:table var="row" value="#{sys_design_navigation.list}" 
			rows="#{sys_design_navigation.collectionNode.pageRows}"
			binding="#{sys_design_navigation.collectionNode.table}"
         width="100%" rowBandingInterval="1">
		<tr:column  width="23px">
			<tr:selectBooleanRadio group="selectedrow" value="#{row.selectedRow}" />
		</tr:column>			
		<tr:column sortable="true" sortProperty="nameWithId" headerText="Name (Id)">
			<tr:commandLink text="#{row.nameWithId}" action="#{row.perform}" />
		</tr:column>
		<tr:column headerText="Rhythmyx Path">
			<tr:outputText value="#{row.properties.rootpath}" />
		</tr:column>        	
		<tr:column headerText="Published URL">
			<tr:outputText value="#{row.properties.baseurl}" />
		</tr:column>
		<tr:column headerText="Published Path">
			<tr:outputText value="#{row.properties.pubpath}" />
		</tr:column>
	</tr:table>
 </jsp:body>
</layout:publishing>
