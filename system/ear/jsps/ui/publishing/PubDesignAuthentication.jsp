<%@page errorPage="/ui/error.jsp" pageEncoding="UTF-8"
   contentType="text/html; charset=UTF-8"
   import="com.percussion.rx.ui.jsf.beans.PSTopNavigation"%>



<%
	if (!PSTopNavigation.hasPubDesignCompRoles())
	{
   	response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
%>
