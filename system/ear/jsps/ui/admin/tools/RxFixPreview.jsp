<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>




<c:set var="page_title" scope="request" value="RxFix Results"/>

<layout:admin>
   <jsp:body>
      <rxcomp:menubar> 
			<rxcomp:menuitem value="Help" 
		   	onclick="openHelpWindow('#{sys_rxfix.helpFile}', true)"/>
    	</rxcomp:menubar>
   
      <rxnav:adminbreadcrumbs/>
      <tr:panelHeader text="RxFix - Results">
         <f:verbatim>
            <p>The following are the results of the run.</p>
         </f:verbatim>

         <h:dataTable var="e" value="#{sys_rxfix.runentries}" >
            <h:column>
               <h:outputText value="#{e.fixname}" styleClass="category" />
               <h:dataTable var="r" value="#{e.results}" rowClasses="rowalign" >
                  <h:column>
                     <h:panelGrid>
                        <h:panelGroup>
                           <h:outputText value="#{r.status}" styleClass="status" />
                           <h:outputText value=" "/>
                           <h:outputText value="#{r.id}" styleClass="id"/>
                        </h:panelGroup>
                        <h:outputText value="#{r.message}" styleClass="message"/>
                     </h:panelGrid>
                  </h:column>
               </h:dataTable>
            </h:column>
         </h:dataTable>

         <f:verbatim><br/></f:verbatim>
         <h:commandButton value="Continue" action="#{sys_rxfix.next}" />
      </tr:panelHeader>
   </jsp:body>
</layout:admin>
