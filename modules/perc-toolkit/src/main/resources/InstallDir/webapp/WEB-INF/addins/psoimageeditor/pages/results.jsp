<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<html>
	<head>
		<title>Results</title>
		<c:if test="${!empty redirectUrl}">
		<meta http-equiv="refresh" content="1;url=${redirectUrl}">
		</c:if>
		
		<link rel="stylesheet" href="/Rhythmyx/sys_resources/css/menupage.css" type="text/css" />	
        <link rel="stylesheet" href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/image_editor_forms.css" type="text/css" />
        <link href="/Rhythmyx/rx_resources/addins/psoimageeditor/css/image_editor_general.css" type="text/css" rel="stylesheet" />
        
        <script type="text/javascript" src="/Rhythmyx/sys_resources/js/browser.js"></script>
        <script type="text/javascript" src="/Rhythmyx/rx_resources/addins/psoimageeditor/js/psoimageeditor.js"></script>
        <script type="text/javascript">
            
        function handleOnLoad()
        {
           parentRefresh(); 
        };
        </script>
        
	</head>
	<body onload="handleOnLoad();">
	    <p><img src="/Rhythmyx/sys_resources/images/banner_bkgd.jpg"></p>	
		<h2>Results</h2>
		<p>Image <c:out value="${contentid}" /> Updated</p>
		<c:if test="${closeWindow == true}">
		<script type="text/javascript">
		   setTimeout("window.close();",2000); 
		</script> 
		</c:if>
	</body>
</html>