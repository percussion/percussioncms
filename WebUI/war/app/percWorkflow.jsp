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
    if (debug == null)
        debug = "false";
%>        
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>

<div id="perc-workflow-menu"> 
</div>
<div id="perc-pageEditor-workflow-toolbar-content" class="ui-helper-clearfix"> </div> 
<div class="perc-whitebg" style="overflow: auto" >
    <div id="perc-workflow-wrapper">
        <table>
            <tr>
                <td style = "vertical-align:top; border-right:2px solid #E6E6E9">
                    <div id="perc-workflows-list">
                    </div>
                    <div id="perc-workflows-assigned-sites-folders">
                    </div>
                </td>
                <td>           
                    <div id="perc-workflow-steps-container">
                    
                    <!-- New Workflow Editor -->
                        <div  id="perc-wf-new-editor" class="perc-wf-editor" style="display: none;">
                            <span class="perc-required-label"><label><i18n:message key = "perc.ui.general@Denotes Required Field"/></label></span>
                             <div id="perc-new-wf-name-label" class = "perc-required-field"><label class="perc-name-label"><i18n:message key = "perc.ui.workflow@Name"/><br></label>
                                <input tabindex="0" maxlength = '50' required pattern='^[a-zA-Z0-9-_\. ]{1,50}$' title='1 to 50 letters,numbers,-, _, or space' id="perc-new-workflow-name">
                            </div>
							<div class = "perc-wf-default"><input tabindex="0" aria-required='true' title='<i18n:message key = "perc.ui.workflow@Make Default"/>' type="checkbox" id="perc-wf-default-new-checkbox" /> <label for ="perc-wf-default-new-checkbox"><i18n:message key = "perc.ui.workflow@Make Default"/></label></div>
                            <!-- control gets generated here in percWorkflowView -->
                           <div id="perc-publish-now-roles-control-new" style="display:block; width:340px;"></div>
                            <div style="width: 100%; height: 50px;" id="perc-new-wf-save-cancel-block"> 
                                <button tabindex="0" title='<i18n:message key ="perc.ui.button@Save"/>' id="perc-wf-save" class="btn btn-primary" name="perc_wizard_save"><i18n:message key ="perc.ui.button@Save"/></button>
                                <button tabindex="0" title='<i18n:message key ="perc.ui.common.label@Cancel"/>' id="perc-wf-cancel" class="btn btn-primary" name="perc_wizard_cancel"><i18n:message key ="perc.ui.common.label@Cancel"/></button>
                            </div>
                        </div>   

                    <!-- Update Workflow Editor -->
                        <div  id="perc-wf-update-editor"  class="perc-wf-editor" style="display: none;">
                            <span class="perc-required-label"><label><i18n:message key = "perc.ui.general@Denotes Required Field"/></label></span>
                             <div id="perc-update-wf-name-label" class = "perc-required-field" ><label class="perc-name-label"><i18n:message key = "perc.ui.workflow@Name"/><br></label>
                                <input maxlength = '50' aria-required='true' required pattern='^[a-zA-Z0-9-_\. ]{1,50}$' title='1 to 50 letters,numbers,-, _, or space' id="perc-update-workflow-name">
                            </div>
							 <div class = "perc-wf-default"><input tabindex="0" title='<i18n:message key = "perc.ui.workflow@Make Default"/>' type = "checkbox" id="perc-wf-default-update-checkbox" /> <label for="perc-wf-default-update-checkbox"><i18n:message key = "perc.ui.workflow@Make Default"/></label></div>
                             <!-- control gets generated here in percWorkflowView -->
                             <div id="perc-publish-now-roles-control" style="display:block; width:340px;"></div>
                            <div style="width: 100%; height: 50px; " id="perc-update-wf-save-cancel-block"> 
                                <button tabindex="0" title='<i18n:message key ="perc.ui.button@Save"/>' name="perc_wizard_save" class="btn btn-primary" id="perc-wf-update-save"><i18n:message key ="perc.ui.button@Save"/></button>
                                <button tabindex="0" title='<i18n:message key ="perc.ui.common.label@Cancel"/>' name="perc_wizard_cancel" class="btn btn-primary" id="perc-wf-update-cancel"><i18n:message key ="perc.ui.common.label@Cancel"/></button>
                            </div>
                        </div> 
                        
                    <!-- Workflow Name and Edit Button -->
                        <div id="perc-wf-name-wrapper">
                        <div class = "perc-wf-action-wrapper">
                            <div class = 'perc-default-wf-marker'><i18n:message key = "perc.ui.workflow@Default"/></div>
                            <div role="button" tabindex="0" title="<i18n:message key = "perc.ui.workflow@Edit Workflow Details"/>" id="perc-wf-edit"></div>
                        </div>     
                            <div id="perc-workflow-name"></div>
                        </div>
                        
                     <!-- Workflow Step container -->   
                    
                            <table id="perc-workflow-table"> </table>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>               

