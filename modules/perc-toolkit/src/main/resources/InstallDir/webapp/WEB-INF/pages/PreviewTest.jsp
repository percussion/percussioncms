<%@page import="com.percussion.pso.preview.SiteFolderLocation"%>
<%@page import="com.percussion.security.SecureStringUtils"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="java.util.Iterator"%>
<%@ page import="java.util.List" %>
<html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<jsp:useBean id="preview" class="com.percussion.pso.preview.SiteFolderFinderImpl" scope="request"/> 

<head><title>Preview Tester Page</title> 
</head> 
<body>
<h2>Preview Tester</h2>
<%

      //Checking for vulnerability
      String str = request.getQueryString();
      if(str != null && str != ""){
            response.sendError(response.SC_FORBIDDEN, "Invalid QueryString!");
      }

      String cid = StringUtils.defaultString(request.getParameter("sys_contentid"));
      //Checking for vulnerability
      if(!SecureStringUtils.isValidPercId(cid)){
            response.sendError(response.SC_FORBIDDEN, "Invalid cid!");
      }
      String fid = StringUtils.defaultString(request.getParameter("sys_folderid"));
      //Checking for vulnerability
      if(!SecureStringUtils.isValidPercId(fid)){
            response.sendError(response.SC_FORBIDDEN, "Invalid fid!");
      }
      String sid = StringUtils.defaultString(request.getParameter("sys_siteid"));
      //Checking for vulnerability
      if(!SecureStringUtils.isValidPercId(sid)){
            response.sendError(response.SC_FORBIDDEN, "Invalid sid!");
      }
%>
<csrf:form method="POST" action="PreviewTest.jsp">
<p>Content ID:<input name="sys_contentid" type="text" value="<%=cid%>" /> </p>
<p>Folder ID:<input name="sys_folderid" type="text" value="<%=fid%>"/> </p>
<p>Site ID:<input name="sys_siteid" type="text" value="<%=sid%>" /> </p>
<p><input name="submit" type="submit" value="submit" /> </p>
</csrf:form>
<%
if(StringUtils.isNotBlank(cid))  
{
%>
<table> 
<tr><td>Site</td><td>Path</td><td>Folder</td></tr>
 
<%
  
   List plist = preview.findSiteFolderLocations(cid,fid,sid); 
   Iterator pitr = plist.iterator();
   while(pitr.hasNext())
   {
      SiteFolderLocation loc = (SiteFolderLocation) pitr.next(); 
      %>
      <tr>
      <td><%=loc.getSiteName()%></td><td><%=loc.getFolderPath()%></td><td><%=loc.getFolderid()%></td>
      </tr>
      <% 
   }
}
%>
</body>
</html>
