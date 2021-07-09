<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n"%>

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

<div class="ui-helper-clearfix" id="perc-pageEditor-publish-servers-toolbar-content" style="z-index: 4360;">
</div>
<div class='perc-whitebg' style="overflow : auto">
    <div id = 'perc-servers-wrapper'>
        <table>
            <tr>
                <td style ="vertical-align:top; border-right:2px solid #E6E6E9">
                    <div id="perc-servers-list" style="z-index: 4430;">
                    </div>
                </td>
                <td>
                    <div id="perc-server-name-wrapper" >
                        <div id="perc-server-edit" title="Edit Server details" style="z-index: 4220;">
                        </div>
                        <div id="perc-server-name">
                        </div>
                    </div>
                    <div id = 'perc-servers-container'>
                        <div style ='clear:both'>
                        </div>
                        <div id = 'perc-editor-summary'>

                        </div>
                        <div id = 'perc-editor-form'>
                        </div>
                    </div>
                    <label class="perc-warning-message" ><i18n:message key = "perc.ui.publish.servers@Restart CM1"/></label>
                </td>
            </tr>
        </table>
    </div>
</div>
