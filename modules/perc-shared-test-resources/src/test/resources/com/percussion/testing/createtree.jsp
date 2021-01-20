<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" 
    import="com.percussion.testing.PSFolderTreeCreator"
  	 import="com.percussion.testing.PSContentHelper"
    import="org.apache.commons.lang.*"
    %>
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
	String errors = null;
	String submit = request.getParameter("start");
	String source = request.getParameter("source");
	String destination = request.getParameter("destination");
	String count = request.getParameter("count");
	String depth = request.getParameter("depth");
	String folderDist = request.getParameter("folderDist");
	String contentPortion = request.getParameter("contentPortion");
	
	if (submit != null && submit.equals("start"))
	{
	   try
	   {
	      int icount = Integer.parseInt(count);
	      int idepth = Integer.parseInt(depth);
	      float scontentPortion = Float.parseFloat(contentPortion);
	      float sfolderDist = Float.parseFloat(folderDist);

	      PSFolderTreeCreator creator = new PSFolderTreeCreator();
	      creator.createTree(source, destination, icount, idepth, 
	            scontentPortion, sfolderDist);
	   }
	   catch(Exception e)
	   {
	      errors = e.getLocalizedMessage();
	   }
	}
	else
	{
	   if (StringUtils.isBlank(contentPortion))  
	   {
	      contentPortion = "1";
	   }
	   if (StringUtils.isBlank(depth))  
	   {
	      depth = "1";
	   }
	   if (StringUtils.isBlank(count))  
	   {
	      count = "100";
	   }	   
	   if (StringUtils.isBlank(folderDist))  
	   {
	      folderDist = ".3";
	   }	
	   if (StringUtils.isBlank(source))
	   {
	      source = "";
	   }
	   if (StringUtils.isBlank(destination))
	   {
	      destination = "";
	   }  
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Setup folders, items and navigation for testing purposes</title>
<style type="text/css">
	body { font-family: sans-serif }
	.in tr td p {font-size: smaller; margin-top: 3px }
</style>
</head>
<body>
	<h3>Use this page to create a random folder hierarchy populated by content for 
testing purposes</h3>
	<%
		if (errors != null) {
   %>
   	<blockquote style="color: red">Errors: <%= errors %></blockquote>
   <%
		}
	%>
<form method="post">
<table class="in" cellpadding="0" cellspacing="0">
	<!-- MSTableType="layout" -->
	<tr>
		<td valign="top">Source path</td>
		<td valign="top">
		<input type="text" name="source" size="80" value="<%= source %>"><p>This source path is 
		used to get the properties and permissions for the created folders, and 
		it provides the content items to clone to populate the tree. The source 
		path should be a folder that only creates content and no subfolders.</td>
	</tr>
	<tr>
		<td valign="top">Destination path</td>
		<td valign="top">
		<input type="text" name="destination" size="80" value="<%= destination %>"><p>The folders are 
		created at this location as the root folder.</td>
	</tr>
	<tr>
		<td valign="top">Folder count</td>
		<td valign="top">
		<input type="text" name="count" size="6" value="<%= count %>"><p>The count of 
		folders to create, must be a number &gt; 0</td>
	</tr>
	<tr>
		<td valign="top">Max depth</td>
		<td valign="top">
		<input type="text" name="depth" size="6" value="<%= depth %>"><p>The maximum depth 
		of the folder tree to create, must be &gt;= 1</td>
	</tr>
	<tr>
		<td width="127" valign="top">Folder dist</td>
		<td width="908" valign="top">
		<input type="text" name="folderDist" size="6" value="<%= folderDist %>"><p>A factor 
		used to determine what proportion of the folders to be created should be 
		created at each level. The minimum number of folders created at each 
		level is 1. A larger factor means more will be created at each folder 
		level.</td>
	</tr>
	<tr>
		<td width="127" valign="top">Content dist</td>
		<td width="908" valign="top">
		<input type="text" name="contentPortion" size="6" value="<%= contentPortion %>"><p>What percentage of the source folder&amp;s content items should be cloned into each destination folder.</td>
	</tr>
	<tr>
		<td width="127" valign="top">&nbsp;</td>
		<td width="908" valign="top" height="19">
		<input type="submit" name="start" value="start" label="Create" /> 
		<p>When pressed, start 
		creating the folder tree and populating it. Please be aware that this 
		can take some time, and that you'll need to refresh to CX to see the 
		created folders and items.</td>
	</tr>
</table>
</form>
</body>
</html>