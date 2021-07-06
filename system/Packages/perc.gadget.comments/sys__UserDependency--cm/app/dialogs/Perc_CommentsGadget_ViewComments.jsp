<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
	String locale= PSRoleUtilities.getUserCurrentLocale();
	String lang="en";
	if(locale==null){
		locale="en-us";
	}else{
		if(locale.contains("-"))
			lang=locale.split("-")[0];
		else
			lang=locale;
	}
	String debug = request.getParameter("debug");
	boolean isDebug = "true".equals(debug);
	String debugQueryString = isDebug ? "?debug=true" : "";
	String site = request.getParameter("site");
	if (site == null)
		site = "";
	if (debug == null)
		debug = "false";
%>
<i18n:settings lang="<%=locale%>" prefixes="perc.ui." debug="<%=debug%>"/>
<!DOCTYPE html>
<html lang="<%= lang %>">

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Comment</title>
		<link rel="stylesheet" type="text/css" href="/cm/themes/smoothness/jquery-ui-1.8.9.custom.css" />
		<link rel="stylesheet" type="text/css" href="/cm/gadgets/repository/common/perc_common_gadget.css" />
		<link rel="stylesheet" type="text/css" href="/cm/gadgets/repository/common/css/PercDataTable.css" />
		<link rel="stylesheet" type="text/css" href="/cm/gadgets/repository/perc_comments_gadget/Perc_CommentsGadget_ViewComments.css" />
		<script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%= locale%>"></script>
	</head>
	<body>
		<div id="perc-gadget-comments-viewComments-container">
		</div>
		<%@include file="/cm/app/includes/common_js.jsp" %>

		<script src="/cm/jslib/profiles/3x/jquery/plugins/jquery-timeago/jquery.timeago.js"></script>
		<script src="/cm/jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.getUrlParam.js"></script>
		<script src="/cm/jslib/profiles/3x/jquery/plugins/jquery-datatables/js/jquery.dataTables.js"></script>
		<script src="/cm/jslib/profiles/3x/jquery/plugins/jquery-datatables-fixedheader/js/dataTables.fixedHeader.js"></script>
        <script src="/cm/widgets/PercDataTable/PercDataTable.js"></script>
		<script src="/cm/gadgets/repository/perc_comments_gadget/PercCommentsGadgetService.js"></script>
		<script src="/cm/gadgets/repository/perc_comments_gadget/Perc_CommentsGadget_ViewComments.js"></script>
	</body>
</html>
