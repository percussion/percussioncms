<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>



<c:set var="page_title" scope="request" value="Variant Convertion Results Page"/>

<layout:admin>
   <jsp:body>
      <rxnav:adminbreadcrumbs/>
      <tr:panelHeader text="Variant Conversion Page - Results">
         <f:verbatim>
            <p>Results from converting selected variants to templates.</p>
         </f:verbatim>

         <t:dataTable var="v" rowClasses="datacell1,datacell2"
            value="#{sys_variantmigrationbean.processedvariants}" border="1">
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Name" />
               </f:facet>
               <h:outputText value="#{v.name}" />
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Resource" />
               </f:facet>
               <h:outputText value="#{v.resource}" />
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="New Template Name" />
               </f:facet>
               <h:outputText value="#{v.newtemplatename}" />
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText value="Problems (if any)" />
               </f:facet>
               <h:dataTable var="e" value="#{v.errors}">
                  <h:column>
                     <h:outputText value="#{e}" />
                  </h:column>
               </h:dataTable>
            </h:column>
         </t:dataTable>
         <h:commandButton action="admin-convert-variants" value="Back to Variants Selection"/>
      </tr:panelHeader>
   </jsp:body>
</layout:admin>
