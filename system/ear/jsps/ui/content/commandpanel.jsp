<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>


<%
   String locale = PSI18nUtils.getSystemLanguage();
%>
<%
   String idPrefix = request.getParameter("idPrefix");        
%>
<table align="center" cellpadding="0" cellspacing="0" border="0">
<tr>	
	<%if(idPrefix.equals("ps.content.searchpanel.")){%>
	<td>
		<button style="border: 1px solid black; width: 70px;" dojoType="ps:PSButton" id="<%= idPrefix %>searchBackButton">
			${rxcomp:i18ntext('jsp_commandpanel@back',locale)}
		</button>
	</td>
	<td>&nbsp;</td>
	<%}%>
	<td>
		<button style="border: 1px solid black; width: 70px;" dojoType="ps:PSButton"
		      id="<%= idPrefix %>okButton" disabled="true" caption="${rxcomp:i18ntext('jsp_commandpanel@open',locale)}"/>
	</td>
	<td>&nbsp;</td>
	<td>
		<button style="border: 1px solid black; width: 70px;" dojoType="ps:PSButton" id="<%= idPrefix %>cancelButton">
			${rxcomp:i18ntext('jsp_commandpanel@close',locale)}
		</button>
	</td>	
</tr>
</table>
