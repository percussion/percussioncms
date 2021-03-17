<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils,com.percussion.services.utils.jspel.*" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
<%
   String locale = PSI18nUtils.getSystemLanguage();
   String root = request.getContextPath();
%>
<html>
	<head>
		<title>Desktop Content Explorer Header</title>
		<link rel="stylesheet" type="text/css" href="<%=root%>/sys_resources/css/templates.css"/>
		<link rel="stylesheet" type="text/css" href="<%=root%>/rx_resources/css/templates.css"/>
		<link rel="stylesheet" type="text/css" href="{concat('<%=root%>/rx_resources/css/',$lang,'/templates.css')}"/>
	</head>
	<body>
		<div id="RhythmyxBanner"><!--Background image from templates.css-->
			<% if(PSRoleUtilities.getUserRoles().contains("Admin")){ %>
			<table width="500" cellpadding="0" cellspacing="0" border="0" class="rx-banner-table">
				<tr class="rx-banner-row" valign="bottom" align="right">
					<td>
						<a href="<%=root%>/ui/publishing/SiteList.faces" class="button" accesskey="d">Publishing Design</a>
						<a href="<%=root%>/ui/pubruntime" class="button" accesskey="r">Publishing Runtime</a>
						<a href="<%=root%>/Rhythmyx/sys_wfEditor/welcome.html?sys_componentname=wf_all&sys_pagename=wf_all" class="button" accesskey="w">Workflow</a>
						<a href="<%=root%>/ui/admin" class="button" accesskey="a">Admin</a>
					</td>
				</tr>
			</table>
			<% } %>
		</div>
	</body>
</html>


