<%@ page import="java.util.*" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.lang.String" %>
<%@ page import="com.percussion.services.guidmgr.*" %>
<%@ page import="com.percussion.design.objectstore.*" %>
<%@ page import="com.percussion.services.system.*" %>
<%@ page import="com.percussion.util.*" %>
<%@ page import="com.percussion.services.workflow.*" %>
<%@ page import="com.percussion.server.*" %>
<%@ page import="com.percussion.cx.objectstore.*" %>
<%@ page import="org.json.JSONArray" %>
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
   //@todo Better to move this to a seperate bean.
   String contentId = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
   List contentids = new ArrayList();
   IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
	contentids.add(guidMgr.makeGuid(new PSLocator(contentId)));
   
   IPSRequestContext reqCtx = (IPSRequestContext)request.getAttribute(
      "RX_REQUEST_CONTEXT");
   int userCommunity = -1;
	String usercomm = (String)reqCtx.getSessionPrivateObject(
	    IPSHtmlParameters.SYS_COMMUNITY);
	if(usercomm != null)
		userCommunity = Integer.parseInt(usercomm);
	String userName = reqCtx.getUserName();
	String locale = reqCtx.getUserLocale();
	List userRoles = reqCtx.getSubjectRoles();

   IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();
   List assignmentTypes = sysSvc.getContentAssignmentTypes(contentids,  
         userName, userRoles, userCommunity);

	IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
	List actions = svc.getAllWorkflowActions(contentids, 
	    assignmentTypes, userName, userRoles, locale);
	Map actionMap = new HashMap();
	boolean hasCommentBox = false;
	boolean hasAdhocBox = false;
	for(int i=0;i<actions.size();i++)
	{
  		PSMenuAction action = (PSMenuAction)actions.get(i);
		//Add name to the array
		String name = action.getName();
		JSONArray id = new JSONArray();
		id.put(name);
		String wfAction = action.getParameter("WFAction");
		//Add comment to array
		String comment = action.getCommentRequired();
		String commentVal = "1";//Optional
		if(comment.equals(PSMenuAction.VAL_BOOLEAN_TRUE))
			commentVal = "2"; //Required
		else if(comment.equals(PSMenuAction.VAL_HIDE))
			commentVal = "0";//Hide
		id.put(commentVal);
		if(Integer.parseInt(commentVal)>0)
			hasCommentBox = true;
		//Add adhoc to the array			
		if(action.isAdhoc())
		{
			id.put("1"); //Adhoc required
			hasAdhocBox = true;
		}
		else
		{
			id.put("0"); //no Adhoc 
		}
		
		//Add action type to array
		String actionType = "4"; //Transition
		if(name.equals(PSMenuAction.CHECKIN_ACTION_NAME))
			actionType = "0"; //Checkin
		else if(name.equals(PSMenuAction.FORCE_CHECKIN_ACTION_NAME))
			actionType = "1";//Force Checkin
		else if(name.equals(PSMenuAction.CHECKOUT_ACTION_NAME))
			actionType = "2"; //Checkout
		else if(wfAction.equals("Quick Edit"))
			actionType = "3"; //Transition and Checkout
		id.put(actionType);
		
		//Add wfaction name to array
		id.put(wfAction);
		
		//Add wfaction name to array
		id.put(action.getParameter(IPSHtmlParameters.SYS_TRANSITIONID));
		
		//Add action label to array
		id.put(action.getLabel());
		String label = action.getLabel();
		if(wfAction.equals("Quick Edit"))
			label = "Quick Edit Check-out";
		actionMap.put(label,id.toString());
	}
%>
<%if(!actionMap.isEmpty()){%>
<div id="ps.workflow.actionPane" style="overflow:auto; border: 0px solid black;">
   <div id="ps.workflow.actionDiv">
	  <table width="100%">
		 <tr>
			<td align="right" width="30%">Action:</td>
			<td  width="70%">
			   <select id="ps.workflow.workflowActionSelect">
				  <%
				  Iterator iter = actionMap.keySet().iterator();
				  while(iter.hasNext()){
				  		String lbl = (String)iter.next();
				  		String wid = (String)actionMap.get(lbl);
				  %>
				  <option value='<%=wid%>'><%=lbl%></option>
				  <%}%>
			   </select>
			</td>
		 </tr>
	  </table>
   </div>

<% if (hasCommentBox) { %>
   <div>
	  <table width="100%">
		 <tr>
			<td align="right" width="30%"><span id="ps.workflow.commentStar" style="visibility:hidden"><font color="red">*</font></span>Comments:</td>
			<td>
			   <textarea id="ps.workflow.commentText" cols="30" rows="6"></textarea>
			</td>
		 </tr>
	  </table>
   </div>
<%} // hasCommentBox%>

<% if (hasAdhocBox) { %>
   <div>
	  <table width="100%">
		 <tr>
			<td align="right"  width="30%">Ad-hoc Assignees:<br/><button style="border: 1px solid black;" dojoType="Button" id="ps.workflow.wgtButtonAdhocSearch">Search</button></td>
			<td align="left"  width="70%">
			   <textarea id="ps.workflow.adhocUsers" cols="30" rows="6"></textarea>
			</td>
		 </tr>
	  </table>
   </div>
<%} // hasAdhocBox %>

   <div id="ps.workflow.buttonsDiv">
		<table align="center" width="100%" cellpadding="2" cellspacing="0" border="0">
		 <tr>
				<td align="right">
					<button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.workflow.wgtButtonSubmit">
						Submit
					</button>
				</td>
				<td align="left">
					<button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.workflow.wgtButtonCancel">
						Close
					</button>
				</td>
		 </tr>
	  </table>
   </div>
</div>
<%}else{%>
	<div>
		<table width="100%" align="left" cellpadding="1">
			<tr>
				<td align="center" vslign="middle">
					You are not authorized to perform any workflow action.
				</td>
			</tr>
			<tr>
				<td height="5">
					<img src="/sys_resources/images/spacer.gif" height="5"/>
				</td>
			</tr>
			<tr>
				<td align="center">
					<button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.workflow.wgtButtonClose">
						Close
					</button>
				</td>
			</tr>
		</table>
	</div>
<%}%>
