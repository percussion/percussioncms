<%@page import="com.percussion.server.PSServer" %>


<%
    /* this page returns a full version string of the form:
    
            Version major.minor.micro  Build build# (buildCounter)
        
       eg: Version 6.7.0  Build 201004X01 (80)
    */
    String ver = PSServer.getVersionString();
%>
<%= ver %>
