<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>



<c:set var="page_title" scope="request" value="Check Repository Consistency"/>

<layout:admin>
   <jsp:body>
      <rxcomp:menubar> 
			<rxcomp:menuitem value="Help" 
		   	onclick="openHelpWindow('#{sys_consistencychecker.helpFile}', true)"/>
    	</rxcomp:menubar>   
      <rxnav:adminbreadcrumbs/>
      <tr:panelHeader text="Check Repository Consistency">
         <f:verbatim>
            <p>Choose the content types that should be checked in the database.</p> 
         </f:verbatim>
         <h:messages showDetail="true"/>

         <h:dataTable var="e" value="#{sys_consistencychecker.typeentries}">
            <h:column>
               <h:selectBooleanCheckbox value="#{e.selected}"></h:selectBooleanCheckbox>
            </h:column>
            <h:column>
               <h:outputText value="#{e.label}"/>
            </h:column>
         </h:dataTable>

         <f:verbatim>
            <p><i>Please note:</i> The check may take a few minutes to run,
            please be patient.</p>
         </f:verbatim>
         <h:commandButton value="Check" action="#{sys_consistencychecker.check}"/>
      </tr:panelHeader>
   </jsp:body>
</layout:admin>
