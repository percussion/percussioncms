<%@ page import="com.percussion.server.PSServer" %>
<%@ page import="java.net.URL" %>
<%@ page session="false" %>


<!DOCTYPE html>
<html lang="en">
	<head><title>Welcome to Percussion</title>
<%
	if(PSServer.isRequestBehindProxy(request)) {
		String redirectUrl = "";
		String proxyUrl = PSServer.getProxyURL(request, true);
		if(proxyUrl == ""){
			redirectUrl = request.getContextPath()+"/cm/app/";
		}else{
			redirectUrl = proxyUrl + "/cm/app/";
		}
		response.sendRedirect(response.encodeUrl(redirectUrl));
	}else {
		response.sendRedirect(response.encodeUrl(request.getContextPath()
				+ "/cm/app"));
	}
%>
	</head>
	<body>
		<H1>Welcome to Percussion - redirecting...</H1>
	</body>
</html>
