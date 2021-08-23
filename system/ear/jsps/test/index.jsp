<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities,com.percussion.server.PSServer,com.percussion.i18n.PSI18nUtils" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>
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
