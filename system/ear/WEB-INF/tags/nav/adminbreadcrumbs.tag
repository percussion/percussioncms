
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:tr="http://myfaces.apache.org/trinidad"
	version="1.2">
	<tr:breadCrumbs value="#{sys_admin_navigation.tree}" var="node">
		<f:facet name="nodeStamp">
			<tr:commandNavigationItem  styleClass="pub-breadcrumb-label"
				action="#{node.perform}" disabled="#{!node.enabled}" 
				text="#{node.label}"	shortDesc="#{node.title}" 
				selected="#{node.selected}" />
		</f:facet>
	</tr:breadCrumbs>
</jsp:root>
