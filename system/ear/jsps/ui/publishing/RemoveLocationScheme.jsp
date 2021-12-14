<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Rhythmyx - Delete Location Scheme" />
<layout:publishing>
   <jsp:body>
   <rxcomp:menubar>
      <rxcomp:menuitem value="Delete" 
         action="#{sys_design_navigation.currentNode.deleteSchemeCompletion}" />
      <rxcomp:menuitem value="Cancel" 
         action="#{sys_design_navigation.currentNode.cancelSchemeAction}" />
   </rxcomp:menubar>
   <rxnav:listbreadcrumbs />
      <tr:panelBox inlineStyle="width: 50%">
         <tr:outputText  
               value="The Location Scheme '#{sys_design_navigation.currentNode.selectedScheme.nameWithId}' 
 will be permanently deleted from Context '#{sys_design_navigation.currentNode.title}'.
 Click on 'Delete' to do this or cancel to return to the previous page." />
      </tr:panelBox>
  </jsp:body>
</layout:publishing>
