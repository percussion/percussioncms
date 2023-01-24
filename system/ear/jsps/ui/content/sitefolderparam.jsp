<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>


<%-- Shows sites and folders checkboxes. Takes 3 mandatory parameters:
* idPrefix - controls prefix.
* includeSitesLabel - text for the "includeSite" checkbox.
* includeFoldersLabel - text for the "includeFolder" checkbox.
--%>
<%
final String idPrefix = request.getParameter("idPrefix");
assert idPrefix != null;
final String includeSitesLabel = request.getParameter("includeSitesLabel");
assert includeSitesLabel != null;
final String includeFoldersLabel = request.getParameter("includeFoldersLabel");
assert includeFoldersLabel != null;
%>
<div id="<%= idPrefix %>siteAndFolderFilterDiv">
   <table cellpadding="0" cellspacing="0" border="0">
      <tr>
         <td>&nbsp;</td>
         <td>
            <input type="checkbox" value="includeSite" name="includeSite" id="<%= idPrefix %>includeSitesCheckbox"/>
            <%-- text defined on the client --%>
            <span id="<%= idPrefix %>includeSitesCheckboxLabel"><%= includeSitesLabel %></span><br/>
         </td>
         <td>&nbsp;</td>
         <td>
            <input type="checkbox" value="includeFolder" name="includeFolder" id="<%= idPrefix %>includeFoldersCheckbox"/>
            <%-- text defined on the client --%>
            <span id="<%= idPrefix %>includeFoldersCheckboxLabel"><%= includeFoldersLabel %></span>
         </td>   
      </tr>
   </table>
</div>
