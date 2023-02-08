<%@page contentType="text/html; charset=utf-8" 
   import="com.percussion.services.utils.jspel.*"
   import="com.percussion.i18n.*" 
   import="java.net.URLEncoder"
   import="org.apache.commons.lang.*"
   %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%
	String fullcommstr = PSRoleUtilities.getUserCurrentCommunity();
	String commstr = StringUtils.abbreviate(fullcommstr, 35);
	String fullrolestr = PSRoleUtilities.getUserRoles();
	String rolestr = StringUtils.abbreviate(fullrolestr, 35);
	String user = request.getRemoteUser();
	String locale = PSRoleUtilities.getUserCurrentLocale();
	String localeDisplay = PSLocaleManager.getInstance().getLocale(locale).getDisplayName();
	String redirect = (String) request.getParameter("sys_redirecturl");
	String rxloginurl = "/login";
	if (redirect != null)
	{
	   rxloginurl += "&sys_redirecturl=" + URLEncoder.encode(redirect, "UTF-8");
	}
	// Bind variables for use in page
	pageContext.setAttribute("locale",locale);
	pageContext.setAttribute("fullrolestr",fullrolestr);
	pageContext.setAttribute("rolestr",rolestr);
	pageContext.setAttribute("localeDisplay",localeDisplay);
	pageContext.setAttribute("commstr",commstr);
	pageContext.setAttribute("fullcommstr",fullcommstr);	
%>
<table border="0" cellpadding="0" cellspacing="0" class="user-info" width="100%">
	<tr>
		<td class="field">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@User",locale))}:</td>	
		<td><%= user %></td>
	</tr>
	<tr>
		<td class="field">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Roles",locale))}:</td>
		<td><script>
				   var textWin = null;
				   function textWindow(s)
				   {
				      textWin = window.open('','UserRoles','width=500,height=100,resizable=yes');
					   textWin.document.write('<h3>${fn:escapeXml(fullrolestr)}</h3>');
					   textWin.document.title='${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@User Roles",param.sys_lang))}';
					   textWin.document.close();
				      setTimeout('textWin.close()',10000);				      
				   }
				</script><a class="banner_blue" href="javascript:void(0)"
			title="${fn:escapeXml(fullrolestr)}"
			OnClick="textWindow('${fn:escapeXml(fullrolestr)}')">${fn:escapeXml(rolestr)}</a></td>
	</tr>
	<tr>
		<td class="field">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Community",locale))}:</td>
		<td><a
			href="<%= rxloginurl %>"
			title="${fn:escapeXml(fullcommstr)}"
			target="_parent">${fn:escapeXml(commstr)}</a></td>
	</tr>
	<c:if test="${rxcomp:getLocaleCount() > 1}">
	<tr>
		<td class="field">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Locale",locale))}:</td>
		<td><a
			href="<%= rxloginurl %>"
			target="_parent">${fn:escapeXml(localeDisplay)}</a></td>
	</tr>	
	</c:if>
	<tr>
			<td rowspan="4" valign="bottom" align="right"><a href="../rxloggingout.jsp" target="_parent"><img
			alt="Log out" title="Log out" height="17"
			src="/rx_resources/images/${locale}/logout.gif" width="62"/></a></td>
	</tr>
</table>
