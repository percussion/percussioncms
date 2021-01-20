<%@page%>
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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
   response.sendRedirect(response.encodeRedirectURL("/cm/"));
%>

<%
String method = request.getMethod();
// Redirect webdav root requests
if (method.equals("OPTIONS") || method.equals("PROPFIND"))
{
   response.sendRedirect(response.encodeRedirectURL("/Rhythmyx/rxwebdav"));
   return;
}
%>

<html>
<head>
<title>Rhythmyx Application Server Home</title>
<link rel="stylesheet" href="/Rhythmyx/sys_resources/css/menupage.css"
	type="text/css" />
</head>
<body leftmargin="0" marginheight="0" marginwidth="0" topmargin="0">
<table height="66" width="100%" background="/Rhythmyx/sys_resources/images/banner_bkgd.jpg" style="background-attachment: fixed; background-repeat: no-repeat;">
<tr>
<td>
</td>
</tr>
</table>
<div style="margin-left:10">
<h2>Rhythmyx Application hosted on JBoss&#8482;</h2>
<ul>
<li><a href="/Rhythmyx" title="Content Explorer">Content Explorer</a></li>
<li><a href="/Rhythmyx/ui/admin" title="Administrative Links">Administrative Links</a></li>
<li><a href="/Rhythmyx/sys_resources/ui/gwt/com.percussion.gwt.pkgmgtui.PkgMgtUI/PkgMgtUI.html" title="Package Manager">Package Manager</a></li>
<li><a href="/Rhythmyx/test" title="Testing and Debugging tools for implementers">Testing and Debugging tools for implementers</a></li>
<li><a href="/Rhythmyx/taxonomy/taxonomy.htm" title="Taxonomy Admin">Taxonomy Admin</a></li>
<li><a href="http://www.percussion.com/" title="Percussion Home Page">Percussion Home Page</a></li>
<li><a href="/Rhythmyx/linkback/help" title="Linkback Help">Linkback Help</a></li>
<li><a href="/Rhythmyx/Administration/DeveloperTools/InstData/Windows/VM/devToolsSetup.exe" title="Developer Tools Installer">Developer Tools</a></li>
</ul>
<h3>JBoss Online Resources </h3>
<ul>
  <li><a href="http://www.jboss.org" title="JBoss Site">JBoss Site</a>
  <li><a href="http://jboss.org/products/jbossas/docs" title="JBoss 4.0 documentation">JBoss 4.0 documentation </a>
  <li><a href="http://www.jboss.org/wiki/Wiki.jsp" title="JBoss Wiki">JBoss Wiki </a>
  <li><a href="http://www.jboss.org/index.html?module=bb" title="JBoss forums">JBoss forums </a></li>
</ul>
<h3>JBoss Management </h3>
<ul>
  <li><a href="/status" title="Tomcat status">Tomcat status </a><a href="/status?full=true" title="(full)">(full) </a><a href="/status?XML=true" title="(XML)">(XML) </a>
  <li><a href="/jmx-console/" title="JMX Console">JMX Console </a>
  <li><a href="/web-console/" title="JBoss Web Console">JBoss Web Console </a></li>
</ul>
JBoss&#8482; Application Server
</div>
</body>
</html>

