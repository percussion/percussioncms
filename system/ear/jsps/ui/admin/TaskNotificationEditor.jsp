<%@ page language="java"
   import="com.percussion.design.objectstore.PSAclEntry,com.percussion.server.IPSRequestContext"
   pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="com.percussion.server.PSServer" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
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

<c:set var="page_title" scope="request" value="Task Notification Editor"/>

<%
IPSRequestContext reqCtx = (IPSRequestContext) request
   .getAttribute("RX_REQUEST_CONTEXT");
PSServer.checkAccessLevel(reqCtx.getSecurityToken(),
   PSAclEntry.SACE_ADMINISTER_SERVER);
%>

<layout:admin>
   <jsp:body>
      <rxcomp:menubar>
         <rxcomp:menuitem value="Save"
               action="#{sys_admin_navigation.currentNode.save}" />
         <rxcomp:menuitem value="Cancel" immediate="true"
               action="#{sys_admin_navigation.currentNode.cancel}" />
	      <rxcomp:menuitem value="Help" 
			   	onclick="openHelpWindow('#{sys_admin_navigation.currentNode.helpFile}')"/>
      </rxcomp:menubar>
      <rxnav:adminbreadcrumbs/>

      <tr:panelFormLayout>
         <tr:inputText label="Name"
               value="#{sys_admin_navigation.currentNode.name}"
               validator="#{sys_admin_unique_name_validator.validate}"
               required="true">
            <f:validator validatorId="com.percussion.jsf.name"/>
            <f:validateLength minimum="1" maximum="255"/>
         </tr:inputText>
         <tr:inputText label="Subject" rows="5" columns="80"
                  value="#{sys_admin_navigation.currentNode.subject}"
                  required="true"/>
         <tr:inputText label="Template" rows="30" columns="80"
                  value="#{sys_admin_navigation.currentNode.template}"/>
      </tr:panelFormLayout>
   </jsp:body>
</layout:admin>

