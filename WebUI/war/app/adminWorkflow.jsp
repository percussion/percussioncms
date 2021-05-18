<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    if (site == null)
        site = "";
    if (debug == null)
        debug = "false";
%>
<i18n:settings lang="<%=locale%>" prefixes="perc.ui." debug="<%=debug%>"/>
<!DOCTYPE html>
<html lang="<%= lang %>">
<head>
    <title><i18n:message key="perc.ui.navMenu.workflow@Administration"/></title>
    <!--Meta Includes -->
    <meta name="viewport" content="width=device-width, initial-scale=1">
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
    <!-- <link rel="stylesheet" type="text/css" href="../cui/components/twitter-bootstrap-3.0.0/dist/css/bootstrap.min.css"/> -->
     <link rel="stylesheet" type="text/css" href="../jslib/profiles/3x/libraries/bootstrap/css/bootstrap.min.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%= locale%>"></script>
    <script src="../cui/components/jquery/jquery.min.js"></script>
    <script src="../cui/components/jquery/jquery-migrate.min.js"></script>
    <script src="../jslib/profiles/3x/libraries/bootstrap/js/bootstrap.bundle.min.js"></script>
    <% if (isDebug) { %>

    <!-- CSS Includes -->
    <%@include file="includes/common_css.jsp" %>
    <link type="text/css" href="../css/perc_css_editor.css" rel="stylesheet"/>
    <link type="text/css" href="../css/styles.css" rel="stylesheet"/>
    <link type="text/css" href="../css/percWorkflow.css" rel="stylesheet"/>
    <link type="text/css" href="../css/perc_mcol.css" rel="stylesheet"/>
    <link type="text/css" href="../css/perc_decoration.css" rel="stylesheet"/>
    <link type="text/css" href="../css/jquery.tooltip.css" rel="stylesheet"/>
    <!-- Stuff needed for finder to work like Editor -->
    <link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css"/>
    
    <!-- JavaScript Includes (order matters) -->
    <%@include file="includes/common_js.jsp" %>
    <script src="../jslib/jquery.dynatree.js"></script>
    <script src="../plugins/PercDataList.js"></script>
    <script src="../plugins/PercAddItemDialog.js"></script>
    <script src="../services/PercUserService.js"></script>
    <script src="../controllers/PercUserController.js"></script>
    <script src="../views/PercUserView.js"></script>
    <script src="../controllers/PercRoleController.js"></script>
    <script src="../views/PercRoleView.js"></script>
    <script src="../services/PercCategoryService.js"></script>
    <script src="../controllers/PercCategoryController.js"></script>
    <script src="../views/PercCategoryView.js"></script>
    <script src="../views/PercEditWorkflowStepDialog.js"></script>
    <script src="../views/PercWorkflowStepsView.js"></script>
    <script src="../plugins/PercDataTree.js"></script>
    <script src="../plugins/perc_assign_workflow_sites_folder_dialog.js"></script>
    <script src="../views/PercWorkflowView.js"></script>
    <script src="../plugins/perc_ChangePwDialog.js"></script>
    <%@include file="includes/finder_js.jsp" %>
    <% } else { %>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_users.packed.min.css"/>
    <script src="../jslibMin/perc_users.packed.min.js"></script>
    <% } %>
    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="../css/IE_styles.css"/><![endif]-->
    <!--[if gte IE 8]>
    <link rel="stylesheet" type="text/css" href="../css/IE8_styles.css"/><![endif]-->
    <script  >
        var dirtyController = $j.PercDirtyController;
        function navigationEvent() {
            // if template is not dirty, return nothing and allow navigation
            // otherwise return alert message and display confirmantion box
            $j.wfViewObject.cancelJobs();
            return dirtyController.navigationEvent();
        }
        //Initialization code
        $j(document).ready(function () {
            $j.Percussion.PercFinderView();
            $j.PercWorkflowView();
            $j.PercUserView();
            $j.PercRoleView();
            $j.PercCategoryView();
            fixBottomHeight();
            $j("select").keypress(function () {
                dirtyController.setDirty(true, "asset");
            });
            $j("#tabs").tabs({
                select: function (event, ui) {
                    if (dirtyController.isDirty()) {
                        // if dirty, then show a confirmation dialog                    
                        dirtyController.confirmIfDirty(function () {
                            // if they click ok, then reset dirty flag and proceed to select the tab
                            dirtyController.setDirty(false);
                            $j.PercUserController.cancel();
                            $j.PercRoleController.cancel();
                            $j("#tabs").tabs('select', ui.index);
                        });
                        return false;
                    }

                    if (ui.index != 3) {
                        $j.PercCategoryController.getTabLockData(function (lockinfo) {
                            if (lockinfo.userName == $j.PercNavigationManager.getUserName()) {
                                // delete the category tab lock file.
                                $j.PercCategoryController.removeCatTabLock(function(){
                                	
                                });
                            }
                        });

                    }
                    loadTab(ui.index);
                }

            });


            function loadTab(index) {
                var WORKFLOW_TAB = 0;
                var ROLES_TAB = 1;
                var USERS_TAB = 2;
                var CATEGORIES_TAB = 3;
                if (index === USERS_TAB) {
                    // Update the 'Available Roles'cache object
                    $j.PercUserController.cacheAllRoles();

                    //Update the Assgined Roles list for currently selected user                 
                    $j("li.perc-users-selected").click();
                    $j("#perc-wf-update-cancel").click();
                }
                else if (index === ROLES_TAB) {
                    //Update the Assigned Users list for currently selected role
                    $j("li.perc-item-selected").click();
                    $j("#perc-wf-update-cancel").click();
                }
                else if (index === WORKFLOW_TAB) {
                    //Update the Workflow to get changes if a role has been deleted
                    var workflowName = $j("#perc-workflows-list .perc-itemname-list").find(".perc-item-selected").text();
                    if ($j.wfViewObject.isWorkflowAvailable) {
                        if (workflowName)
                            $j.PercWorkflowStepsView.refresh(workflowName);
                        else
                            $j.PercWorkflowView().init();
                    }
                }
                else if (index === CATEGORIES_TAB) {

                    $j.PercCategoryController.getTabLockData(function (lockinfo) {
                        var user = lockinfo.userName;

                        if (user != null && user !== "") {
                            var currentDate = new Date();
                            var lockDate = Date.parse(lockinfo.creationDate);
                            var diffMs = (currentDate - lockDate);
                            var diffMins = Math.round(((diffMs % 86400000) % 3600000) / 60000); // difference in minutes

                            // compare the current time with the lock creation time. If the difference is more than or equal to 20 min, override the lock to the new user.
                            if (diffMins >= 20) {
                                $j.PercCategoryController.lockCategoryTab();
                            } else if (user !== $j.PercNavigationManager.getUserName()) {
                                $j.PercCategoryController.confirmDialog(I18n.message("perc.ui.admin.workflow@Categories Locked", [0]), function (action) {
                                    if (action === "cancel") {
                                        $j("#tabs").tabs('select', 0);
                                    }
                                });
                            }
                        } else {
                            // lock categories tab for the current user.
                            $j.PercCategoryController.lockCategoryTab();
                        }
                    });
                }
            }
        });

    </script>
</head>
<body onbeforeunload="return navigationEvent()" style="overflow : hidden">
<div class="perc-main perc-finder-fix">
    <jsp:include page="includes/header.jsp" flush="true">
        <jsp:param name="mainNavTab" value="workflow"/>
    </jsp:include>
    <div class="ui-layout-north" style="padding: 0px 0px; overflow: visible">
        <jsp:include page="includes/finder.jsp" flush="true">
            <jsp:param name="openedObject" value="PERC_SITE"/>
        </jsp:include>
    </div>
</div>
<div class="perc-workflow-container">
    <div id="tabs" style="min-width:500px;">
        <ul role="tablist">
            <li role="presentation"><a role="tab" id="perc-tab-workflow" href="#tabs-1"><i18n:message key = "perc.ui.navMenu.workflow@Workflow" /></a></li>
            <li role="presentation"><a role="tab" id="perc-tab-roles" href="#tabs-2"><i18n:message key = "perc.ui.perc.role.view@Roles" /></a></li>
            <li role="presentation"><a role="tab" id="perc-tab-users" href="#tabs-3"><i18n:message key = "perc.ui.editSiteSectionDialog.label@Users" /></a></li>
            <li role="presentation"><a role="tab" id="perc-tab-category" href="#tabs-4"><i18n:message key = "perc.ui.admin.workflow@Categories"/></a></li>
        </ul>
        <div id="tabs-1" role="tabpanel">
            <jsp:include page="percWorkflow.jsp" flush="true"></jsp:include>
        </div>
        <div id="tabs-2" role="tabpanel">
            <jsp:include page="percRoles.jsp" flush="true"></jsp:include>
        </div>
        <div id="tabs-3" role="tabpanel">
            <jsp:include page="percUsers.jsp" flush="true">
                <jsp:param name="debug" value="<%=debug%>"/>
                <jsp:param name="lang" value="<%=lang%>"/>
            </jsp:include>
        </div>
        <div id="tabs-4" role="tabpanel">
            <jsp:include page="percCategories.jsp" flush="true"></jsp:include>
        </div>
    </div>
</div>
<%@include file='includes/siteimprove_integration.html'%>
</body>
</html>
