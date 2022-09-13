<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>P13N Delivery AJAX Test</title>
<style type="text/css">
	.snip {
		border : thin solid red;
	}
	.contentId {
		margin-right: 1em;
	}
	.p13nRuleItemSlot {
		border : thin solid green;
	}
</style>
<%
	String ruleItemId = request.getParameter("ruleItemId");
%>
<script type="text/javascript" src="<%=request.getContextPath()%>/solution/resources/scripts/jquery-1.2.2.js">
</script>
<script type="text/javascript" 
	src="<%=request.getContextPath()%>/solution/resources/scripts/p13n/perc_p13n_delivery.js">
</script>
<script type="text/javascript">
P13NRuleItem.prototype.url = "/P13N/delivery/deliver";
</script>


</head>
<body>
<h1>P13N Delivery AJAX Test</h1>


<% if (ruleItemId == null)  { %>
<div style="color:red; font-size: x-large">
Pass the ruleItemId request parameter to specify which rule to load.
</div>
<%} else { %>
<div style="margin:1em">
Rule Item : <%=ruleItemId %> is below here.
</div>
<span class="p13nRuleItemId_<%=ruleItemId%>"><!-- To be replaced --></span>
<script type="text/javascript">
//<![CDATA[
(function() {
	var p13n_rule_item = new P13NRuleItem('<%=ruleItemId%>', '', '', '', '');
	p13n_rule_item.run();
})();
//]]>
</script>
<% } %>
</body>
</html>