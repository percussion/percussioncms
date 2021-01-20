<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8"
   contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>

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

<c:set var="page_title" scope="request" value="Rhythmyx - Delivery Type Editor" />
<layout:publishing>
   <jsp:body>
	<rxcomp:menubar>
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
            maximumLength="50"
            value="#{sys_design_navigation.currentNode.name}"
            validator="#{sys_design_unique_name_validator.validate}"
			   required="true">
            <f:validator validatorId="com.percussion.jsf.name"/>
         </tr:inputText>
         <tr:inputText label="Description" rows="4" columns="80"
            maximumLength="255"
            value="#{sys_design_navigation.currentNode.description}" />
         <tr:inputText label="Spring Bean Name" columns="80"
            maximumLength="255" required="true"
            value="#{sys_design_navigation.currentNode.beanName}" />
         <tr:selectBooleanCheckbox label="Assemble Item For Unpublish"
            value="#{sys_design_navigation.currentNode.unpublishingRequiresAssembly}">
         </tr:selectBooleanCheckbox>
      </tr:panelFormLayout>
 </jsp:body>
</layout:publishing>
