
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:tr="http://myfaces.apache.org/trinidad"
	version="1.2">
	<tr:breadCrumbs value="#{sys_design_navigation.tree}" var="node">
		<f:facet name="nodeStamp">
			<tr:outputText value="#{node.label}" styleClass="pub-breadcrumb-label" />
		</f:facet>
	</tr:breadCrumbs>
</jsp:root>
