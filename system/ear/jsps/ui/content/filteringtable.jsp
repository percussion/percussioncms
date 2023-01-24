<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>


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
