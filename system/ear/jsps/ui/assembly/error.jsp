<%@page contentType="text/html; charset=utf-8" import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.util.Enumeration" %>
<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
	String id = (String) request.getAttribute("id");
	String error = (String) request.getAttribute("error");
%>
<html>
<head>
	<title>Assembly error for item: <%= id %></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="../sys_resources/css/rxcx.css" type="text/css" media="screen" />
	<link href="../sys_resources/css/templates.css" rel="stylesheet" type="text/css">
	<link href="../rx_resources/css/templates.css" rel="stylesheet" type="text/css">
	<link href="../rx_resources/css/en-us/templates.css" rel="stylesheet" type="text/css">
</head>
<body>
<div style="background-color: white; margin: 10px; padding-top: 0px; padding: 10px">
	<p><img src="../sys_resources/images/banner_bkgd.jpg"></p>
	<h2>Problem during the assembly of item</h2>
	<p><a href="/test/velocitylog.jsp">Click here to view velocity log</a></p>
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