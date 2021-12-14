<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

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

<c:set var="page_title" scope="request" value="Variant Convertion Selection Page"/>

<layout:admin>
   <jsp:body>
   
      <rxcomp:menubar> 
			<rxcomp:menuitem value="Help" 
		   	onclick="openHelpWindow('#{sys_variantmigrationbean.helpFile}', true)"/>
    	</rxcomp:menubar>
      <rxnav:adminbreadcrumbs/>
      <tr:panelHeader text="Variant Conversion Page - Choose Variants">
         <f:verbatim>
            <p>Choose from which variant sets to create templates.
            You can accept or change the names of the created templates
            here as well.</p>
         </f:verbatim>

         <t:dataTable var="v" value="#{sys_variantmigrationbean.variants}"
               rowClasses="datacell1,datacell2" border="1">
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Selected" />
               </f:facet>
               <h:selectBooleanCheckbox value="#{v.selected}"></h:selectBooleanCheckbox>
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Variant Name" />
               </f:facet>
               <h:outputText value="#{v.name}"/>
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Template Name" />
               </f:facet>
               <h:inputText value="#{v.newtemplatename}"/>
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Resource" />
               </f:facet>
               <h:outputText value="#{v.resource}"/>
            </h:column>             
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Content Type IDs" />
               </f:facet>
               <h:column>                    
                  <h:outputText value="#{v.contenttypes}" />
               </h:column>
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Status"/>
               </f:facet>
               <h:outputText value="Already Processed" rendered="#{v.processed}"/>
               <h:outputText value="" rendered="#{!v.processed}"/>                  
            </h:column>
         </t:dataTable>

         <f:verbatim><br/></f:verbatim>
         <h:commandButton action="#{sys_variantmigrationbean.process}" value="Convert"/>
      </tr:panelHeader>
   </jsp:body>
</layout:admin>
