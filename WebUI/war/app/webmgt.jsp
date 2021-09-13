<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<%@ page import="com.percussion.i18n.PSI18nUtils" %>
<%@ page import="com.percussion.i18n.ui.PSI18NTranslationKeyValues" %>
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
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    if (debug == null)
        debug = "false";
    String ua = request.getHeader("User-Agent");
    boolean isMSIE = (ua != null && ua.indexOf("MSIE") != -1);
    char msiever = '0';
    if (isMSIE) {
        int msieverind = ua.indexOf("MSIE ");
        if (ua.length() > ua.indexOf("MSIE ") + 5)
            msiever = ua.charAt(ua.indexOf("MSIE ") + 5);
    }

    //Set inline help message
    Boolean hasSites = (Boolean) request.getAttribute("hasSites");
    if(hasSites==null)
        hasSites=false;

    Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
    if(isAdmin==null)
        isAdmin = false;

    String inlineHelpMsg = PSI18nUtils.getString("perc.ui.home@Click on Site Work on Pages", locale)
            + PSI18nUtils.getString("perc.ui.home@Click A Folder Work on Assets", locale);
    if (!hasSites) {
        inlineHelpMsg = isAdmin
                ? PSI18nUtils.getString("perc.ui.home@Click Create Site", locale)
                : PSI18nUtils.getString("perc.ui.home@No Site Exists", locale);
    }

%>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<!DOCTYPE html>
<html lang="<%=lang %>">
<head>
    <title><i18n:message key="perc.ui.webmgt.title@Web Management"/></title>
    <!--Meta Includes -->
    <meta http-equiv="X-UA-Compatible" content="IE=10"/>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
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
    <link rel="stylesheet" type="text/css" href="../themes/smoothness/jquery-ui-1.8.9.custom.css"/>
    <link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%=locale %>"></script>
    <script src="/JavaScriptServlet"></script>
    <% if (isDebug) { %>

    <!-- CSS Includes -->
    <%@include file="includes/common_css.jsp" %>
    <link rel="stylesheet" type="text/css" href="../css/perc_mcol.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_widget_library.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_viewport.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_page_editor.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_collapsible.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_save_as_dialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css"/>
    <link rel="stylesheet" type="text/css" href="../css/jquery.jmodal.css"/>
    <link rel="stylesheet" type="text/css" href="../css/styles.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_template_layout.css"/>
    <link rel="stylesheet" type="text/css" href="../css/PercCommentsDialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css" />

    <%--  JavaScript Includes (order matters)
          this include goes first since it loads infrastructure and common files
    --%>
    <%@include file="includes/common_js.jsp" %>
    <%@include file="includes/finder_js.jsp" %>
    <%--  Services
          ========
          Services should not have any dependencies other than the base common service PercServiceUtil.js
          PercServiceUtils.js goes first because all other services (should) depend on it
    --%>

    <%--  Adapters
          ========
          These are scripts that convert between various types of data
                   such as between JSON and XML
    --%>

    <%--  Controllers
          ===========
          Controllers encode the logic and flow of the application
          Controllers make changes to the state or model of the application on behalf
          of the user as they interact with the user interface.
          Controllers interpret the events of the user interface and act on the data.
          Controllers depend on the definition of datastructures and model as well as
          the services used to create, load, save, persist the model back to the server.
          Controllers should load after models and services
    --%>

    <%-- Views: Views implement the user interface capturing the user's gestures
            and notifying the controller of user events and rendering data coming back
            from the controllers. These include widgets (jQuery), dialogs, and other
            UI related JavaScript files
    --%>

    <%-- --%>
    <script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.xmldom-1.0.js"></script>
    <script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.jmodal.js"></script>

    <script src="../widgets/PercSimpleMenu/PercSimpleMenu.js"></script>
    <script src="../widgets/PercPageDataTable/PercPageDataTable.js"></script>
    <script src="../widgets/PercActionDataTable/PercActionDataTable.js"></script>
    <script src="../widgets/PercAutoScroll.js"></script>
    <script src="../services/PercPathService.js"></script>
    <script src="../services/PercAssetService.js"></script>
    <script src="../services/PercItemPublisherService.js"></script>
    <script src="../services/PercWorkflowService.js"></script>
    <script src="../services/PercRevisionService.js"></script>
    <script src="../services/PercFolderService.js"></script>
    <script src="../services/PercSiteImpactService.js"></script>
    <script src="../services/PercUtilService.js"></script>
    <script src="../services/PercLicenseService.js"></script>
    <script src="../services/PercReusableSearchService.js"></script>
    <script src="../controllers/PercCSSController.js"></script>
    <script src="../views/PercNewAssetDialog.js"></script>
    <script src="../views/PercCSSPreviewView.js"></script>
    <script src="../views/PercChangeTemplateDialog.js"></script>
    <script src="../views/PercSiteImpactView.js"></script>
    <script src="../views/PercEditRegionPropertiesDialog.js"></script>
    <script src="../plugins/perc_page_schema.js"></script>
    <script src="../widgets/perc_asset_edit_dialog.js"></script>
    <script src="../widgets/perc_page_edit_dialog.js"></script>
    <script src="../widgets/perc_save_as.js"></script>
    <script src="../plugins/perc_save_as_shared_asset_dialog.js"></script>
    <script src="../widgets/perc_widget_library.js"></script>
    <script src="../plugins/perc_contentEditDecorate.js"></script>
    <script src="../plugins/perc_content_viewer.js"></script>
    <script src="../classes/perc_page_class.js"></script>
    <script src="../classes/perc_template_layout_class.js"></script>
    <script src="../plugins/perc_template_layout_helper.js"></script>
    <script src="../widgets/perc_template_layout_widget.js"></script>
    <script src="../plugins/perc_layout_controller.js"></script>
    <script src="../plugins/perc_template_manager.js"></script>
    <script src="../plugins/perc_page_schema.js"></script>
    <script src="../plugins/perc_template_schema.js"></script>
    <script src="../models/PercTemplateModel.js"></script>
    <script src="../models/PercPageModel.js"></script>
    <script src="../services/PercTemplateService.js"></script>
    <script src="../controllers/PercLayoutController.js"></script>
    <script src="../controllers/PercSizeController.js"></script>
    <script src="../controllers/PercDecorationController.js"></script>
    <script src="../views/PercLayoutView.js"></script>
    <script src="../views/PercInspectionToolHandler.js"></script>
    <script src="../views/PercContentView.js"></script>
    <script src="../views/widgetPropertiesDialog.js"></script>
    <script src="../plugins/PercContentEditorHandlers.js"></script>
    <script src="../plugins/PercRevisionDialog.js"></script>
    <script src="../plugins/PercScheduleDialog.js"></script>
    <script src="../plugins/PercCommentsDialog.js"></script>
    <script src="../plugins/PercPublishingHistoryDialog.js"></script>
    <script src="../plugins/PercContributorUiAdaptor.js"></script>
    <script src="../services/PercBlogService.js"></script>
    <script src="../plugins/perc_ChangePwDialog.js"></script>
    <script src="../plugins/perc_newsitedialog.js"></script>
    <script src="../services/perc_sectionServiceClient.js"></script>

    <% } else { %>
    <script src="../jslibMin/perc_webmgt.packed.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_webmgt.packed.min.css"/>
    <% } %>

    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="../css/IE_styles.css"/><![endif]-->
    <!--[if gte IE 8]>
    <link rel="stylesheet" type="text/css" href="../css/IE8_styles.css"/><![endif]-->
    <style type="text/css">
        .perc-toolbar-item {
            float: left;
        }
    </style>

    <script>
        gDebug = <%= debug %>;
        //Create and initialize view wrapper and set it on manager.
        $(document).ready(function () {
            if (($.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT) &&
                ($.PercNavigationManager.getId())) {

                $.PercLicenseService.getAllModuleLicenses(function (status, data) {
                    if (status &&
                        data.moduleLicenses &&
                        data.moduleLicenses.moduleLicenses) {

                        var lic;
                        if (Array.isArray(data.moduleLicenses.moduleLicenses)) {
                            lic = data.moduleLicenses.moduleLicenses[0];
                        }
                        else {
                            lic = data.moduleLicenses.moduleLicenses;
                        }

                        var url = lic.uiProvider + '/scripts/native-cm1.js';
                        var element = document.createElement('script');
                        element.setAttribute('type', 'text/javascript');
                        element.setAttribute('src', url);
                        document.head.appendChild(element);
                    }
                });
            }

            $.PercViewReadyManager.init();
            var viewWrapper = $.PercComponentWrapper("perc-view-editor", ["perc-ui-component-finder", "perc-ui-component-editor-toolbar", "perc-ui-component-editor-frame"]);
            $.PercViewReadyManager.setWrapper(viewWrapper);
            $.Percussion.PercFinderView();
        });
        // dont allow navigation and window events if template is dirty
        // this method is bound to body's onbeforeunload event
        // if method returns string, it's used to display message and confirmation to navigate away
        // if method returns nothing, navigation is allowed
        var dirtyController = $.PercDirtyController;
        function navigationEvent() {
            // if template is not dirty, return nothing and allow navigation
            // otherwise return alert message and display confirmantion box
            return dirtyController.navigationEvent();
        }
    </script>
</head>
<body onbeforeunload="return navigationEvent()">
<div class="perc-main">
    <jsp:include page="includes/header.jsp" flush="true">
        <jsp:param name="mainNavTab" value="pageeditor"/>
    </jsp:include>

    <div id="perc-web-management">

        <jsp:include page="includes/finder.jsp" flush="true">
            <jsp:param name="openedObject" value="PERC_PAGE"/>
        </jsp:include>
        <div id="perc-pageEditor-tabs" perc-ui-component="perc-ui-component-editor-toolbar">
            <div class="perc-page-details">
                <div class="perc-page-status">
                            <span class="perc-page-status-label">
                                <i18n:message key = "perc.ui.webmgt@Status"/>
                            </span>
                    <span class="perc-page-status-status">
                            </span>
                </div>

                <div class="perc-page-name">
                    <div class="perc-page-name-name perc-ellipsis">

                    </div>
                </div>
                <div class="perc-my-pages">
                    <a class="perc-my-pages-action  perc-font-icon icon-star fas fa-star"></a>
                </div>
                <div style="clear:both"></div>
            </div>
            <ul>
                <li>
                    <a id="perc-tab-content" href="#tabs-2"><i18n:message key="perc.ui.web.mgt@Content"/></a>
                </li>
                <li>
                    <a id="perc-tab-layout"  href="#tabs-3"><i18n:message key = "perc.ui.web.mgt@Layout"/></a>
                </li>
                <li>
                    <a id="perc-tab-style"  href="#tabs-4"><i18n:message key = "perc.ui.web.mgt@Style"/></a>
                </li>
            </ul>
            <div id="tabs-2" style="padding-top: 10px;" >
                <jsp:include page="includes/content_toolbar.jsp" flush="true"/>
            </div>
            <div id="tabs-3" style="padding-top: 10px;" >
                <jsp:include page="template_layout.jsp" flush="true"/>
            </div>
            <div id="tabs-4" style="padding-top: 10px;" >
                <div id="perc-style-menu" >
                    <div id="perc-dropdown-actions-style" style="float:left;"></div>
                    <div id="perc-dropdown-view-style" style="float:left;"></div>
                    <button id="perc-css-editor-save" class="btn btn-primary" style="float:right;" name="perc_wizard_save"><i18n:message key ="perc.ui.button@Save"/></button>
                    <button id="perc-css-editor-cancel" class="btn btn-primary" style="float:right;" name="perc_wizard_cancel"><i18n:message key ="perc.ui.common.label@Cancel"/></button>
                </div>
            </div>
            <div id="perc-editor-inline-help">
                <p><%=inlineHelpMsg%>
                </p>
            </div>
            <div id="perc-pageEditor-toolbar-content" class="ui-helper-clearfix"></div>

        </div>
    </div>
    <div id="bottom"></div>
    <iframe id="frame" name="frame" title="<i18n:message key='perc.ui.navMenu.webmgt@Editor' />" style="width: 100%; border:0;" perc-ui-component="perc-ui-component-editor-frame"
            class="perc-ui-component-processing"></iframe>
</div>
<%@include file='includes/siteimprove_integration.html'%>


</body>
</html>
