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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
   String locale = PSI18nUtils.getSystemLanguage();
%>
<%
   String idPrefix = "ps.content.searchpanel.";
%>
<div dojoType="ps:PSSplitContainer" layoutAlign="client" orientation="vertical" sizerWidth="1"
   activeSizing="true" persist="false" sizerVisible="true" sizerEnabled="false" style="border: 0px solid black; width: 100%; height: 100%;"
   id="<%= idPrefix %>mainsplitpane">
    <div dojoType="ps:PSSplitContainer" layoutAlign="client" orientation="vertical" sizerWidth="1"
           activeSizing="true" persist="false" style="border: 0px solid black; width: 100%; height: 100%;"
           id="<%= idPrefix %>contentsplitpane" sizeMin="200" sizeShare="95">
	   <div dojoType="ContentPane" style="background: #ffffff;overflow:auto;" id="<%= idPrefix %>tablepanel" sizeMin="100" sizeShare="85">
			<jsp:include page="filteringtable.jsp" flush="true">
			   <jsp:param name="idPrefix" value="<%= idPrefix %>" />
			</jsp:include>
	   </div>
	   <div dojoType="ContentPane" style="background: #ffffff;overflow:auto;" id="<%= idPrefix %>searchformpanel" sizeMin="100" sizeShare="85"
	         preload="true" cacheContent="false">
		   ${rxcomp:i18ntext('jsp_searchpanel@loading',locale)}
	   </div>
	   <div dojoType="ContentPane" style="padding: 10px; overflow: hidden;" id="<%= idPrefix %>filterpanel" sizeMin="30" sizeShare="10">
			<jsp:include page="filterpanel.jsp" flush="true">
			   <jsp:param name="idPrefix" value="<%= idPrefix %>" />
			</jsp:include>
         <%-- the include site/folder checkboxes for the search parameters form --%>
			<jsp:include page="sitefolderparam.jsp" flush="true">
			   <jsp:param name="idPrefix" value="<%= idPrefix %>" />
			   <jsp:param name="includeSitesLabel" value="Show sites" />
			   <jsp:param name="includeFoldersLabel" value="Show folders" />
			</jsp:include>
	   </div>
   </div>
   <div dojoType="ContentPane" style="padding:10px; overflow: hidden;" sizeMin="35" sizeShare="6" id="<%= idPrefix %>commandpanel">
		<jsp:include page="commandpanel.jsp" flush="true">
		   <jsp:param name="idPrefix" value="<%= idPrefix %>" />
		</jsp:include>
  </div> 
</div>
