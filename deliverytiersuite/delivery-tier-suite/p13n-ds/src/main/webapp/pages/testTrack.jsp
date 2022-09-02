<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="segments" content="6:1,7:3" /> 

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
jQuery(function() {
	jQuery('body').bind("p13nAfterTracking", function(e,d,p) {
		var message = "Tracking completed: " + d.status + "<br/> Message: " + d.errorMessage;
		message += "<br/>" + "actionName=" + p.actionName  + "</pre>";
		console.log(d);
		console.log(p);
		jQuery(".p13nTrackMessage").html(message);
	});
});
var p13N_TrackingService="../track/track";
p13nTrackUpdate();
</script>

<!--  This span element should be replaced -->
<div style="margin:1em; padding:1em; border: thin solid blue;" class="p13nTrackMessage"></div>
<span class="p13nTrackResult" />

</body>
</html>