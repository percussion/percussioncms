<%@ page import="java.util.*" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.lang.String" %>
<%@ page import="com.percussion.services.guidmgr.*" %>
<%@ page import="com.percussion.services.system.*" %>
<%@ page import="com.percussion.util.*" %>
<%@ page import="com.percussion.design.objectstore.*" %>
<%@ page import="com.percussion.services.catalog.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
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
   String roleName = request.getParameter("rolename");
   String nameFilter = request.getParameter("namefilter");
   IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();

   IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();
   List members = sysSvc.getAdhocRoleMembers(guidMgr.makeGuid(new PSLocator(contentId)),  
         guidMgr.makeGuid(transitionid,PSTypeEnum.WORKFLOW_TRANSITION),roleName,nameFilter);
%>
<div style="width:100%;height:100%;overflow:auto;">
   <table cellspacing="1" width="100%">
		<%int memeberCount = 0;%>
		<%for(int i=0;i<members.size();i++){
			String member = (String)members.get(i);
			if(member.equals("rxserver"))
			{
				continue;
			}
			memeberCount++;
		%> 
	      <tr bgcolor="white">
	         <td align="center" width="10%"><input id="ps.workflow.adhocusercheckbox_<%=i%>" value="<%=member%>" type="checkbox" onclick="ps.aa.controller.wfActions.onUserChecked()"/></td>
	         <td width="90%"><%=member%></td>
	      </tr>
	   <%}%>
	   <%if(memeberCount < 1)
	   {%>
	      <tr bgcolor="white">
	         <td width="100%" align="center"><font color="red"><b>No results found for your search criteria.</b></font></td>
	      </tr>
	   <%}%>
   </table>
   <input type="hidden" id="ps.workflow.adhocusercount" value="<%=memeberCount%>"/>
</div>
