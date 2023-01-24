<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>


<%
   String locale = PSI18nUtils.getSystemLanguage();
%>
<%
   String idPrefix = request.getParameter("idPrefix");
%>
<table style="font-size:0.9em" align="left" cellpadding="2" cellspacing="0" border="0">
	<tr>
		<td>
			<div id="<%= idPrefix %>nameAndCtypeFilterDiv">
				<table>
					<tr>
						<td>&nbsp;</td>
						<td>
							<span class="PsDojoLabelText">${rxcomp:i18ntext('jsp_filterpanel@filterbyname',locale)}:&nbsp;</span><input style="font-size:0.9em" type="text" id="<%= idPrefix %>filterText" value="" size="30" />
						</td>
						<td>&nbsp;</td>
						<td>
							<span class="PsDojoLabelText">${rxcomp:i18ntext('jsp_filterpanel@filterbytype',locale)}:&nbsp;&nbsp;</span>
							<select style="font-size:0.9em" id="<%= idPrefix %>ctypeList">
					            <option value='-1'>All</option>
					            <option value='-2'>_____________</option> <%-- a dummy option to maintain the dropdown size --%>
							</select>		
						</td>
					</tr>	
				</table>
			</div>
		</td>
	</tr>
</table>
