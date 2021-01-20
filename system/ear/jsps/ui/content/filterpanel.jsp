<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
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
