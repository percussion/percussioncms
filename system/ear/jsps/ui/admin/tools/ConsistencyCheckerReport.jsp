<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>



<c:set var="page_title" scope="request" value="Check Repository Consistency"/>

<layout:admin>
   <jsp:body>
      <rxnav:adminbreadcrumbs/>
      <tr:panelHeader text="Consistency Checker Problem Report">
      </tr:panelHeader>

      <h:panelGroup rendered="#{sys_consistencychecker.hasProblems}">
         <h:dataTable var="e" style="width: 100%" value="#{sys_consistencychecker.problemsFound}">
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Title (contentid)"/>
               </f:facet>
               <h:outputText value="#{e.title} (#{e.contentid})"/>
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Status"/>
               </f:facet>
               <h:commandButton value="Fix" rendered="#{e.canfix}"
                   action="#{e.fix}"/>
               <h:outputText value="Fixed" rendered="#{e.fixed}"/>
               <h:outputText style="color: red" value="Broken" rendered="#{!e.fixable}"/>
            </h:column>
            <h:column>
                <f:facet name="header">
                   <h:outputText value="Problems found"/>
                </f:facet>
                <h:dataTable var="p" value="#{e.problems}" rendered="#{e.canfix || !e.fixable}">
                   <h:column>
                       <h:outputText value="#{p.table}"/>
                   </h:column>
                   <h:column>
                      <h:outputText value="#{p.missingRevisions}"/>
                   </h:column>
                </h:dataTable>
             </h:column>
          </h:dataTable>
          <h:commandButton value="View Report"
             action="#{sys_consistencychecker.finish}"/>
             <f:verbatim>&nbsp;&nbsp;</f:verbatim>
          <h:commandButton value="Back" action="back"/>
       </h:panelGroup>

       <h:panelGroup rendered="#{!sys_consistencychecker.hasProblems}">
          <f:verbatim><br/></f:verbatim>
          <h:outputText style="display: block; font-weight: bold" value='No problems found!'/>
          <f:verbatim><br/></f:verbatim>
          <h:commandButton value="Back" action="back"/>
       </h:panelGroup>
   </jsp:body>
</layout:admin>

