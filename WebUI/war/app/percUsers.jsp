<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ page import=" com.percussion.utils.PSSpringBeanProvider" %>
<%@ page import="com.percussion.utils.service.impl.PSUtilityService" %>


<i18n:settings lang='<%= request.getParameter("lang") %>' prefixes="perc.ui." debug='<%= request.getParameter("debug") %>' />

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

    PSUtilityService utilityService = (PSUtilityService) PSSpringBeanProvider.getBean("utilityService");
    boolean isSaaS = utilityService.isSaaSEnvironment();
%>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<style>
    .ui-dialog-titlebar-close{
        display:none;
    }

    .paddingBetweenCols td
    {
        padding:0 10px;
    }
    #perc-users-import-users-dialog-fixed {
        padding : 4px;
    }
    #perc-users-import-users-dialog-fixed div.ui-dialog-content {
        padding : 10px;
        padding-bottom : 75px;
    }

    #perc-users-import-users-dialog-fixed input {
        width : 10px;
    }

    .ui-dialog .ui-dialog-title {
        float:left;
        color:#666666;
        font-family:verdana;
        font-size:16px;
    }

    #perc-users-narrow-search {
        top : 108px;
    }

    #perc-button-format{
        padding-top: 10px;
        padding-right: 10px;
    }
</style>


<div id="perc-users-menu" style="height:54px;">
    <div id="perc-button-format">
        <button id="perc-users-cancel" class="btn btn-primary" name="perc_wizard_cancel">
            <i18n:message key="perc.ui.common.label@Cancel" /></button>
        <button id="perc-users-save" class="btn btn-primary" name="perc_wizard_save">
            <i18n:message key="perc.ui.button@Save" /></button>
    </div>
</div>
<div id="perc-pageEditor-users-toolbar-content" class="ui-helper-clearfix"> </div>

<div class='perc-whitebg' style="overflow : auto">
    <div id="perc-users-wrapper" style="width:1024px">
        <div id="perc-users-list">
            <div class="perc-user-list-label">
                Users
                <div role="button" id="perc-users-add-user-button" title="<i18n:message key='perc.ui.users@Add New User' />"></div>
                <div role="button" id="perc-users-import-users-button" title="<i18n:message key='perc.ui.users.import.tooltips@ImportDirectoryUsersTooltip'/>" class="perc-users-import-users-button-enabled"></div>
            </div>
            <div id="perc-username-list">
                <ul>
                </ul>
            </div>
        </div>
        <div id="perc-users-details">

            <csrf:form autocomplete="off" method="post" action="percUsers.jsp">
				<span style="float:left;" class="perc-required-label">
				  <label>
					<i18n:message key="perc.ui.general@Denotes Required Field" />
				  </label>
				</span><br />
                <!--  fake fields are a workaround for chrome/opera autofill getting the wrong fields -->
				<input title="fakeusernameremembered" id="username" style="display:none" type="text" name="fakeusernameremembered">
                <input title="fakepasswordremembered" id="password" style="display:none" type="password" name="fakepasswordremembered">

                <div id="perc-users-info">
                    <div role="button" id="perc-users-edit-user-button" title="<i18n:message key='perc.ui.users@Edit User Details' />"></div>
                    <div id="perc-users-username-label">
                        <i18n:message key="perc.ui.workflow@Name" /><br />
						<input id="perc-users-username-field" maxlength="50" autocomplete="nope" title='<i18n:message key="perc.ui.workflow@Name" />'/>
                    </div><br />
                    <div id="perc-users-external-user-label" style="display : none">
                        <i18n:message key="perc.ui.users.import.label@ThisIsLDAPUser" />.</div>
                    <div id="perc-users-password-block">
                        <div id="perc-users-password-label">
                            <i18n:message key="perc.ui.users@Password" /><br />
							<input id="perc-users-password-field" type="password" value="*******" autocomplete="new-password" title='<i18n:message key="perc.ui.users@Password" />' />
                        </div><br />
                        <div id="perc-users-password-confirm-label">
                            <i18n:message key="perc.ui.users@Confirm Password" /><br />
							<input id="perc-users-password-confirm-field" type="password" value="*******" autocomplete="new-password" title='<i18n:message key="perc.ui.users@Confirm Password" />' />
                        </div><br />
                        <div id="perc-users-email-label">
                            <i18n:message key="perc.ui.users@Email" /><br />
							<input id="perc-users-email-field" maxlength="250" title='<i18n:message key="perc.ui.users@Email" />'/>
                        </div><br />
                    </div>
                    <div id="perc-users-available-roles">
                        <div id="perc-users-available-roles-label"><i18n:message key = "perc.ui.users@Available Roles"/></div>
                        <select size="5"></select>
                    </div>
                    <div id="perc-users-roles-add-remove-buttons">
                        <div role="button" label="move right" title="move right" id="perc-users-add-role-button"></div>
                        <div role="button" label="move left" title="move left" id="perc-users-remove-role-button"></div>
                    </div>
                    <div id="perc-users-assigned-roles">
                        <div id="perc-users-assigned-roles-label" class="perc-required-field">
                            <i18n:message key="perc.ui.users@Assigned Roles" />
                        </div>
                        <select size="5">
                        </select>
                    </div>
                </div>
            </csrf:form>
        </div>
    </div>
</div>

<div id="perc-users-import-users-dialog-fixed" class="perc-dialog" style="z-index:5500;">
    <div class="ui-dialog-content" style="height : 410px; overflow : hidden">
        <div id="perc-users-search">
      <span id="perc-users-search-label">
        <i18n:message key="perc.ui.users.import.dialogs@NameStartsWith" />
      </span><br />
			<input id="perc-users-search-input" style="width : 355px" title='<i18n:message key="perc.ui.users.import.dialogs@NameStartsWith" />'></input>
            <button id="perc-users-search-button" class="btn btn-primary" name="perc-users-search-button">
                <i18n:message key="perc.ui.users.import.dialogs@Search" />
            </button>
            <div id="perc-users-narrow-search" style="position : absolute; display : none"></div>
        </div>
        <!-- header for the table below to select/deselect all users -->
        <br />
        <div style="margin-left : 5px;">
            <table id="perc-users-directory-users-header">
                <tr>
                    <td><input id="perc-users-directory-users-selectall-checkbox" type="checkbox" title='<i18n:message key="perc.ui.users.import.dialogs@SelectAll" />'/>&nbsp;<span id="perc-users-directory-users-selectall-label">
                        <i18n:message key="perc.ui.users.import.dialogs@SelectAll" /></span>
                    </td>
                </tr>
                <tr>
                    <td><span id="perc-users-select-at-least-one-user-label" style="display:none">*
      <i18n:message key="perc.ui.users.import.dialogs@SelectOneUser" /></span>
                    </td>
                </tr>
            </table>
        </div>
        <!-- a scrollable list of users from the directory service created dinamically -->
        <div id="perc-users-directory-users-list">
            <table id="perc-users-directory-users-table" class="paddingBetweenCols">
            </table>
        </div>
        <div id="perc-users-directory-users-buttons">
            <button id="perc-users-directory-users-import-button" class="btn btn-primary" name="perc-users-directory-users-import-button" title='<i18n:message key="perc.ui.users.import.tooltips@SelectUsersToImport"/>'>
                <i18n:message key="perc.ui.users.import.dialogs@Import" />
            </button>
            <button id="perc-users-directory-users-cancel-button" class="btn btn-primary" name="perc-users-directory-users-cancel-button">
                <i18n:message key="perc.ui.users.import.dialogs@Cancel" />
            </button>
        </div>
    </div>
</div>
