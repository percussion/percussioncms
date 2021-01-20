<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>

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

<c:set var="page_title" scope="request" value="Consistency Checker Review"/>

<layout:admin>
   <jsp:body>
      <rxnav:adminbreadcrumbs/>
      <tr:panelHeader text="Consistency Checker Review">
         <f:verbatim>
            <p>Warning: Fixed revisions contain cloned data from prior revisions.
            Any original data that is being replaced is missing permanently.
            Therefore it is important that this report be printed and
            that a person examine the current revision and corrects any missing
            or incorrect data, including any simple or multi-valued children.</p> 
         </f:verbatim>

         <h:dataTable var="e" style="width: 100%" value="#{sys_consistencychecker.problemsFound}">
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Title (contentid)"/>
               </f:facet>
               <h:outputText value="#{e.title} (#{e.contentid})"/>
            </h:column>
            <h:column>
               <f:facet name="header"> 
                  <h:outputText value="Final Status"/>
               </f:facet>
               <h:outputText value="Fixed" rendered="#{e.fixed}"/>
               <h:outputText value="Not fixed (but fixable)" rendered="#{!e.fixed && e.canfix}"/>
               <h:outputText style="color: red" value="Broken" rendered="#{!e.fixable}"/>
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Problems"/>
               </f:facet>
               <h:dataTable var="p" value="#{e.problems}" rendered="#{e.fixed}">
                  <h:column>
                     <h:outputText value="#{p.table}"/>
                  </h:column>
                  <h:column>
                     <h:outputText value="#{p.missingRevisions}"/>
                  </h:column>
               </h:dataTable>
            </h:column>
         </h:dataTable>
         <f:verbatim><br/></f:verbatim>
         <h:commandButton value="Restart" action="restart"/>
      </tr:panelHeader>
   </jsp:body>
</layout:admin>