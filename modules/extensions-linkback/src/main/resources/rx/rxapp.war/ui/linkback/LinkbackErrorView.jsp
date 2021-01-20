<%@page %>
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

<jsp:useBean id="message" class="java.lang.String" scope="request"/>
<html>
<head>
<title>Linkback Error</title>
<style type="text/css">
span.error {
color:red;
}
a.bmk {
border:1px outset #DDDDDD;
padding:1px;
vertical-align:1px;
}
a.bmk {
background:#D8DFFF none repeat scroll 0%;
color:darkgreen;
font-family:sans-serif;
font-size:80%;
text-decoration:none;
}
</style>
</head>
<body>
<p>
This page does not support Linkback.  Please contact your administrator for assistance.
</p>
<% if (message != null && message.length() > 0) { %>
<span class="error">Error Details: <%=message%></span>
<% } %>
</body>
</html>
