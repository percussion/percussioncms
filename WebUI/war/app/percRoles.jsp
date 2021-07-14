<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

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
    String status = request.getParameter("status");
    String msgClass = null;
    if(status != null && status.equals("PERC_SUCCESS"))
        msgClass = "perc-success";
    else if(status != null && status.equals("PERC_ERROR"))
        msgClass = "perc-error";
    String message = request.getParameter("message");
%>

<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<div id="perc-roles-menu" style="height:54px;"> 
</div>
<div id="perc-pageEditor-roles-toolbar-content" class="ui-helper-clearfix"> </div> 
<div class='perc-whitebg' style="overflow : auto">
    <div id = "perc-roles-wrapper" style="width:1024px">                
        <div id = "perc-roles-list">
        </div>
        <div id="perc-roles-details">
            <div id="perc-roles-info">
                <div id="perc-roles-edit-role-button" title="<i18n:message key = "perc.ui.users@Edit Role Details"/>"></div>
                <span class="perc-required-label" style="display:none;"><label><i18n:message key = "perc.ui.general@Denotes Required Field"/></label></span> 
                <div id="perc-roles-name-label"><i18n:message key = "perc.ui.workflow@Name"/><br />
                    <input id="perc-orig-roles-name-field" type="hidden" />
                    <input id="perc-roles-name-field" class="perc-roles-name-edit-field" maxlength="50" />
                </div><br />
                <div id="perc-roles-description-label"><i18n:message key = "perc.ui.roles@Description"/><br />
                    <textarea id="perc-roles-description-field" maxlength="255" style="overflow:auto" ></textarea>
                </div><br />
                <div id="perc-roles-homepage-label"><i18n:message key = "perc.ui.roles@Homepage"/><br />
                    <select id="perc-roles-homepage-field">
                        <option value="Dashboard"><i18n:message key = "perc.ui.navMenu.dashboard@Dashboard"/></option>
                        <option value="Editor"><i18n:message key = "perc.ui.navMenu.webmgt@Editor"/></option>
                        <option value="Home"><i18n:message key = "perc.ui.navMenu.home@Home"/></option>
                    </select>
                    <div id="perc-roles-homepage-field-readonly" class="perc-roles-field-readonly"></div>
                </div>
                <div id="perc-role-save-cancel-block" style="width:100%; height:50px;"> 
                    <button id="perc-roles-save"   class="btn btn-primary" name="perc_wizard_save"><i18n:message key ="perc.ui.button@Save"/></button>
                    <button id="perc-roles-cancel" class="btn btn-primary" name="perc_wizard_cancel"><i18n:message key ="perc.ui.common.label@Cancel"/></button>
                </div>
            </div>
            <div           id="perc-roles-users-editor">
                <div       id="perc-roles-assigned-users">
                    <div    id="perc-roles-assigned-users-label"><i18n:message key = "perc.ui.roles@Users"/>
                        <div class="perc-roles-removeusers-button perc-item-disabled" title="Remove users from role"></div>
                        <div class="perc-roles-addusers-button" title="<i18n:message key = "perc.ui.users@Add Users To Role"/>"></div>
                    </div>
                    <div class="perc-roles-assigned-users-list"></div>
                </div>
            </div>  
        </div>
    </div>    
</div>
