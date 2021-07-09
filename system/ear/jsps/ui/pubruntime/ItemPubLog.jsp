<%@ include file="/ui/pubruntime/PubRuntimeAuthentication.jsp" %>

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

<c:set var="page_title" scope="request" value="Rhythmyx - Item Publication Log" />
<layout:pubruntime>
   <jsp:body>   
      <rxcomp:menubar>
         <rxcomp:menuitem value="Previous" action="#{sys_runtime_navigation.detailItem.previous}"/>
         <rxcomp:menuitem value="Next" action="#{sys_runtime_navigation.detailItem.next}"/>
         <rxcomp:menuitem value="Done" action="pub-runtime-job-log"/>
         <rxcomp:menuitem value="Help"
           onclick="openHelpWindow('#{sys_runtime_navigation.itemPubLogHelpFile}')"/>
         
      </rxcomp:menubar>
      <rxnav:runtimebreadcrumbs/>
      <tr:panelFormLayout>
         <tr:inputText label="Reference Id:" value="#{sys_runtime_navigation.detailItem.itemStatus.referenceId}" readOnly="true"/>
         <tr:inputText label="Content id (revision):"
           value="#{sys_runtime_navigation.detailItem.itemStatus.contentId}(#{sys_runtime_navigation.detailItem.itemStatus.revisionId})"
           readOnly="true"/>
         <tr:inputText label="Template:" value="#{sys_runtime_navigation.detailItem.properties.template}" readOnly="true"/>
         <tr:inputText label="Start Time (elapsed):"
           value="#{sys_runtime_navigation.detailItem.properties.date} (#{sys_runtime_navigation.detailItem.properties.elapsed})"
           readOnly="true"/>
         <tr:inputText label="Site Folder:" value="#{sys_runtime_navigation.detailItem.properties.siteFolder}" readOnly="true"/>
         <tr:inputText label="Operation:" value="#{sys_runtime_navigation.detailItem.properties.operation}" readOnly="true"/>
         <tr:inputText label="Status:" value="#{sys_runtime_navigation.detailItem.properties.status}" readOnly="true"/>
         <tr:inputText label="Location:" value="#{sys_runtime_navigation.detailItem.itemStatus.location}" readOnly="true"/>
         <tr:inputText label="Delivery Type:" value="#{sys_runtime_navigation.detailItem.itemStatus.deliveryType}" readOnly="true"/>
         <tr:inputText rows="4" columns="80" readOnly="true" label="Assembly URL:"
           value="#{sys_runtime_navigation.detailItem.itemStatus.assemblyUrl}" />
      </tr:panelFormLayout>
      <tr:separator rendered="#{sys_runtime_navigation.detailItem.hasMessages}"/>
      <tr:table var="message" rowBandingInterval="1" width="100%"
        value="#{sys_runtime_navigation.detailItem.messages}" rendered="#{sys_runtime_navigation.detailItem.hasMessages}">
        <tr:column headerText="Messages">
          <tr:inputText value="#{message}" readOnly="true"/>
        </tr:column>
      </tr:table>
   </jsp:body>
</layout:pubruntime>
