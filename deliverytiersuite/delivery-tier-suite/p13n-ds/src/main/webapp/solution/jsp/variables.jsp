<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
	String temp = request.getParameter("FromRx");
	boolean insideRx = (temp == null || !temp.equalsIgnoreCase("yes"))? false : true;
	String aaMenuVisibility = insideRx ? "visible":"hidden";
	String pageUrl = request.getParameter("url");
	pageUrl = pageUrl == null ? "/" : pageUrl;
	request.setAttribute("insideRx", new Boolean(insideRx));
	request.setAttribute("previewPageUrl", pageUrl);
	request.setAttribute("basePath", request.getContextPath());
%>
<spring:theme code="resources" var="themeResources"/>
<spring:theme code="scripts" var="themeScripts"/>
<spring:theme code="profileEdit" var="themeProfileEdit"/>
<spring:theme code="profileMenu" var="themeProfileMenu"/>
<spring:theme code="profileMain" var="themeProfileMain"/>
<c:set var="scripts" value="${basePath}${themeScripts}" />
<c:set var="resources" value="${basePath}${themeResources}" />
<c:set var="profileEditorUrl" value="${basePath}${themeProfileEdit}" />
<c:set var="profileMainUrl" value="${basePath}${themeProfileMain}" />
<c:set var="profileMenuUrl" value="${basePath}${themeProfileMenu}" />