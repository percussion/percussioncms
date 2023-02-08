<%@page isErrorPage="true" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>


<html>
	<head>
		<title>Publishing error</title>
		<%@include file="../header.jsp"%>
	</head>
	<body>	
		<div style="background-color: white; margin: 10px; padding-top: 0px; padding: 10px">
			<p><img src="../../sys_resources/images/banner_bkgd.jpg"></p>
			<h2>Problem during the publishing of one or more items</h2>
			<TABLE>
			<tr><td valign="top">Exception Class:</td><td><%= exception.getClass() %></td></tr>
			<tr><td valign="top">Message:</td><td><%= exception.getMessage() %></td></tr>
			</TABLE>
			<i>Please note: More information may be available on the console</i>
		</div>
	</body>
</html>
