<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8"
   contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>



<c:set var="page_title" scope="request" value="Rhythmyx - Browse Site Root Path" />

<layout:publishing>
   <jsp:body>
	<rxcomp:menubar>
	   <rxcomp:menuitem value="Cancel"
	           immediate="true"
              action="#{sys_design_navigation.currentNode.scheme.perform}" />  
      <rxcomp:menuitem value="Help" 
			onclick="openHelpWindow('#{sys_design_navigation.currentNode.scheme.jexlTestPanel.itemBrowser.helpFile}')"/>
		
	</rxcomp:menubar>
	<rxnav:listbreadcrumbs />

   <tr:panelHorizontalLayout valign="center" halign="start"
         inlineStyle="padding: 5px 0px 5px 0px;">
      <tr:inputText label="Site Root Path"
         columns="80" 
         required="true"
         converter="sys_normalize_path"
         validator="#{sys_path_validator.validate}"
         value="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.itemBrowser.path}" />
      <tr:spacer width="10" />
      <tr:commandButton text="Go" 
         action="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.itemBrowser.gotoFolder}" />
      <tr:spacer width="10" />
      <tr:commandButton icon="../../sys_resources/images/arrowup.gif"
         immediate="true"
         action="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.itemBrowser.gotoParent}" />
   </tr:panelHorizontalLayout>
	<tr:table var="row" value="#{sys_design_navigation.currentNode.scheme.jexlTestPanel.itemBrowser.children}" rows="25"
         width="100%" rowBandingInterval="1">
      <tr:column sortable="true" sortProperty="name" headerText="Name">
		   <tr:panelHorizontalLayout valign="center" halign="start" >		   
		   	 <tr:commandLink immediate="true" action="#{row.perform}">
				      <tr:image source="#{row.folder ? '../../sys_resources/images/folder.gif' : '../../sys_resources/images/item.gif'}" />
             </tr:commandLink>
		       <tr:spacer width="3" />
			    <tr:commandLink text="#{row.name}" action="#{row.perform}" immediate="true" />
			</tr:panelHorizontalLayout>
		</tr:column>
	</tr:table>
	<tr:message messageType="info"
       message="#{empty sys_design_navigation.currentNode.scheme.jexlTestPanel.itemBrowser.children ? 'This folder is empty.' : ''}" />
	
 </jsp:body>
</layout:publishing>
