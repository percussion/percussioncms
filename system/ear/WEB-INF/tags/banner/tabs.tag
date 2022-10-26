<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:rx="http://rhythmyx.percussion.com/components"
	version="1.2">
	<jsp:scriptlet>
		request.setAttribute("servlet_path", request.getServletPath());
	</jsp:scriptlet>
	<jsp:useBean id="sys_tabs" 
		class="com.percussion.rx.ui.jsf.beans.PSTopNavigation"
		scope="page"/>
	<jsp:setProperty name="sys_tabs" property="path" 
		value="${servlet_path}"/>
	<rx:tabContainer value="${sys_tabs}" var="tab">
		<c:choose>
			<c:when test='${tab.enabled}'>
				<a href="${tab.url}" 
					class="${tab.style}" 
					target="_parent">${tab.label}</a>
			</c:when>
			<c:otherwise>
				<a class="${tab.style}">${tab.label}</a>
			</c:otherwise>
		</c:choose>
	</rx:tabContainer>
</jsp:root>
