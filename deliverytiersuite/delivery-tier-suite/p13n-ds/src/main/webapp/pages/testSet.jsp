<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

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
<script type="text/javascript" 
	src="<%=request.getContextPath()%>/solution/resources/scripts/p13n/perc_p13n_track.js">
</script>


</head>
<body>
<h1>P13N Track AJAX Test</h1>

<hr/>

<script type="text/javascript">
//<![CDATA[
//
p13nTrackSet();
//]]>
</script>

<!--  This span element should be replaced -->
<span class="p13nTrackResult" />

</body>
</html>