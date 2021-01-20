<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8"
   contentType="text/html; charset=UTF-8"%>
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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Rhythmyx - Browse Site Root Path" />

<layout:publishing>
   <jsp:body>
	<rxcomp:menubar>
		<rxcomp:menuitem value="Done"
              action="#{sys_design_navigation.currentNode.rootBrowser.done}" />
	   <rxcomp:menuitem value="Cancel"
	           immediate="true"
              action="#{sys_design_navigation.currentNode.perform}" />  
		<rxcomp:menuitem value="Help" 
	  			onclick="openHelpWindow('#{sys_design_navigation.currentNode.rootBrowser.helpFile}')"/>
		
		
	</rxcomp:menubar>
	<rxnav:listbreadcrumbs />

   <tr:panelHorizontalLayout valign="center" halign="start"
         inlineStyle="padding: 5px 0px 5px 0px;">
      <tr:inputText label="Site Root Path"
         columns="80" 
         required="true"
         converter="sys_normalize_path"
         validator="#{sys_path_validator.validate}"
         value="#{sys_design_navigation.currentNode.rootBrowser.path}" />
      <tr:spacer width="10" />
      <tr:commandButton text="Go" 
         action="#{sys_design_navigation.currentNode.rootBrowser.gotoFolder}" />
      <tr:spacer width="10" />
      <tr:commandButton icon="../../sys_resources/images/arrowup.gif"
         immediate="true"
         action="#{sys_design_navigation.currentNode.rootBrowser.gotoParent}" />
   </tr:panelHorizontalLayout>
	<tr:table var="row" value="#{sys_design_navigation.currentNode.rootBrowser.children}" rows="25"
         width="100%" rowBandingInterval="1">
      <tr:column sortable="true" sortProperty="name" headerText="Name">
		   <tr:panelHorizontalLayout valign="center" halign="start" >
		    	 <tr:commandLink immediate="true" action="#{row.perform}">
				      <tr:image source="#{row.folder ? '../../sys_resources/images/folder.gif' : '../../sys_resources/images/item.gif'}" />
             </tr:commandLink>
		       <tr:spacer width="3" />
			    <tr:commandLink text="#{row.name}" action="#{row.perform}" immediate="true" />
			</tr:panelHorizontalLayout>
		</tr:column>
	</tr:table>
	<tr:message messageType="info"
       message="#{empty sys_design_navigation.currentNode.rootBrowser.children ? 'This folder is empty.' : ''}" />
	
 </jsp:body>
</layout:publishing>
