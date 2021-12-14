<%@ page import="java.util.*" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.lang.String" %>
<%@ page import="com.percussion.services.guidmgr.*" %>
<%@ page import="com.percussion.services.system.*" %>
<%@ page import="com.percussion.util.*" %>
<%@ page import="com.percussion.services.catalog.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.percussion.design.objectstore.*" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
   String contentId = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
   String transitionid = request.getParameter(IPSHtmlParameters.SYS_TRANSITIONID);
   IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();

   IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();
   List roles = sysSvc.getAdhocRoles(guidMgr.makeGuid(new PSLocator(contentId)),  
         guidMgr.makeGuid(transitionid,PSTypeEnum.WORKFLOW_TRANSITION));
   
	
%>
	<table width="100%" align="left" cellpadding="1" bgcolor="#e3edfa">
		<tr>
			<td width="15%%">Role:</td>
			<td width="40%">
				<select id="ps.workflow.adhocRole">
					<%for(int i=0;i<roles.size();i++){
						String role = (String)roles.get(i);%> 
						<option value="<%=role%>"><%=role%></option>
					<%}%>
				</select>
			</td>
			<td width="15%">Filter:</td>
			<td width="15%"><input type="text" id="ps.workflow.nameFilter" size="8"/></td>
			<td width="15%"><button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.workflow.wgtButtonSearch">Go</button></td>
	   </tr>
   </table>
