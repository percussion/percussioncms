
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:tr="http://myfaces.apache.org/trinidad"
	version="1.2">
	<tr:breadCrumbs value="#{sys_runtime_navigation.tree}" var="node">
		<f:facet name="nodeStamp">
		  <h:panelGroup>
         <tr:commandNavigationItem 
            action="#{node.perform}" styleClass="pub-breadcrumb-label"
            rendered="#{node.enabled}" text="#{node.label}"
            shortDesc="#{node.title}" selected="#{node.selected}" />
         <tr:outputText 
            styleClass="pub-breadcrumb-label"
            rendered="#{!node.enabled}" value="#{node.label}" />
         </h:panelGroup>
		</f:facet>
	</tr:breadCrumbs>
</jsp:root>
