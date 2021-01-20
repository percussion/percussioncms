<%@ page 
   import="com.percussion.i18n.PSI18nUtils" 
   import="java.net.URLEncoder"
   import="java.text.MessageFormat"
   import="java.util.*"
   %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
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
	String locale = PSI18nUtils.getSystemLanguage();
	pageContext.setAttribute("locale",locale);
   //String msg = "You are not authorized to access the resource.";
%>	
<!DOCTYPE HTML PUBLIC "-//W3C//Dtd HTML 4.0 Transitional//EN">
<html>
<head>
	<title>${rxcomp:i18ntext('jsp_auth@auth_fail_title',locale)}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link rel="stylesheet" href="../sys_resources/css/rxcx.css" type="text/css" media="screen" />
</head>

<body>

		<table class="RxLogin" cellpadding="0" cellspacing="0" border="0">
			<tr>
				<td><table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td width="25"><img height="25" src="../rx_resources/images/${locale}/rhythmyx_login_topleft.gif" width="25"></td>
						<td class="rhythmyx_login_topbkgd"><img height="25" src="../rx_resources/images/${locale}/blank-pixel.gif" width="25"></td>
						<td width="25"><img height="25" src="../rx_resources/images/${locale}/rhythmyx_login_topright.gif" width="25"></td>
						</tr></table></td>
				<td class="RightShadow"><img src="../rx_resources/images/${locale}/shadow-topright.gif" width="9" height="25" /></td>
			</tr>
			<tr>
				<td class="BannerCell"><img height="50" src="../rx_resources/images/${locale}/rhythmyx_login_banner.jpg" width="516"></td>
				<td class="RightShadow">&nbsp;</td>
			</tr>
		<tr> 
			<td class="grayBKGD"> 
			   <p class="windowName">${rxcomp:i18ntext('jsp_auth@access_unauth_resource',locale)}</p>
			</td>
			<td class="RightShadow">&nbsp;</td>
		</tr>
		<tr>
			<td class="BottomShadow">&nbsp;</td>
			<td><img src="../rx_resources/images/${locale}/shadow-bottomright.gif" width="9" height="9"></td>
		</tr>
	</table>
<div class="copyright">&copy; Copyright Percussion Software @COPYRIGHTYEAR@</div>
</body>
</html>
