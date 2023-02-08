<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>



<c:set var="page_title" scope="request" value="Command Console" />
<c:set var="page_script" scope="request">
   <script>
         function setCaretToEnd() {
           input = document.getElementById("dispResults");
           input.scrollTop = input.scrollHeight;
           
           // leave focus in command field  
           cmd = document.getElementById("command");
           cmd.focus();
         }
   </script>
</c:set>
<c:set var="page_onload" scope="request" value="setCaretToEnd()" />

<layout:admin>
   <jsp:body>
      <rxcomp:menubar> 
			<rxcomp:menuitem value="Help" 
		   	onclick="openHelpWindow('#{sys_admin_navigation.startingNode.helpFile}')"/>
    	</rxcomp:menubar>
      <rxnav:adminbreadcrumbs/>
       <tr:panelHeader styleClass="rxPanelHeader" text="Rhythmyx Command Console">
         <tr:panelFormLayout inlineStyle="background-color:#bec5e7;">
            <tr:inputText id="command" labelAndAccessKey="&Command:"
                columns="40"
                onkeypress="if ((window.event && window.event.keyCode === 13) || (event && event.which === 13)) this.form.submit(); else return true;"
                value="#{sys_admin_navigation.startingNode.command}"/>
            <tr:commandButton id="submitButton" action="success" text="Go"/>
            <tr:inputText id="dispResults" labelAndAccessKey="Command &Output:"
                     rows="20" columns="60"
                     readOnly="true"
                     value="#{sys_admin_navigation.startingNode.result}"/>
            <tr:selectBooleanCheckbox id="append"
                     label="Append Command Output" accessKey="A"
                     value="#{sys_admin_navigation.startingNode.append}"/>
         </tr:panelFormLayout>
      </tr:panelHeader>
   </jsp:body>
</layout:admin>
