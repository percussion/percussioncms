<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities,com.percussion.server.PSServer,com.percussion.i18n.PSI18nUtils" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>

<%
    String isEnabled = PSServer.getServerProps().getProperty("enableDebugTools");

    if(isEnabled == null)
        isEnabled="false";

    if(isEnabled.equalsIgnoreCase("false")){
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    String fullrolestr = PSRoleUtilities.getUserRoles();

    if (!fullrolestr.contains("Admin"))
        response.sendError(HttpServletResponse.SC_NOT_FOUND);

%>
<html>
	<head>
		<title>Debugging and testing pages</title>
		<link rel="stylesheet" href="/sys_resources/css/menupage.css"
		type="text/css" />
	</head>
   <body leftmargin="0" marginheight="0" marginwidth="0" topmargin="0">
      <table height="66" width="100%" background="/sys_resources/images/banner_bkgd.jpg" style="background-attachment: fixed; background-repeat: no-repeat;">
         <tr>
            <td>
            </td>
         </tr>
      </table>
      <div style="margin-left:10">
         <p><a href="./search.jsp">Test JSR-170 searches</a></p>
         <p><a href="./velocitylog.jsp">Retrieve velocity log from server</a></p>
         <p><a href="./jnditest.jsp">Test bound JDBC resources</a></p>
      </div>
   </body>
</html>
