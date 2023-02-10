<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>


<%
	String locale = PSI18nUtils.getSystemLanguage();
	pageContext.setAttribute("locale",locale);
%>	
<!DOCTYPE HTML PUBLIC "-//W3C//Dtd HTML 4.0 Transitional//EN">
<html>
<head>
	<title>${rxcomp:i18ntext('jsp_logout@Rhythmyx Logout',locale)}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<META HTTP-EQUIV="Refresh" CONTENT="0; URL=./logout">
	<link rel="stylesheet" href="sys_resources/css/rxcx.css" type="text/css" media="screen" />
</head>

<body>

		<table class="RxLogin" cellpadding="0" cellspacing="0" border="0">
			<tr>
				<td><table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td width="25"><img height="25" src="rx_resources/images/${locale}/rhythmyx_login_topleft.gif" width="25"></td>
						<td class="rhythmyx_login_topbkgd"><img height="25" src="rx_resources/images/${locale}/blank-pixel.gif" width="25"></td>
						<td width="25"><img height="25" src="rx_resources/images/${locale}/rhythmyx_login_topright.gif" width="25"></td>
						</tr></table></td>
				<td class="RightShadow"><img src="rx_resources/images/${locale}/shadow-topright.gif" width="9" height="25" /></td>
			</tr>
			<tr>
				<td class="BannerCell"><img height="50" src="rx_resources/images/${locale}/rhythmyx_login_banner.jpg" width="516"></td>
				<td class="RightShadow">&nbsp;</td>
			</tr>
		<tr> 
			<td class="grayBKGD"> 
				<p class="windowName">${rxcomp:i18ntext('jsp_loggingout@loggingout',locale)}</p>
			</td>
			<td class="RightShadow">&nbsp;</td>
		</tr>
		<tr>
			<td class="BottomShadow">&nbsp;</td>
			<td><img src="rx_resources/images/${locale}/shadow-bottomright.gif" width="9" height="9"></td>
		</tr>
	</table>
<div class="copyright">&copy; Copyright Percussion Software @COPYRIGHTYEAR@</div>
</body>
</html>
