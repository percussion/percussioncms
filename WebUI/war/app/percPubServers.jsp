<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>




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
