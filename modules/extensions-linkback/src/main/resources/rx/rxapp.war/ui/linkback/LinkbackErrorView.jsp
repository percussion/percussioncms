<%@page %>


<jsp:useBean id="message" class="java.lang.String" scope="request"/>
<html>
<head>
<title>Linkback Error</title>
<style type="text/css">
span.error {
color:red;
}
a.bmk {
border:1px outset #DDDDDD;
padding:1px;
vertical-align:1px;
}
a.bmk {
background:#D8DFFF none repeat scroll 0%;
color:darkgreen;
font-family:sans-serif;
font-size:80%;
text-decoration:none;
}
</style>
</head>
<body>
<p>
This page does not support Linkback.  Please contact your administrator for assistance.
</p>
<% if (message != null && message.length() > 0) { %>
<span class="error">Error Details: <%=message%></span>
<% } %>
</body>
</html>
