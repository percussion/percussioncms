<%@ page language="java"
   import="com.percussion.design.objectstore.PSAclEntry,com.percussion.server.IPSRequestContext"
   pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<c:set var="page_title" scope="request" value="Scheduled Task Editor"/>

<%
IPSRequestContext reqCtx = (IPSRequestContext) request
   .getAttribute("RX_REQUEST_CONTEXT");
PSServer.checkAccessLevel(reqCtx.getSecurityToken(),
   PSAclEntry.SACE_ADMINISTER_SERVER);
%>

<layout:admin>
   <jsp:body>
      <rxcomp:menubar>
         <rxcomp:menu label="Action">
            <rxcomp:menuitem value="Run Now" 
               action="#{sys_admin_navigation.currentNode.runNow}" />  
         </rxcomp:menu>
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
         </tr:inputText>
         
         <tr:selectOneChoice onchange="submit()" label="Extension"
            value="#{sys_admin_navigation.currentNode.extensionName}" 
            required="true">
            <f:selectItems value="#{sys_admin_navigation.currentNode.taskExtensionChoices}" />
         </tr:selectOneChoice>   
         <tr:table var="parameter" value="#{sys_admin_navigation.currentNode.params}" width="100%"
            rendered="#{! empty sys_admin_navigation.currentNode.params}">
            <tr:column>
               <tr:outputText value="#{parameter.name}" />
            </tr:column>
            <tr:column separateRows="true">
               <tr:inputText value="#{parameter.value}" columns="80" 
               maximumLength="2048"/>
               <tr:outputText value="#{parameter.description}"/>
            </tr:column>
         </tr:table>    
         
         <tr:inputText label="Cron Specification"
               value="#{sys_admin_navigation.currentNode.event.cronSpecification}"
               required="true"/>
         <tr:inputText label="Server"
               value="#{sys_admin_navigation.currentNode.event.server}"/>
         <tr:selectOneChoice label="Notify When" 
            value="#{sys_admin_navigation.currentNode.event.notifyWhen}"
            required="true">
            <f:selectItems
                 value="#{sys_admin_navigation.currentNode.notifyWhenChoices}"/>
         </tr:selectOneChoice>
         <tr:selectOneChoice label="Notify Role"
               value="#{sys_admin_navigation.currentNode.event.notify}">
            <f:selectItems
                 value="#{sys_admin_navigation.currentNode.notifyRowChoices}"/>
         </tr:selectOneChoice>
         <tr:inputText label="Email Addresses (',' separated)"
               rows="4" columns="80"
               shortDesc="Comma separated list of email addresses and subjects"
               value="#{sys_admin_navigation.currentNode.event.emailAddresses}"/>
         <tr:outputText value="#{sys_admin_navigation.currentNode.notifyWarning}" 
            rendered="#{! empty sys_admin_navigation.currentNode.notifyWarning}"
            styleClass="OraInlineErrorText"/>
            
         <tr:selectOneChoice label="Notification Template" 
            value="#{sys_admin_navigation.currentNode.notifyTemplate}">
            <f:selectItems
                 value="#{sys_admin_navigation.currentNode.notificationTemplateChoices}"/>
         </tr:selectOneChoice>
         <tr:outputText value="#{sys_admin_navigation.currentNode.notifyTemplateWarning}" 
            rendered="#{! empty sys_admin_navigation.currentNode.notifyTemplateWarning}"
            styleClass="OraInlineErrorText"/>
         
      </tr:panelFormLayout>
   </jsp:body>
</layout:admin>

