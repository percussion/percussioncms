<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.Map, com.percussion.demo.p13n.tracking.VisitorProfile,com.percussion.soln.p13n.tracking.web.VisitorProfileWebUtils"%>
<%@ taglib prefix="track" uri="http://www.percussion.com/soln/p13n/tracking"  %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>


<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="segments" content="test1,test2,test3"> 

<title>P13N Track Login Test</title>
<style type="text/css">
	
</style>
<script type="text/javascript" src="<%=request.getContextPath()%>/solution/resources/scripts/jquery-1.2.2.js">
</script>
<script type="text/javascript" 
	src="<%=request.getContextPath()%>/solution/resources/scripts/p13n/perc_p13n_track.js">
</script>
<%
String username = request.getParameter("username");
String label = request.getParameter("label");
%>


</head>
<body>
<h1>P13N Track Login Test</h1>
<form action="testTrackLogin.jsp">
Login as :<input type="edit" name="username" />
Label :<input type="edit" name="label" />
<input type="submit" value="login"/>
</form>

<hr/>
<% 

			VisitorProfile profile = VisitorProfileWebUtils.getVisitorProfileFromSession(session);
			String profileId="";
			String profileUserName = null;
			Map<String,Integer> weights;
			if (profile != null) {
				profileId = String.valueOf(profile.getId());
				profileUserName = profile.getUserid();
			weights = profile.getSegmentWeights();
%>
			Current User = <%=profile.getUserid() %><br/>
			Current User Label =  <%=profile.getLabel() %><br/>
			Current User Id =  <%=profile.getId() %><br/>
			Is Preview Profile =  <%=profile.isPreviewProfile() %><br/>
			Last Updated =  <%=profile.getLastUpdated() %><br/>
			Last Updated =  <%=profile.getLastUpdated() %><br/>
			<% for (String segment : weights.keySet() ) {%>
			Segment Weight "<%=segment%>" = <%=weights.get(segment) %>
			<%}
			} %>

</body>
</html>