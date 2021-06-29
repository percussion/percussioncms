<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ page import=" com.percussion.utils.PSSpringBeanProvider" %>
<%@ page import="com.percussion.assetmanagement.data.PSAsset" %>
<%@ page import="com.percussion.assetmanagement.service.impl.PSAssetService" %>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
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
    if (debug == null)
        debug = "false";
    String ua = request.getHeader("User-Agent");
    boolean isMSIE = (ua != null && ua.contains("MSIE"));
    char msiever = '0';
    if (isMSIE) {
        int msieverind = ua.indexOf("MSIE ");
        if (ua.length() > ua.indexOf("MSIE ") + 5)
            msiever = ua.charAt(ua.indexOf("MSIE ") + 5);
    }
    String assetId = request.getParameter("id");
    boolean isResource = false;
    if (assetId != null && assetId.length() > 0) {
        PSAssetService assetService = (PSAssetService) PSSpringBeanProvider.getBean("assetService");
        PSAsset asset = assetService.load(assetId, true);
        isResource = asset.isResource();
    }
%>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<!DOCTYPE html>
<html lang="<%=lang %>">
<head>
    <title><i18n:message key="perc.ui.webmgt.title@Web Management"/></title>
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
    <link rel="stylesheet" type="text/css" href="../themes/smoothness/jquery-ui-1.8.9.custom.css">
    <link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%=locale%>"></script>
    <% if (isDebug) { %>
    <!-- CSS Includes -->
    <%@include file="includes/common_css.jsp" %>
    <link rel="stylesheet" type="text/css" href="../css/perc_mcol.css">
    <link rel="stylesheet" type="text/css" href="../css/perc_widget_library.css">
    <link rel="stylesheet" type="text/css" href="../css/perc_viewport.css">
    <link rel="stylesheet" type="text/css" href="../css/perc_page_editor.css">
    <link rel="stylesheet" type="text/css" href="../css/perc_collapsible.css">
    <link rel="stylesheet" type="text/css" href="../css/perc_save_as_dialog.css">
    <link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css"/>
    <link rel="stylesheet" type="text/css" href="../css/jquery.jmodal.css">
    <link rel="stylesheet" type="text/css" href="../css/styles.css">
    <link rel="stylesheet" type="text/css" href="../css/perc_template_layout.css">
    <link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css"/>

    <%--  JavaScript Includes (order matters)
          this include goes first since it loads infrastructure and common files
    --%>
    <%@include file="includes/common_js.jsp" %>

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

    <script src="../plugins/perc_page_schema.js"></script>
    <script src="../plugins/perc_template_manager.js"></script>

    <script src="../widgets/perc_asset_edit_dialog.js"></script>
    <script src="../widgets/perc_page_edit_dialog.js"></script>
    <script src="../widgets/perc_save_as.js"></script>

    <%@include file="includes/finder_js.jsp" %>
    <script src="../widgets/perc_collapsible.js"></script>
    <script src="../plugins/perc_layout_controller.js"></script>
    <script src="../widgets/perc_widget_library.js"></script>
    <script src="../plugins/perc_content_viewer.js"></script>
    <script src="../plugins/perc_contentEditDecorate.js"></script>

    <script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.jmodal.js"></script>
    <script src="../classes/perc_page_class.js"></script>
    <script src="../classes/perc_template_layout_class.js"></script>
    <script src="../plugins/perc_template_layout_helper.js"></script>
    <script src="../widgets/perc_template_layout_widget.js"></script>

    <script src="../plugins/perc_template_schema.js"></script>
    <script src="../services/PercTemplateService.js"></script>
    <script src="../models/PercTemplateModel.js"></script>
    <script src="../models/PercPageModel.js"></script>
    <script src="../controllers/PercLayoutController.js"></script>
    <script src="../controllers/PercSizeController.js"></script>
    <script src="../controllers/PercDecorationController.js"></script>
    <script src="../controllers/PercCSSController.js"></script>
    <script src="../views/PercLayoutView.js"></script>

    <script src="../views/PercContentView.js"></script>
    <script src="../views/PercCSSPreviewView.js"></script>
    <script src="../views/widgetPropertiesDialog.js"></script>
    <script src="../widgets/PercSimpleMenu/PercSimpleMenu.js"></script>
    <script src="../widgets/PercPageDataTable/PercPageDataTable.js"></script>
    <script src="../widgets/PercActionDataTable/PercActionDataTable.js"></script>

    <script src="../plugins/PercContentEditorHandlers.js"></script>
    <script src="../services/PercRevisionService.js"></script>
    <script src="../services/PercSiteImpactService.js"></script>
    <script src="../plugins/PercRevisionDialog.js"></script>
    <script src="../plugins/PercPublishingHistoryDialog.js"></script>
    <script src="../views/PercSiteImpactView.js"></script>
    <script src="../plugins/PercScheduleDialog.js"></script>
    <script src="../plugins/perc_ChangePwDialog.js"></script>
    <% } else { %>
    <script src="../jslibMin/perc_webmgt.packed.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_webmgt.packed.min.css">
    <% } %>

    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="../css/IE_styles.css"><![endif]-->
    <!--[if gte IE 8]>
    <link rel="stylesheet" type="text/css" href="../css/IE8_styles.css"><![endif]-->
    <style type="text/css">
        .perc-toolbar-item {
            float: left;
        }
    </style>

    <script  >
        gDebug = <%= debug %>;
        $(document).ready(function () {
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
<div class="perc-main" align="center">
    <jsp:include page="includes/header.jsp" flush="true">
        <jsp:param name="mainNavTab" value="pageeditor"/>
    </jsp:include>

    <div id="perc-web-management" align="left">
        <jsp:include page="includes/finder.jsp" flush="true">
            <jsp:param name="openedObject" value="PERC_PAGE"/>
        </jsp:include>
        <div id="perc-content-menu" align="left" style="margin: 0 0 0 0;">
            <div style='float:left'><a href="#" id="perc-revisions-button"
                                       class="ui-widget ui-state ui-meta-pre-disabled"
                                       style="margin-left: 10px; margin-right:30px; line-height: 5em;">
                <i18n:message key="perc.ui.webmgt.pageeditor.menu@Revisions"/>
            </a></div>
            <%if (isResource) {%>
            <div style='float:left'><a href="#" id="perc-pubhistory-button"
                                       class="ui-widget ui-state ui-meta-pre-disabled"
                                       style="margin-left: 10px; margin-right:30px; line-height: 5em;">
                <i18n:message key="Publishing History"/>
            </a></div>
            <%}%>
            <div id="perc-dropdown-publish-now" style="float : left">
            </div>
            <div id="perc-dropdown-page-workflow" style="float : right" role="menu">
            </div>
        </div>
        <div id="perc-pageEditor-toolbar-content" class="ui-helper-clearfix"></div>
        <div id="bottom"></div>
    </div>
</div>
<div id="bottom"></div>
<iframe id="frame" name="frame" style="width: 100%" class="perc-ui-component-ready" frameborder="0"></iframe>
<%@include file='includes/siteimprove_integration.html'%>
</body>
</html>
