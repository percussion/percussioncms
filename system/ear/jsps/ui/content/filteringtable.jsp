<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
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
   String locale = PSI18nUtils.getSystemLanguage();

   String idPrefix = request.getParameter("idPrefix");
%>
<style type="text/css">
		table.ps_content_browse_viewtable {
			font-family:Lucida Grande, Verdana;
			font-size:0.8em;
			width:100%;
			border:0px solid #ccc;
			border-collapse:collapse;
			cursor:default;
		}
		table.ps_content_browse_viewtable td,
		table.ps_content_browse_viewtable th{
			padding:2px;
			font-weight:normal;
		}
		table.ps_content_browse_viewtable thead td, table.ps_content_browse_viewtable thead th {
			background-image:url(/Rhythmyx/sys_resources/ps/content/images/ft-head.gif);
			background-repeat:no-repeat;
			background-position:top right;
		}
		table.ps_content_browse_viewtable thead td.selectedUp, table.ps_content_browse_viewtable thead th.selectedUp {
			background-image:url(/Rhythmyx/sys_resources/ps/content/images/ft-headup.gif);
		}
		table.ps_content_browse_viewtable thead td.selectedDown, table.ps_content_browse_viewtable thead th.selectedDown {
			background-image:url(/Rhythmyx/sys_resources/ps/content/images/ft-headdown.gif);
		}
			
		table.ps_content_browse_viewtable tbody tr td{
			border-bottom:0px solid #ddd;
		}
		table.ps_content_browse_viewtable tbody tr.alt td{
			background: #ffffdd;
		}
		table.ps_content_browse_viewtable tbody tr.selected td{
			background: #ddffff;
		}
		table.ps_content_browse_viewtable tbody tr:hover td{
		}
		table.ps_content_browse_viewtable tbody tr.selected:hover td{
		}
	</style>

<table class="ps_content_browse_viewtable" dojoType="filteringTable" id="<%= idPrefix %>FilteringTable" multiple="false" 
   alternateRows="true" maxSortable="1" cellpadding="0" cellspacing="0" border="0">
<thead>
	<tr>
		<th field="Name" dataType="String" align="left" valign="top"
				sortusing="ps.content.BrowseTabPanel.compareIgnoreCase">
			${rxcomp:i18ntext('jsp_filteringtable@name',locale)}
		</th>
		<th field="Description" dataType="String" align="left" valign="top"
				sortusing="ps.content.BrowseTabPanel.compareIgnoreCase">
			${rxcomp:i18ntext('jsp_filteringtable@desc',locale)}
		</th>
	</tr>
</thead>
</table>
