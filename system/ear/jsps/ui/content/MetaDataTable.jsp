<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="java.util.Map, java.util.Set, java.util.Collections, java.util.Map.Entry, java.util.Iterator,
java.util.HashMap, java.util.Arrays, java.util.ArrayList, java.util.List"
         import="com.percussion.services.filestorage.extensions.PSFileStorageTools"
         import="com.percussion.services.filestorage.PSFileStorageServiceLocator,com.percussion.services.filestorage.IPSFileStorageService"
         import="com.percussion.services.filestorage.IPSFileMeta"
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

<html><head><title>Binary Metadata</title></head><body>
<%
    String hash=request.getParameter("hash");
    IPSFileStorageService fss = PSFileStorageServiceLocator.getFileStorageService();
    IPSFileMeta meta =fss.getMeta(hash);
    Iterator itt = meta.keySet().iterator();
%> <TABLE  BORDER="5"  WIDTH="50%"  CELLPADDING="4" CELLSPACING="3"> <tr><TH COLSPAN="2"><BR><H3>Meta Data</H3>
</TH></tr><TR>
    <TH>Name</TH>
    <TH>Value</TH>
</TR><%
    while (itt.hasNext()) {
        Object o = itt.next();
        String name = (String)o;
        String value = meta.get(name);
%>
    <tr><td><%=name %></td><td><%=value %></td></tr>
    <%
        }
    %></table>


</body></html>