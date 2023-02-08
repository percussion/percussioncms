<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="track" uri="http://www.percussion.com/soln/p13n/tracking"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="segments" content="test1:1,test2:2,test3:3"> 

<title>P13N Delivery AJAX Test</title>
<style type="text/css">
	
</style>
<script type="text/javascript" src="<%=request.getContextPath()%>/solution/resources/scripts/jquery-1.2.2.js">
</script>



</head>
<body>
<h1>P13N Track Set AJAX Test</h1>

<track:set requestURI="http://localhost:8080/P13N/track/track" segmentWeights="a,b:2"/>

<hr/>


</body>
</html>