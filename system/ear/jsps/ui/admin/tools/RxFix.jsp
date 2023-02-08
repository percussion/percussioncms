<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>



<c:set var="page_title" scope="request" value="RxFix Tool"/>

<layout:admin>
   <jsp:body>
      <rxcomp:menubar> 
			<rxcomp:menuitem value="Help" 
		   	onclick="openHelpWindow('#{sys_rxfix.helpFile}', true)"/>
    	</rxcomp:menubar>
   
      <rxnav:adminbreadcrumbs/>
      <tr:panelHeader text="RxFix - Choose Actions">
         <f:verbatim>
            <p>Choose which of the following fixes to preview. Once the preview
            has run, you will be able to confirm that you want to run the fix.</p> 
         </f:verbatim>

         <h:dataTable var="e" value="#{sys_rxfix.entries}">
            <h:column>
               <h:selectBooleanCheckbox value="#{e.dofix}"></h:selectBooleanCheckbox>
            </h:column>
            <h:column>
               <h:outputText value="#{e.fixname}"/>
            </h:column>
         </h:dataTable>

         <f:verbatim><br/></f:verbatim>
         <f:verbatim>
            <p><i>Please note:</i> The preview may take a few minutes to run,
            please be patient.</p>
         </f:verbatim>
         <h:commandButton value="Preview" action="#{sys_rxfix.preview}" />
      </tr:panelHeader>
   </jsp:body>
</layout:admin>
