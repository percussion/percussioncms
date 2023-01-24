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
