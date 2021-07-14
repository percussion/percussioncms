<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" 
    import="javax.jcr.query.Query"
    import="javax.jcr.query.QueryResult"  
    import="javax.jcr.query.RowIterator" 
    import="javax.jcr.query.Row" 
    import="javax.jcr.Value" 
    import="com.percussion.services.contentmgr.IPSContentMgr, com.percussion.services.contentmgr.PSContentMgrLocator, com.percussion.webservices.content.PSContentWsLocator, com.percussion.webservices.content.IPSContentWs, com.percussion.utils.guid.IPSGuid, com.percussion.server.webservices.PSServerFolderProcessor, com.percussion.server.PSRequest, com.percussion.utils.request.PSRequestInfo"
    import="com.percussion.design.objectstore.PSLocator, com.percussion.webservices.security.IPSSecurityWs, com.percussion.webservices.security.PSSecurityWsLocator"
    import="com.percussion.services.guidmgr.data.PSLegacyGuid, com.percussion.services.security.data.PSCommunity"
    import="com.percussion.cms.PSCmsException, com.percussion.webservices.PSErrorResultsException"
    import="com.percussion.cms.objectstore.PSObjectAclEntry, com.percussion.cms.objectstore.IPSDbComponent, com.percussion.cms.objectstore.PSObjectAcl, com.percussion.cms.objectstore.PSFolder"
    import="java.util.Map, java.util.Set, java.util.Collections, java.util.Map.Entry, java.util.Iterator, java.util.HashMap, java.util.Arrays, java.util.ArrayList, java.util.List, org.apache.commons.lang.StringUtils, javax.servlet.jsp.JspWriter"
    import="org.apache.log4j.Logger"
    import="com.percussion.services.utils.jspel.PSRoleUtilities"
    import="com.percussion.server.PSServer"
    %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
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
    String isEnabled = PSServer.getServerProps().getProperty("enableDebugTools");

    if(isEnabled == null)
        isEnabled="false";

    if(isEnabled.equalsIgnoreCase("false")){
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
                + "/ui/RxNotAuthorized.jsp"));
    }
    String fullrolestr = PSRoleUtilities.getUserRoles();

    if (!fullrolestr.contains("Admin"))
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
                + "/ui/RxNotAuthorized.jsp"));

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Update Folder Security</title>
</head>
<body>
<%! 
   // initialize variables used in the JSP page. 
   int ADMIN_ACCESS = PSObjectAclEntry.ACCESS_ADMIN | PSObjectAclEntry.ACCESS_WRITE | PSObjectAclEntry.ACCESS_READ; 
   int WRITE_ACCESS = PSObjectAclEntry.ACCESS_WRITE | PSObjectAclEntry.ACCESS_READ; 
   int READ_ACCESS  = PSObjectAclEntry.ACCESS_READ;
   int TYPE_ROLE    = PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE;
   int TYPE_VIRTUAL = PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL; 
   String EVERYONE  = PSObjectAclEntry.ACL_ENTRY_EVERYONE;
 
   IPSContentMgr mgr = PSContentMgrLocator.getContentMgr(); 
   IPSContentWs contentWs = PSContentWsLocator.getContentWebservice(); 
   PSServerFolderProcessor folderProcessor = getFolderProcessor(); 
   IPSSecurityWs securityWs = PSSecurityWsLocator.getSecurityWebservice();
   Map pmap = new HashMap(); 
   Logger logger = Logger.getLogger("UpdateFolderSecurity");
%>
<%! 
	/**
	 * @param virtualPerm virtual permission
	 * @param rolePerms role permissions in (String, String) paris.
	 */
	private void setFolderPermissions(PSLocator[] folderIds, String communityIdString, String virtualPerm, 
	  Map rolePerms, JspWriter out) throws java.io.IOException, PSCmsException, PSErrorResultsException
	{
		outputFolderPermission(folderIds, virtualPerm, rolePerms, out);
		int communityId = getCommunityId(communityIdString, out);
		
		logger.info("Updating " + folderIds.length + " Folders ...");
		for (int i=0; i < folderIds.length; i++)
		{
		   PSLocator id = folderIds[i];
		   setFolderPermission(id, communityId, virtualPerm, rolePerms, out);
		   
		   logger.info("Updated Folder[" + i + "] ID: " + id.getId());
		}
	}

	private int getCommunityId(String communityId, JspWriter out) throws java.io.IOException
	{
		int id = Integer.parseInt(communityId);
	
		if (id == -1)
		{
			return id;
		}

		PSSecurityWsLocator.getSecurityWebservice();
		List communities = securityWs.loadCommunities("*");
		for (int i=0; i<communities.size(); i++)
		{
		   PSCommunity comm = (PSCommunity) communities.get(i);
		   if (comm.getId() == id)
		   	   return id;
		} 
		
		throw new RuntimeException("Cannot find community ID: " + communityId);	
	}
	
	private void setFolderPermission(PSLocator folderId, int communityId, String virtualPerm, Map rolePerms, JspWriter out) 
	   throws java.io.IOException, PSCmsException, PSErrorResultsException
	{
	    List ids = Collections.singletonList(new PSLegacyGuid(folderId));
	    List folders = contentWs.loadFolders(ids);
		PSFolder folder = (PSFolder) folders.get(0); 
		
		folder.setCommunityId(communityId);
		
		//outputPermissions("BEFORE remove", folder, out);
		removeAclEntries(folder);
		
		//outputPermissions("AFTER remove", folder, out);
		setVirtualPermission(folder, virtualPerm);

		//outputPermissions("AFTER set virtaul", folder, out);
		setRolePermission(folder, rolePerms);
		
		//outputPermissions("AFTER set role permission", folder, out);
		folder = folderProcessor.save(folder);
		
		//outputPermissions("AFTER save the folder", folder, out);
		contentWs.saveFolders(Collections.singletonList(folder));
		
		//folders = contentWs.loadFolders(ids);
		//folder = (PSFolder) folders.get(0);		
		//outputPermissions("AFTER save the folder 2", folder, out);
	}
	
	private void validatePermissions(String communityId, String virtualPerm, Map rolePerms)
	{
		int id = Integer.parseInt(communityId);
		
		if (StringUtils.isBlank(virtualPerm))
		{
			throw new RuntimeException("Must specify virtual permissions.");
		}
		
		if (StringUtils.isNotBlank(virtualPerm))
		{
			int perm = getPermission(virtualPerm);
			if (perm == ADMIN_ACCESS)
				return;
		}
				
		PSObjectAclEntry aclEntry;
		Set entrySet = rolePerms.entrySet();
        Iterator it = entrySet.iterator();
        while (it.hasNext())
        {
        	Map.Entry entry = (Map.Entry) it.next();
        	int perm = getPermission((String)entry.getValue());
        	if (perm == ADMIN_ACCESS)
        		return;
        } 
	
		throw new RuntimeException("One of the permissions must be \"admin\".");
	}
	
	private void setVirtualPermission(PSFolder folder, String virtualPerm)
	{
		PSObjectAclEntry aclEntry;
		if (StringUtils.isNotBlank(virtualPerm))
		{
			int perm = getPermission(virtualPerm);
			aclEntry = new PSObjectAclEntry(TYPE_VIRTUAL, EVERYONE, perm);
			folder.getAcl().add(aclEntry);
		}
	}

	private void setRolePermission(PSFolder folder, Map rolePerms)
	{
		PSObjectAclEntry aclEntry;
		Set entrySet = rolePerms.entrySet();
        Iterator it = entrySet.iterator();
        while (it.hasNext())
        {
        	Map.Entry entry = (Map.Entry) it.next();
        	String roleName = (String)entry.getKey();
        	int perm = getPermission((String)entry.getValue());
        	aclEntry = new PSObjectAclEntry(TYPE_ROLE, roleName, perm);
        	folder.getAcl().add(aclEntry);
        } 
	}
	
	private void outputPermissions(String heading, PSFolder folder, JspWriter out) 
		throws java.io.IOException, PSCmsException
	{
		out.println("\n" + heading);
		//out.println("   FOLDER: " + folder.toString());
		
		PSObjectAclEntry aclEntry;
        Iterator it = folder.getAcl().iterator();
        while (it.hasNext())
        {
        	aclEntry = (PSObjectAclEntry) it.next();
        	out.println("  [" + aclEntry.getType() + "] name=" + aclEntry.getName() + ", permissions=" 
        	   + aclEntry.getPermissions() + ", isPersisted=" + aclEntry.isPersisted());
        }
	}
	
    private void removeAclEntries(PSFolder folder) throws PSCmsException
    {
    	PSObjectAcl acl = folder.getAcl();
        if (acl == null)
        {
        	folder.setAcl(new PSObjectAcl());
            return;
        }
        
        acl.clear();
    }	
%>

<h1>Update Folder Security</h1>
<p>
Use this page to update community and security of a folder and its descendant folders
</p>
<csrf:form method="POST" action="/test/updateFolderSecurity.jsp">

<%
    String[] allNames = expandParam(request.getParameterValues("qname"), 6);
         String[] allValues = expandParam(request.getParameterValues("qvalue"), 6);

		 String lastCommunityId = request.getParameter("CommunityId");
         if (StringUtils.isBlank(lastCommunityId))
         {
            lastCommunityId = "-1";
         }
         
		 String lastVirtualPerm = request.getParameter("VirtualPerm");
         if (StringUtils.isBlank(lastVirtualPerm))
         {
            lastVirtualPerm = "";
         }
		 
         String lastquery = request.getParameter("folderpath");
         if (StringUtils.isBlank(lastquery))
         {
            lastquery = "//Folders/Folder1";
         }
         
%>

Community ID
<br/>	
<input type="text" name="CommunityId" value="<%=lastCommunityId%>">
<br/>
<br/>

Virtual Permission
<br/>	
<input type="text" name="VirtualPerm" value="<%=lastVirtualPerm%>">

<br/>
<br/>
Role Permissions
<br/>	 
<table title="Query Parameters" border="1"> 
<thead>
<tr><th>Name</th><th>Permission</th></tr>
</thead>
   <tr>
       <td><input type="text" name="qname" maxlength="30" value="<%= allNames[0] %>" /> </td>
       <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[0] %>" /> </td>
   </tr>
   <tr>
       <td><input type="text" name="qname" maxlength="30" value="<%= allNames[1] %>" /> </td>
       <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[1] %>" /> </td>
   </tr>   
   <tr>
       <td><input type="text" name="qname" maxlength="30" value="<%= allNames[2] %>" /> </td>
       <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[2] %>" /> </td>
   </tr>   
   <tr>
       <td><input type="text" name="qname" maxlength="30" value="<%= allNames[3] %>" /> </td>
       <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[3] %>" /> </td>
   </tr>   
   <tr>
       <td><input type="text" name="qname" maxlength="30" value="<%= allNames[4] %>" /> </td>
       <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[4] %>" /> </td>
   </tr>   
   <tr>
       <td><input type="text" name="qname" maxlength="30" value="<%= allNames[5] %>" /> </td>
       <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[5] %>" /> </td>
   </tr>   

</table>
<br/>

<textarea name="folderpath" rows="5" cols="60">
<%=lastquery%>
</textarea>
<br/>
<input type="submit" name="execute" value="execute" label="Execute" /> 
</csrf:form>
<p>
<%if (request.getMethod().equals("POST")
               && request.getParameter("execute").equals("execute"))
  {
    try
    {
		out.println("<pre>");
		
		String communityId = request.getParameter("CommunityId").trim();
		String virtualPerm = request.getParameter("VirtualPerm").trim();
	   
		Map pmap = buildParamMap(allNames, allValues, out);
		
		validatePermissions(communityId, virtualPerm, pmap);
		
		String folderPath = request.getParameter("folderpath").trim();
	   
		IPSGuid folderId = contentWs.getIdByPath(folderPath);
		if (folderId == null)
			throw new RuntimeException("Cannot find folder path: \"" + folderPath + "\"");
		out.println("\nFolder path (" + folderId.getUUID() + ") is: " + folderPath.trim());
		
		PSLocator[] folderIds = getDescedentFolderIds(folderId);
		setFolderPermissions(folderIds, communityId, virtualPerm, pmap, out);
		
		out.println("</pre>");
	   			   
     } 
     catch (Exception ex)
     {
        handleException(ex, out);
     }
  }
%>
      
<%! 
	private int getPermission(String access)
	{
		access = access.toLowerCase();
		if ("admin".equalsIgnoreCase(access))
			return ADMIN_ACCESS;
		else if ("write".equalsIgnoreCase(access))
			return WRITE_ACCESS;
		else if ("read".equalsIgnoreCase(access))
			return READ_ACCESS;
		
		throw new RuntimeException("Unknown permission: '" + access + "'");
	}

    private void outputFolderPermission(PSLocator[] folderIds, String virtualPerm, 
	  Map rolePerms, JspWriter out) throws java.io.IOException
	{
		if (StringUtils.isNotBlank(virtualPerm))
			out.println("\nVirtual Permission: " + virtualPerm);

		out.println("\nRole Permissions: ");
        Set entrySet = rolePerms.entrySet();
        Iterator it = entrySet.iterator();
        while (it.hasNext())
        {
        	Map.Entry entry = (Map.Entry) it.next();
        	out.println("   " + (String)entry.getKey() + ", " + (String)entry.getValue());
        } 
        
		out.println("\nFolder IDs: ");
		for (int i=0; i < folderIds.length; i++)
		{
		   PSLocator id = folderIds[i];
		   out.println("   Folder ID: " + id.getId());
		}
	}

   public PSServerFolderProcessor getFolderProcessor()
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      return new PSServerFolderProcessor(req, null);
   }

   public PSLocator[] getDescedentFolderIds(IPSGuid parentId) throws PSCmsException
   {
		PSLocator loc = ((PSLegacyGuid)parentId).getLocator();
       	PSLocator[] ids = folderProcessor.getDescendentFolderLocatorsWithoutFilter(loc);
       	List result = new ArrayList();
       	result.add(loc);
       	result.addAll(Arrays.asList(ids));
       	PSLocator[] resultArray = new PSLocator[result.size()];
       	return (PSLocator[]) result.toArray(resultArray);
   }
    
   private String[] expandParam(String[] inParam, int size)
   {
      int ilen = 0;
      if (inParam != null)
      {
         ilen = inParam.length;
      }
      if (ilen >= size)
      {
         return inParam;
      }
      String[] outParam = new String[size];
      java.util.Arrays.fill(outParam, "");
      if (ilen > 0)
      {
         System.arraycopy(inParam, 0, outParam, 0, ilen);
      }
      return outParam;
   }
   
   private void handleException(Exception ex, javax.servlet.jsp.JspWriter out)
      throws java.io.IOException
   {
      out.println("<pre> Unexpected Exception \n");
      out.println(ex.toString());
      StackTraceElement[] stacktrace = ex.getStackTrace();
      for (int m = 0; m < stacktrace.length; m++)
      {
         out.println(stacktrace[m].toString());
      }
      out.println("</pre>");                  
   }
   
   private Map buildParamMap(String[] allNames, String[] allValues,
         javax.servlet.jsp.JspWriter out)
         throws java.io.IOException
   {
      Map pmap = new HashMap();
      for (int i = 0; i < allNames.length; i++)
      {
         String pNameLoop = allNames[i];
         String pValLoop = allValues[i];
         if (pNameLoop.trim().length() > 0 && pValLoop.trim().length() > 0)
         {
            out.println(pNameLoop + " " + pValLoop);
            pmap.put(pNameLoop, pValLoop);
         }
      }
      return pmap;
   }
   private void printTableHeader(String[] columns,
         javax.servlet.jsp.JspWriter out)
         throws java.io.IOException
   {
      out.println("<thead><tr>");
      for (int j = 0; j < columns.length; j++)
      {
         out.println("<th>" + columns[j] + "</th>");
      }
      out.println("</tr></thead>");
   }
   private void printTableRows(QueryResult qresults,
         javax.servlet.jsp.JspWriter out)
         throws java.io.IOException, javax.jcr.RepositoryException
   {
      RowIterator rows = qresults.getRows();
      while (rows.hasNext())
      {
         out.println("<tr>");
         Row nrow = rows.nextRow();
         Value[] nvalues = nrow.getValues();
         for (int k = 0; k < nvalues.length; k++)
         {
            String kval = "&nbsp;";
            if (nvalues[k] != null)
            {
               kval = nvalues[k].getString();
            }
            out.println("<td>" + kval + "</td>");
         }
         out.println("</tr>");
      }
   }
   %>
</p>


</body>
</html>
