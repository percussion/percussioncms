<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import=" com.percussion.utils.PSSpringBeanProvider" %>
<%@ page import="com.percussion.utils.service.impl.PSUtilityService" %>
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
    PSUtilityService utilityService = (PSUtilityService) PSSpringBeanProvider.getBean("utilityService");
    boolean isSaaS = utilityService.isSaaSEnvironment();
    String debug = request.getParameter("debug");
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    String site = request.getParameter("site");
    if (site == null)
        site = "";
    if (debug == null)
        debug = "false";
%>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%=debug%>"/>
<!DOCTYPE html>
<html lang="<%=lang %>">
<head>
    <title><i18n:message key="perc.ui.navMenu.users@Users"/></title>
    <!--Meta Includes -->
    <%@include file="includes/common_meta.jsp" %>
    <%--
         When ran in normal mode all javascript will be in one compressed file and
         the same for css (Currently just concatenated but not compressed.).
         To run from the non-compressed file simply add the query string param
         ?debug=true to the url for the page.

         Be sure that when a new javascript file is added to the page, an entry
         for each inclusion will be needed in the appropriate concat task within
         the minify target in the build.xml file. If this is not done then it won't
         get into the files used in production.
    --%>

    <!-- Themes never should be concatenated or packed -->
    <link rel="stylesheet" type="text/css" href="../themes/smoothness/jquery-ui-1.7.2.custom.css"/>
    <script
            src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%= locale %>"></script>

    <% if (isDebug) { %>
    <!-- CSS Includes -->
    <%@include file="includes/common_css.jsp" %>
    <link type="text/css" href="../css/perc_css_editor.css" rel="stylesheet"/>
    <link type="text/css" href="../css/layout.css" rel="stylesheet"/>
    <link type="text/css" href="../css/styles.css" rel="stylesheet"/>
    <link type="text/css" href="../css/perc_template_layout.css" rel="stylesheet"/>
    <link type="text/css" href="../css/perc_mcol.css" rel="stylesheet"/>
    <link type="text/css" href="../css/perc_decoration.css" rel="stylesheet"/>
    <link type="text/css" href="../demos.css" rel="stylesheet"/>
    <link type="text/css" href="../css/jquery.tooltip.css" rel="stylesheet"/>
    <!-- Stuff needed for finder to work like Editor -->
    <link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css"/>

    <!-- JavaScript Includes (order matters) -->
    <%@include file="includes/common_js.jsp" %>
    <script src="../services/PercUserService.js"></script>
    <script src="../controllers/PercUserController.js"></script>
    <script src="../views/PercUserView.js"></script>
    <%@include file="includes/finder_js.jsp" %>
    <script src="../plugins/perc_ChangePwDialog.js"></script>
    <% } else { %>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_users.packed.min.css"/>
    <script src="../jslibMin/perc_users.packed.min.js"></script>
    <% } %>
    <script>
        // dont allow navigation and window events if template is dirty
        // this method is bound to body's onbeforeunload event
        // if method returns string, it's used to display message and confirmation to navigate away
        // if method returns nothing, navigation is allowed
        var dirtyController = $j.PercDirtyController;
        function navigationEvent() {
            // if template is not dirty, return nothing and allow navigation
            // otherwise return alert message and display confirmantion box
            return dirtyController.navigationEvent();
        }

        //Finder initialization code
        $j(document).ready(function () {

            $j("#perc-manual-publish-widget").find(".perc-foldable").click();
            $j.Percussion.PercFinderView();
            $j.PercUserView();

            $j("select").keypress(function () {
                dirtyController.setDirty(true, "asset");
            });

            fixBottomHeight();
        });

    </script>

    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="../css/IE_styles.css"/><![endif]-->
    <!--[if gte IE 8]>
    <link rel="stylesheet" type="text/css" href="../css/IE8_styles.css"/><![endif]-->

    <style>
        #perc-users-import-users-dialog-fixed {
            padding: 4px;
        }

        #perc-users-import-users-dialog-fixed div.ui-dialog-content {
            padding: 10px;
            padding-bottom: 50px;
        }

        #perc-users-import-users-dialog-fixed input {
            width: 10px;
        }

        .ui-dialog .ui-dialog-title {
            float: left;
            color: #666666;
            font-family: verdana;
            font-size: 16px;
        }

        #perc-users-narrow-search {
            top: 108px;
        }
    </style>
</head>
<body onbeforeunload="return navigationEvent()" style="overflow : hidden">
<div class="perc-main perc-finder-fix" align="center">
    <jsp:include page="includes/header.jsp" flush="true">
        <jsp:param name="mainNavTab" value="users"/>
    </jsp:include>
    <div class="ui-layout-north" style="padding: 0px 0px; overflow: visible">
        <jsp:include page="includes/finder.jsp" flush="true">
            <jsp:param name="openedObject" value="PERC_SITE"/>
        </jsp:include>
    </div>
</div>
<div class="perc-body-background">
    <div id="perc-pageEditor-toolbar-content"
         class="ui-helper-clearfix">
    </div>

    <div class="button" id="perc-content-menu" align= "right">
        <button id="perc-users-save" class="btn btn-primary" type="button"><i18n:message key ="perc.ui.button@Save"/></button>
        <a id="perc-users-cancel" class="btn btn-primary" type="button" name="perc_wizard_cancel" style="float: right;"><i18n:message key ="perc.ui.common.label@Cancel"/> </a>
    </div>

    <div id="perc-pageEditor-toolbar-content" class="ui-helper-clearfix"></div>

    <div class='perc-whitebg' style="overflow : auto">

        <span style="position: relative; float: right; margin-right: 10px; margin-top: 2px; font-family: Verdana; font-size: 11px;"><label>* - denotes required field</label></span>
        <div id="perc-users-wrapper" style="width:1024px">

            <div id="perc-users-list">
                <div class="perc-user-list-label">
                    <i18n:message key = "perc.ui.editSiteSectionDialog.label@Users"/>
                    <div id="perc-users-add-user-button" title="Add new user"></div>
                    <div id="perc-users-import-users-button"
                         title="<i18n:message key="perc.ui.users.import.tooltips@ImportDirectoryUsersTooltip"/>"
                         class="perc-users-import-users-button-enabled"></div>
                </div>

                <div id="perc-username-list">

                    <ul>
                    </ul>

                </div>
            </div>
            <div id="perc-users-details">
                <div id="perc-users-info">
                    <form autocomplete="off">
                        <div id="perc-users-edit-user-button" title=<i18n:message key = "perc.ui.users@Edit User Details"/>></div>
                        <div id="perc-users-username-label"><i18n:message key = "perc.ui.workflow@Name"/><br/>
                            <input id="perc-users-username-field" autocomplete="nope"/>
                        </div>
                        <br/>
                        <div id="perc-users-external-user-label" style="display : none"><i18n:message
                                key="perc.ui.users.import.label@ThisIsLDAPUser"/>.
                        </div>
                        <div id="perc-users-password-block">
                            <div id="perc-users-password-label"><i18n:message key = "perc.ui.users@Password"/><br/>
                                <input id="perc-users-password-field" value="*******" type="password" autocomplete="new-password"/>
                            </div>
                            <br/>
                            <div id="perc-users-password-confirm-label"><i18n:message key = "perc.ui.users@Confirm Password"/><br/>
                                <input id="perc-users-password-confirm-field" value="*******" type="password" autocomplete="new-password"/>
                            </div>
                            <br/>
                        </div>
                        <div id="perc-users-email-label"><i18n:message key = "perc.ui.users@Email"/><br/>
                            <input id="perc-users-email-field" maxlength="250"/>
                        </div>
                    </form>
                </div>
                <div id="perc-users-roles-editor">
                    <div id="perc-users-available-roles">
                        <div id="perc-users-available-roles-label"><i18n:message key = "perc.ui.users@Available Roles"/></div>
                        <select size="5"></select>
                    </div>
                    <div id="perc-users-roles-add-remove-buttons">
                        <div id="perc-users-add-role-button"></div>
                        <div id="perc-users-remove-role-button"></div>
                    </div>
                    <div id="perc-users-assigned-roles">
                        <div id="perc-users-assigned-roles-label" class="perc-required-field"><i18n:message key = "perc.ui.users@Assigned Roles"/></div>
                        <select size="5">
                        </select>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="perc-users-import-users-dialog-fixed" class="perc-dialog">
    <div class="ui-dialog-content" style="height : 410px; overflow : hidden">

        <div id="perc-users-search">
                    <span id="perc-users-search-label"><i18n:message key="perc.ui.users.import.dialogs@NameStartsWith"/>
                    </span><br/>
            <input id="perc-users-search-input" style="width : 355px"></input>
            <div id="perc-users-search-button"></div>
            <div id="perc-users-narrow-search" style="position : absolute; display : none"></div>
        </div>
        <!-- header for the table below to select/deselect all users -->
        <br/>
        <div style="margin-left : 5px;">
            <table id="perc-users-directory-users-header">
                <tr>
                    <td><input id="perc-users-directory-users-selectall-checkbox" type="checkbox"></input>
                    </td>
                    <td><span id="perc-users-directory-users-selectall-label"><i18n:message
                            key="perc.ui.users.import.dialogs@SelectAll"/></span>
                    </td>
                </tr>
            </table>
        </div>
        <!-- a scrollable list of users from the directory service created dinamically -->
        <div id="perc-users-directory-users-list">
            <table id="perc-users-directory-users-table" style="cellpadding : 5px">
            </table>
        </div>
        <span id="perc-users-select-at-least-one-user-label" style="display:none">* <i18n:message
                key="perc.ui.users.import.dialogs@SelectOneUser"/></span>
        <div id="perc-users-directory-users-buttons">
            <div id="perc-users-directory-users-import-button" class="perc-users-directory-users-import-button-disabled"
                 title="<i18n:message key="perc.ui.users.import.tooltips@SelectUsersToImport"/>">
            </div>
            <div id="perc-users-directory-users-cancel-button">
            </div>
        </div>

    </div>
</div>
</body>
</html>
