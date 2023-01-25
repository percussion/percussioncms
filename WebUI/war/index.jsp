

<%
  	response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setDateHeader("Expires", -1);
    String redirectURL = "app/?"+ request.getQueryString();
    response.sendRedirect(redirectURL);%>
