<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
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
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    String site = request.getParameter("site");
    if (debug == null)
        debug = "false";
%>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%=debug%>"/>
<div id = 'perc-editor-filter-wrapper'>
    <span class="perc-required-label">
        <label>
            <i18n:message key ="perc.ui.publish.servers@Required"/>
        </label>
    </span>
    <div>
        <label>
            <span class='perc-required-field'></span><i18n:message key ="perc.ui.publish.servers@Name"/>:
        </label>
        <input percName = 'serverName' type = 'text' name = 'serverName'>
    </div>
    <div id = 'perc-editor-filter'>
        <div id = 'perc-type-filter' name = 'type'>
            <label>
                <span class='perc-required-field'></span><i18n:message key ="perc.ui.publish.servers@Type"/>:
            </label>
            <select name = 'publishType' id = 'publishType' class = 'type'>
                <option>Select</option>
                <option>File</option>
                <option>Database</option>
            </select>
        </div>
        <div id = 'perc-driver-filter' name = 'driver'>
            <label>
                <span class='perc-required-field'></span><i18n:message key ="perc.ui.publish.servers@Driver"/>:
            </label>
            <select id = 'perc-driver' disabled = true>
                <option>Select</option>
                <option name = 'FTP' class = 'file-driver'>FTP</option>
                <option name = 'LOCAL' class = 'file-driver'>Local</option>
                <option name = 'AMAZON S3' class = 'file-driver'>Amazon S3</option>
                <option name = 'MSSQL' class = 'database-driver'>MSSQL</option>
                <option name = 'ORACLE' class = 'database-driver'>Oracle</option>
                <option name = 'MYSQL' class = 'database-driver'>MySQL</option>
            </select>
        </div>
    </div>
</div>
<div style ='clear:both'>
</div>
<div id = 'perc-editor-wrapper'>
</div>
<div id = 'perc-filter-actions'>
    <div id = 'perc-pub-now-wrapper' style ='float:left'>
        <input percName = 'isDefault' id ='perc-pub-now' type ='checkbox' ><i18n:message key ="perc.ui.publish.servers@Set as Publish Now Server"/></input>
    </div>
    <div id = 'perc-ignore-assets-wrapper' style ='float:left'>
        <input percName = 'ignoreUnModifiedAssets' id ='perc-ignore-unmodified-assets' type ='checkbox' ><i18n:message key ="perc.ui.publish.servers@Ignore Unmodified Assets"/></input>
    </div> 
    <div id = 'perc-publish-related-wrapper' style ='float:left'>
        <input percName = 'publishRelatedItems' id ='perc-publish-related-items' type ='checkbox' ><i18n:message key ="perc.ui.publish.servers@Publish Related Items"/></input>
    </div> 
 <button id="perc-define-save" class="btn btn-primary" name="perc_wizard_save"><i18n:message key ="perc.ui.button@Save"/></button>
 <button id="perc-define-cancel" class="btn btn-primary" type="button" name="perc_wizard_cancel"><i18n:message key ="perc.ui.common.label@Cancel"/></button>
</div>
