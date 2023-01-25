<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="java.util.Map, java.util.Set, java.util.Collections, java.util.Map.Entry, java.util.Iterator,
java.util.HashMap, java.util.Arrays, java.util.ArrayList, java.util.List"
         import="com.percussion.services.filestorage.extensions.PSFileStorageTools"
         import="com.percussion.services.filestorage.PSFileStorageServiceLocator,com.percussion.services.filestorage.IPSFileStorageService"
         import="com.percussion.services.filestorage.IPSFileMeta"
%>


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
