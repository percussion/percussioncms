<%@page contentType="text/html; charset=utf-8" import="org.apache.commons.lang.StringUtils"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>	
<%@ taglib tagdir="/WEB-INF/tags/banner" prefix="rxb"%>


<%
	String pname = request.getParameter("sys_pagename");

	if (StringUtils.isNotBlank(pname))
	{
	   session.setAttribute("sys_pagename", pname);
	}
%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>${fn:escapeXml(pagetitle)}</title>
	<%@include file="header.jsp"%>
</head>
<body>
	<rxb:tabs/>
</body>
</html>
