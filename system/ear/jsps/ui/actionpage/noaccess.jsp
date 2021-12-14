<%@page pageEncoding="UTF-8" contentType="text/html; charset=utf-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
	
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
	
	String path = request.getContextPath();
   String basePath = request.getScheme() + "://"
               + request.getServerName() + ":" + request.getServerPort() + path
               + "/";
%>

<html>
<head>
	<base href="<%=basePath%>ui/actionpage">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link href="../sys_resources/css/actionpagefonts.css" rel="stylesheet" type="text/css"/>
	<link href="../rx_resources/css/actionpagefonts.css" rel="stylesheet" type="text/css"/>
	<link href="../sys_resources/css/actionpagelayout.css" rel="stylesheet" type="text/css"/>
	<title>${rxcomp:i18ntext('psx.sys_ActionPage.ActionPage@Rhythmyx Action Panel for',
		param.sys_lang)}&#160;${title}</title>
	<%@include file="../header.jsp"%>
</head>
<body>
	<div id="RhythmyxBanner" style="border-bottom:none;">
		<table width="100%" cellpadding="0" cellspacing="0" border="0">
			<tr>
				<td class="action-panel-header">${rxcomp:i18ntext('jsp_actionpage@Action Panel', param.sys_lang)}</td>
				<td align="right"  valign="bottom"><%@ include file="../userstatus-inner.jsp" %></td>
			</tr>
		</table>
	</div>
	<h3>${rxcomp:i18ntext('jsp_actionpage@User has no matching community access for',
		param.sys_lang)}&#160;${title}</h3>
</body>
</html>
