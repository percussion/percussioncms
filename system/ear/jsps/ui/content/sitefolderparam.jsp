<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
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
