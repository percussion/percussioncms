<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>



<%
   /* Always set focus on "Console" node */
 %>
<c:set var="invoke_focusOnStartingNode" scope="request"
   value="${sys_admin_navigation.focusOnStartingNode}" />

<%
   response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
      + "/ui/admin/console.faces"));
%>
