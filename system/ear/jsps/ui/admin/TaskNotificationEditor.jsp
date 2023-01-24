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

