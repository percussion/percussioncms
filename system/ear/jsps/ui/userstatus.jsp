<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:rxb="urn:jsptagdir:/WEB-INF/tags/banner"
	version="1.2">
	<html>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
			<title><jsp:expression>request.getAttribute("pagetitle")</jsp:expression></title>
			<jsp:directive.include file="header.jsp" />
		</head>
		<body>
			<rxb:status/>
		</body>
	</html>
</jsp:root>
