<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="track" uri="http://www.percussion.com/soln/p13n/tracking"  %>

<%
String username = request.getParameter("username");
String label = request.getParameter("label");
if (label == null || label.length()<1) {
	label=username;
}

String[] segments = request.getParameterValues("segment");

String segmentString="";
if (segments!= null) {
for (int i=0; i< segments.length; i++) {
    	String par = segments[i];
      	if (par != null && par.length() > 0) {
       		if (i>0) segmentString +=",";
       		segmentString+=par;
      	}
    }

}
%>
<track:login userName="<%=username%>" label="<%=label%>" segmentWeights="<%=segmentString%>"/>

</body>
</html>