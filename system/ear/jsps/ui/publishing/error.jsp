<%@page isErrorPage="true" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
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

<html>
	<head>
		<title>Publishing error</title>
		<%@include file="../header.jsp"%>
	</head>
	<body>	
		<div style="background-color: white; margin: 10px; padding-top: 0px; padding: 10px">
			<p><img src="../../sys_resources/images/banner_bkgd.jpg"></p>
			<h2>Problem during the publishing of one or more items</h2>
			<TABLE>
			<tr><td valign="top">Exception Class:</td><td><%= exception.getClass() %></td></tr>
			<tr><td valign="top">Message:</td><td><%= exception.getMessage() %></td></tr>
			</TABLE>
			<i>Please note: More information may be available on the console</i>
		</div>
	</body>
</html>