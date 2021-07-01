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
    String debug = request.getParameter("debug");
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    String site = request.getParameter("site");
    if (debug == null)
        debug = "false";
%>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%=debug%>"/>

<div id="ui-layout-west">
    <div>
        <div class='perc-template-container'>
            <div id="perc-styleTabs">
                <ul>
                    <div id = "perc-dropdown-actions-style" style="float:left;"></div>
                    <div id = "perc-dropdown-view-style" style="float:left;"></div>
                    <li ><a class="perc-style-sub-tab" href="#perc-styleTabs-1"><i18n:message key = "perc.ui.template.layout@Select Theme"/></a></li>
                    <li ><a class="perc-style-sub-tab" href="#perc-styleTabs-2"><i18n:message key = "perc.ui.template.layout@View Theme CSS"/></a></li>
                    <li ><a class="perc-style-sub-tab" href="#perc-styleTabs-3"><i18n:message key = "perc.ui.template.layout@Override Theme CSS"/></a></li>
                    <div style="text-align: right; float : right" class="ui-layout-east">
                        <button id="perc-css-editor-save"   class="btn btn-primary" name="perc_wizard_save"   ><i18n:message key ="perc.ui.button@Save"/></button>
                        <button id="perc-css-editor-cancel" class="btn btn-primary" name="perc_wizard_cancel" ><i18n:message key ="perc.ui.common.label@Cancel"/></button>
                    </div>
                </ul>
                <div id="perc-styleTabs-1">
                    <div id="perc-css-gallery">
                    </div>
                </div>
                <div id="perc-styleTabs-2">
                    <div id="perc-css-theme-editor">
                    </div>
                </div>
                <div id="perc-styleTabs-3">
                    <div id="perc-css-editor">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- <iframe id="css_preview" ></iframe> -->
