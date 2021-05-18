<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="/Rhythmyx/sys_resources/css/menupage.css"
			type="text/css" />		
<title>Image Editor Test Page</title>
</head>
<body>
<p><img src="/Rhythmyx/sys_resources/images/banner_bkgd.jpg"></p>
<h2>Image Editor Test Page</h2>
<ul>
<c:set var="urlimage" value="/Rhythmyx/user/apps/imageeditor/images/img${masterImage.imageKey}.jpg"/>
<li>System Title: <c:out value="${masterImage.sysTitle}" /> </li>
<li>Display Title: <c:out value="${masterImage.displayTitle}" /></li> 
<li>Description: <c:out value="${masterImage.description}" /> </li>
<li>Alt: <c:out value="${masterImage.alt}" /></li> 
<li>Image Key: <c:out value="${masterImage.imageKey}" /> &nbsp; <a target="_new" href="${urlimage}">preview</a> </li> 
<li>Image Size: <c:out value="${masterImage.metaData.size}" /></li>
<li>Height: <c:out value="${masterImage.metaData.height}" /></li>
<li>Width: <c:out value="${masterImage.metaData.width}" /></li>
</ul> 

<table>
<c:forEach var="sized" items="${masterImage.sizedImages}">
<tr><td>
<ul>
<li>Size: <c:out value="${sized.sizeDefinition.label}" /></li> 
<li>Actual Height: <c:out value="${sized.metaData.height}" /></li>
<li>Actual Width: <c:out value="${sized.metaData.width}" /></li>
<li>Bytes: <c:out value="${sized.metaData.size}" /></li>
</ul>
</td>
<td>
<img src="/Rhythmyx/user/apps/imageeditor/images/img${sized.imageKey}.jpg"/>
</td>
</tr>
</c:forEach>
</table>


 
</body>
</html>