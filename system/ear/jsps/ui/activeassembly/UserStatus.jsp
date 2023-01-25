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
	String fullrolestr = PSRoleUtilities.getUserRoles();
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
	pageContext.setAttribute("localeDisplay",localeDisplay);
	pageContext.setAttribute("fullcommstr",fullcommstr);	
%>
<table border="0" cellpadding="0" cellspacing="0" class="user-info" width="100%">
	<tr>
		<td class="field">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@User",locale))}:</td>	
		<td><%= user %></td>
	</tr>
	<tr>
		<td class="field">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Roles",locale))}:</td>
		<td>${fn:escapeXml(fullrolestr)}</td>
	</tr>
	<tr>
		<td class="field">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Community",locale))}:</td>
		<td><a
			href="<%= rxloginurl %>"
			target="_parent">${fn:escapeXml(fullcommstr)}</a></td>
	</tr>
	<c:if test="${rxcomp:getLocaleCount() > 1}">
	<tr>
		<td class="field">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Locale",locale))}:</td>
		<td><a
			href="<%= rxloginurl %>"
			target="_parent">${fn:escapeXml(localeDisplay)}</a></td>
	</tr>	
	</c:if>
</table>
