<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    String ua = request.getHeader("User-Agent");
    boolean isMSIE = (ua != null && ua.indexOf("MSIE") != -1);
    String site = request.getParameter("site");
    if (site == null)
        site = "";
   
%>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<!DOCTYPE html>
<html lang="<%= lang %>">
<head>
    <title><i18n:message key="perc.ui.admin.title@Administration"/></title>
    <!--Meta Includes -->
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <%@include file="includes/common_meta.jsp" %>

    <%--
       When ran in normal mode all javascript will be in one compressed file and
       the same for css (Currently just concatenated bu tnot compressed.).
       ?debug=true to the url for the page.

       Be sure that when a new javascript file is added to the page, an entry
       for each inclusion will be needed in the appropriate concat task within
       the minify target in the build.xml file. If this is not done then it won't
       get into the files used in production.
    --%>
    <!-- Themes never should be concatenated or packed -->
    <link rel="stylesheet" type="text/css" href="../themes/smoothness/jquery-ui-1.7.2.custom.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%= locale %>"></script>
    <% if (isDebug) { %>

    <!-- CSS Includes -->
    <%@include file="includes/common_css.jsp" %>
    <link rel="stylesheet" type="text/css" href="../css/perc_css_editor.css"/>
    <link rel="stylesheet" type="text/css" href="../css/styles.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_template_layout.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_mcol.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_save_as_dialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_decoration.css"/>
    <!-- Stuff needed for finder to work like Editor -->
    <link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css"/>
    <link rel="stylesheet" type="text/css" href="../widgets/PercTooltip/PercTooltip.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css"/>

    <!-- JavaScript Includes (order matters) -->
    <%@include file="includes/common_js.jsp" %>
    <%-- Common Utilities --%>
    <script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.text-overflow.js"></script>
    <script src="../jslib/profiles/3x/jquery/plugins/jquery-print-this/printThis.js"></script>

    <%--  Services --%>
    <script src="../services/PercTemplateService.js"></script>
    <script src="../services/PercSiteSummaryService.js"></script>

    <%--  Models --%>
    <script src="../classes/perc_template_summary_class.js"></script>

    <%--  Widgets --%>

    <%--  Views --%>
    <script src="../views/PercIFrameView.js"></script>
    <script src="../widgets/perc_template_metadata_dialog.js"></script>
    <script src="../controllers/PercSiteTemplatesController.js"></script>
    <script src="../widgets/PercTemplateLibraryWidget.js"></script>
    <script src="../plugins/PercAddTemplateDialog.js"></script>
    <script src="../widgets/PercSiteTemplatesWidget.js"></script>
    <script src="../classes/perc_page_class.js"></script>
    <script src="../classes/perc_template_layout_class.js"></script>
    <script src="../plugins/perc_template_layout_helper.js"></script>
    <script src="../widgets/perc_template_layout_widget.js"></script>
    <script src="../plugins/perc_layout_controller.js"></script>
    <script src="../plugins/perc_template_manager.js"></script>
    <script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.xmldom-1.0.js"></script>
    <script src="../plugins/perc_page_schema.js"></script>
    <script src="../plugins/perc_template_schema.js"></script>
    <script src="../plugins/perc_contentEditDecorate.js"></script>
    <%@include file="includes/finder_js.jsp" %>
    <script src="../widgets/perc_save_as.js"></script>
    <script src="../widgets/perc_asset_edit_dialog.js"></script>
    <script src="../plugins/perc_content_viewer.js"></script>
    <script src="../plugins/perc_page_schema.js"></script>
    <script src="../plugins/perc_template_schema.js"></script>
    <script src="../models/PercTemplateModel.js"></script>
    <script src="../controllers/PercLayoutController.js"></script>
    <script src="../controllers/PercSizeController.js"></script>
    <script src="../controllers/PercDecorationController.js"></script>
    <script src="../controllers/PercCSSController.js"></script>
    <script src="../widgets/PercAutoScroll.js"></script>
    <script src="../views/PercChangeTemplateDialog.js"></script>
    <script src="../views/PercLayoutView.js"></script>
    <script src="../views/PercInspectionToolHandler.js"></script>
    <script src="../views/PercTemplateDesignView.js"></script>
    <script src="../views/PercContentView.js"></script>
    <script src="../views/widgetPropertiesDialog.js"></script>
    <script src="../views/PercCSSPreviewView.js"></script>
    <script src="../views/PercCSSGalleryView.js"></script>
    <script src="../views/PercCSSThemeView.js"></script>
    <script src="../views/PercOverrideView.js"></script>
    <script src="../widgets/PercTooltip/PercImageTooltip.js"></script>

    <script src="../plugins/PercContentEditorHandlers.js"></script>
    <script src="../plugins/PercSiteSummaryDialog.js"></script>
    <script src="../plugins/perc_ChangePwDialog.js"></script>

    <% } else { %>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_admin.packed.min.css"/>
    <script src="../jslibMin/perc_admin.packed.min.js"></script>
    <% } %>
    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="../css/IE_styles.css"/><![endif]-->
    <!--[if gte IE 8]>
    <link rel="stylesheet" type="text/css" href="../css/IE8_styles.css"/><![endif]-->

    <script>

        var sGalleryThemeName = "";
        var selectedTemplate;
        var global_templates;
        var global_all_templates = new Array();

        $(document).ready(function () {
            $.Percussion.templateDesignView();
            $.Percussion.PercFinderView();
        });

        function percTempLibMaximizer() {
            var ;
            if ($("#tabs-1 #perc-temp-lib-expander").hasClass("expander-enabled")) {
                if ($("#tabs-1 .perc-template-container").hasClass("perc-visible")) {
                    $("#tabs-1 .perc-template-container").removeClass("perc-visible").addClass("perc-hidden");
                    $("#tabs-1 #perc-temp-lib-expander").removeClass("perc-whitebg");
                    $("#tabs-1 #perc-temp-lib-minimizer").replaceWith('<a id="perc-temp-lib-maximizer" style="float: left;" href="#"></a>');
                }
                else {
                    $("#tabs-1 .perc-template-container").removeClass("perc-hidden").addClass("perc-visible");
                    $("#tabs-1 #perc-temp-lib-expander").addClass("perc-whitebg");
                    $("#tabs-1 #perc-temp-lib-maximizer").replaceWith('<a id="perc-temp-lib-minimizer" style="float: left;" href="#"></a>');
                }
                // Fix the User Template Library area Height
                fixTemplateHeight();
            }
        }

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
    <div class="perc-header">
        <jsp:include page="includes/header.jsp" flush="true">
            <jsp:param name="mainNavTab" value="siteadmin"/>
        </jsp:include>
    </div>

    <div class="ui-layout-north" style="padding: 0px 0px; overflow:visible">
        <jsp:include page="includes/finder.jsp" flush="true">
            <jsp:param name="openedObject" value="PERC_SITE"/>
        </jsp:include>
        <div id="tabs">
            <ul>
                <div id="perc-site-templates-label" class="perc-site-templates-label">
                    <div id="perc-templates-selected-site" class="perc-templates-selected-site">
                        <span class="perc-templates-selected-site-label"><i18n:message key = "perc.ui.content.toolbar@Site Loaded"/></span>
                        <span class="perc-templates-selected-site-site"><%=site%></span>
                    </div>
                </div>
            </ul>
            <div id="tabs-1">
                <jsp:include page="template_create.jsp" flush="true"/>
            </div>
            <div id="bottom"></div>
        </div>
    </div>
</div>
<%@include file='includes/siteimprove_integration.html'%>
<iframe id="frame" name="frame" title="<i18n:message key='perc.ui.design.title@Edit Template' />" style="width: 100%; border: 0" width="100%" class="perc-ui-component-ready"></iframe>
</body>
</html>
