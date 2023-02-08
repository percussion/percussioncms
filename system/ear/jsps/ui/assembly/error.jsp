<%@page contentType="text/html; charset=utf-8"
		import="org.apache.commons.lang.StringEscapeUtils"
		import="com.percussion.server.PSServer"
		import="java.util.Enumeration" %>


<%
	String id = (String) request.getAttribute("id");
	String error = (String) request.getAttribute("error");
%>
<html>
<head>
	<title>Assembly error for item: <%= id %></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="/sys_resources/css/rxcx.css" type="text/css" media="screen" />
	<link href="/sys_resources/css/templates.css" rel="stylesheet" type="text/css">
	<link href="/rx_resources/css/templates.css" rel="stylesheet" type="text/css">
	<link href="../rx_resources/css/en-us/templates.css" rel="stylesheet" type="text/css">
</head>
<body>
<div style="background-color: white; margin: 10px; padding-top: 0px; padding: 10px">
	<p><img src="../sys_resources/images/banner_bkgd.jpg"></p>
	<h2>Problem during the assembly of item</h2>
	<% String isEnabled = PSServer.getServerProps().getProperty("enableDebugTools");
		if(isEnabled == null)
			isEnabled="false";
		if(isEnabled.equalsIgnoreCase("true")){ %>
	<p><a href="/test/velocitylog.jsp">Click here to view velocity log</a></p>
	<% } %>
	<h3>Parameters passed</h3>
	<table border="0" cellspacing="0" cellpadding="1">
		<tr><th>Name</th><th>Value</th></tr>
		<%
			Enumeration e = request.getParameterNames();
			while(e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				String value = request.getParameter(name);
				value = StringEscapeUtils.escapeHtml(value);
		%><tr><td><%=name%></td><td><%=value%></td></tr><%
		}
	%>
	</table>
	<h3>Error reported</h3>
	<p><%= error %></p>
	<i>Please note: More information may be available on the console</i>
</div>
</body>
</html>
