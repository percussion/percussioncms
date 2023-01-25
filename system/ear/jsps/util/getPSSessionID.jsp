<%@ page import="com.percussion.server.PSRequest" %>
<%@ page import="com.percussion.utils.request.PSRequestInfo" %>
<%@ page contentType="application/javascript" %>


<%
   String pssessionid = ""; 
   Object psrequest = 
      PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
   if(psrequest != null)
   {
      String theId = 
         ((PSRequest)psrequest).getSecurityToken().getUserSessionId();
      if(theId != null)
         pssessionid = theId;
   }
%>
var pssessionid = "<%=pssessionid%>";
