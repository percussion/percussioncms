<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
    String debug = request.getParameter("debug");     
    if(debug == null)
        debug = "false";
    String site = request.getParameter("site");
    if (site == null)
        site = "";
    String openedObject = request.getParameter("openedObject");
    boolean showPage = "PERC_PAGE".equals(openedObject);
    String finderMode = request.getParameter("finderMode");
    boolean isLibraryMode = "library".equals(finderMode);
    String view = request.getParameter("view");
    String className = "perc-ui-component-ready";
    if(view.equals("editor")){
    	className = "perc-ui-component-processing";
    }
    String locale= PSRoleUtilities.getUserCurrentLocale();
    String lang="en";
	if(locale==null){
    	locale="en-us";
    }else{
    	if(locale.contains("-"))
    		lang=locale.split("-")[0];
    	else
    		lang=locale;
    }
 %>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<div class='perc-finder-outer <%=className%>' perc-ui-component="perc-ui-component-finder" collapsed="true">
    <table class="perc-finder-header">
        <tr>
            <td class="perc-finder-view-options" <%if(isLibraryMode){%>style="display:none"<%}%>>
                <a id='perc-finder-expander' class="perc-font-icon icon-plus-sign ui-state-enabled"></a>
                <a id="perc-finder-choose-columnview" class="perc-font-icon icon-columns ui-active" title="<i18n:message key="perc.ui.finder@Column View"/>"></a>
                <a id="perc-finder-choose-listview" class="perc-font-icon icon-list" title="<i18n:message key= "perc.ui.finder@List View"/>"></a>
                <a id="perc-finder-choose-mypagesview" class="perc-font-icon icon-star" title="<i18n:message key ="perc.ui.finder@My Pages"/>"></a>
            </td>
            <td class="perc-finder-goto-or-search">
                <div class="perc-wrapper <%if(!isLibraryMode){%>perc-wrapper-bg<%}%>">
                    <table>
                        <tr>
                            <td class="perc-flex">
                                <input id="mcol-path-summary" class="perc-finder-goto-or-search" type="text" aria-label="Enter Path or Search"/>
                            </td>
                            <td class="perc-fixed">
                                <%if(!isLibraryMode){%>
	                                <a class="perc-action perc-action-goto-or-search perc-font-icon icon-search" href="#" role="button" title="Click to Search"></a>
	                                <div class="perc-hide" style="display:none;" aria-hidden="true">
	                                    <!-- using this old ui behind the scenes to minimize code changes -->
	                                    <a id="perc-finder-go-action" href="#"></a>
	                                    <a id="perc-finder-search-submit" href="#"></a>
	                                    <input id="perc-finder-item-search" type="text"/>
	                                </div>
                                <%}%>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
            <td class="perc-finder-menu"><!-- finder actions buttons will magically show up here! --></td>
        </tr>
    </table>
    <div class="perc-finder-message" style="display: none;" <%if(isLibraryMode){%>style="width:150px"<%}%>>
        <!-- finder messages will be appended to this div -->
    </div>
    <div class="perc-finder-body perc-resize">
        <div class="perc-finder perc-resize-height">
            <!-- path explorer is appended to this div -->
        </div>
        <!-- list-view paging toolbar is appended here -->
    </div>
</div>

