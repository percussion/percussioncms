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
