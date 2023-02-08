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

    <div class="ui-helper-clearfix" id="perc-pageEditor-publish-reports-toolbar-content" style="z-index: 4360;"> </div> 
    <div class='perc-whitebg' style="overflow : auto">    
       <!--      Publishing job status      -->
        <div class='perc-publish-section' id='perc-publish-jobs-widget'>
            <span class='perc-foldable'> 
                <a href="#" style="float: left;" class='perc-maximizer'></a>
                <span style="float: left;"><i18n:message key = "perc.ui.publish.reports@Status of Current Jobs"/></span> 
            </span>
            <div class='perc-container perc-hidden'>
                <div id="perc-publish-current-jobs" style="height: 100%"></div>
            </div>
        </div>
        
        <!--     Publishing job logs     -->
        <div class='perc-publish-section' id='perc-publish-logs-widget'>
            <span class='perc-foldable'>
                <a href="#" style="float: left;" class='perc-maximizer'></a>
                <span style="float: left;"><i18n:message key = "perc.ui.publish.reports@Publishing Log"/></span> 
            </span>
            <div class='perc-container perc-hidden'>
                <i18n:message key = "perc.ui.publish.reports@View Last"/>
                <select id='perc-view-last' onChange='publishLogs();'>
                    <option value='3'><i18n:message key = "perc.ui.publish.reports@3 Days"/></option>
                    <option value='5' selected="selected"><i18n:message key = "perc.ui.publish.reports@5 Days"/></option>
                    <option value='10'><i18n:message key = "perc.ui.publish.reports@10 Days"/></option>
                </select>
                <i18n:message key = "perc.ui.publish.reports@Server Tag"/>
                <select id='perc-servers' onChange='publishLogs();'>
                </select>
                <i18n:message key = "perc.ui.publish.reports@Show Tag"/>
                <select id='perc-show' onChange='publishLogs();'>
                    <option value='20'><i18n:message key = "perc.ui.publish.reports@20 items"/></option>
                    <option value='30' selected="selected"><i18n:message key = "perc.ui.publish.reports@30 items"/></option>
                    <option value='50'><i18n:message key = "perc.ui.publish.reports@50 items"/></option>
                </select>
                <button id='perc-publish-log-delete'
                    class='btn btn-primary perc-disabled'
                    style='position: relative; top: -5px; float: right;'>
                    <i18n:message key = "perc.ui.publish.reports@Delete"/>
                </button>
                <div id="perc-publish-logs" style="margin-top: 20px;"></div>
            </div>
        </div>
        
        <!--     Publishing job log in details     -->
        <div class='perc-publish-section  perc-hidden' id='perc-publish-log-details-widget'>
            <span class='perc-foldable'>
                <a href="#" style="float: left;" class='perc-maximizer'></a>
                <span style="float: left;"><i18n:message key = "perc.ui.publish.reports@Pub Log Details"/></span>
                <button 
                    class="btn btn-primary perc-back" id="perc-publish-back" ><i18n:message key = "perc.ui.publish.reports@Back"/></button>
            </span>
            <div class='perc-container perc-hidden'>
                <div id="perc-publish-logs-details" ></div>
            </div>
        </div>
    </div>
