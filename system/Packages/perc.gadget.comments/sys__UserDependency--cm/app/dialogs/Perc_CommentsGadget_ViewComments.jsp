<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>


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
