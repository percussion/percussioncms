<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
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
    List<String> supportedScreens = new ArrayList<String>();
    supportedScreens.add("list");
    supportedScreens.add("library");
    supportedScreens.add("newitem");
    supportedScreens.add("search");

    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    if (debug == null)
        debug = "false";
    String initialScreen = request.getParameter("initialScreen");
    if (!supportedScreens.contains(initialScreen))
        initialScreen = "list";

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
    Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
    //TODO: I18N Below
    String inlineHelpMsg = PSI18nUtils.getString("perc.ui.home@Click on Site", locale)
            + PSI18nUtils.getString("perc.ui.home@Click A Folder", locale);
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
<meta name="viewport" content="width=device-width, initial-scale=1">
<!--Meta Includes -->
<%@include file="includes/common_meta.jsp" %>
    <%@include file='includes/siteimprove_integration.html'%>

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
<script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%=locale%>"></script>

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
<link rel="stylesheet" type="text/css" href="../css/PercPageOptimizerDialog.css"/>
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

<script src="../plugins/perc_save_as_shared_asset_dialog.js"></script>
<script src="../widgets/perc_asset_edit_dialog.js"></script>
<script src="../widgets/perc_page_edit_dialog.js"></script>
<script src="../widgets/perc_save_as.js"></script>

<%@include file="includes/finder_js.jsp" %>
<script src="../widgets/perc_collapsible.js"></script>
<script src="../plugins/perc_layout_controller.js"></script>
<script src="../widgets/perc_widget_library.js"></script>
<script src="../plugins/perc_content_viewer.js"></script>
<script src="../plugins/perc_contentEditDecorate.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-datatables/js/jquery.dataTables.js"></script>

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
<script src="../widgets/PercAutoScroll.js"></script>
<script src="../views/PercLayoutView.js"></script>
<script src="../views/PercInspectionToolHandler.js"></script>

<script src="../views/PercContentView.js"></script>
<script src="../views/PercCSSPreviewView.js"></script>
<script src="../views/widgetPropertiesDialog.js"></script>

<script src="../plugins/PercContentEditorHandlers.js"></script>
<script src="../services/PercRevisionService.js"></script>
<script src="../plugins/PercRevisionDialog.js"></script>
<script src="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/timepicker.js"></script>
<script src="../plugins/PercScheduleDialog.js"></script>
<script src="../views/PercChangeTemplateDialog.js"></script>

<script src="../widgets/PercSimpleMenu/PercSimpleMenu.js"></script>
<script src="../widgets/PercPageDataTable/PercPageDataTable.js"></script>
<script src="../widgets/PercActionDataTable/PercActionDataTable.js"></script>
<script src="../services/PercSiteImpactService.js"></script>
<script src="../views/PercSiteImpactView.js"></script>
<script src="../plugins/PercCommentsDialog.js"></script>
<script src="../services/PercPageOptimizerService.js"></script>
<script src="../plugins/PercPageOptimizerDialog.js"></script>
<script src="../services/PercUtilService.js"></script>
<script src="../plugins/PercContributorUiAdaptor.js"></script>
<script src="../services/PercBlogService.js"></script>
<script src="../services/PercReusableSearchService.js"></script>
<script src="../plugins/perc_ChangePwDialog.js"></script>
<% } else {
%>
<script src="../jslibMin/perc_webmgt.packed.min.js"></script>
<link rel="stylesheet" type="text/css" href="../cssMin/perc_webmgt.packed.min.css"/>
<% } %>
<!-- Stuff needed for finder to work like Editor -->
<%if ("library".equals(initialScreen)) { %>
<script>
    //Finder initialization code
    $(document).ready(function () {
        $.Percussion.PercFinderView();
        $("#top-menu .button").on("click",function () {
            window.location.search = "?view=home&initialScreen=" + $(this).attr("for");
        })
    });
</script>
<%} %>

<link rel="stylesheet" type="text/css" href="/cm/cui/components/perc-css/perc.css"/>

<script  >
    gDebug = <%= debug %>;
    gInitialScreen = "<%=initialScreen%>";
</script>
</head>
<body>
<div class="perc-main <%if("library".equals(initialScreen)){%>perc-mode-library<%}else{%>perc-mode-contributorui<%}%>">
    <jsp:include page="includes/header.jsp" flush="true">
        <jsp:param name="mainNavTab" value="home"/>
    </jsp:include>
</div>
<%if ("library".equals(initialScreen)) { %>
<div id="top-menu" class="top-menu" style="margin:36px 20px 20px 70px;">
    <div id="add-new-action-button" class="button primary-button" for="newitem" style="margin-right:30px;"><i18n:message key = "perc.ui.home@Add New"/></div>
    <div id="search-action-button" class="button primary-button" for="search" style="margin-right:30px;"><i18n:message key = "perc.ui.dashboard@Search"/></div>
    <div id="library-toggle-action-button" class="button primary-button primary-button-ext" for="list"
         style="margin-right:30px;"><i18n:message key = "perc.ui.home@My Recent"/>
    </div>
</div>
<div class="ui-layout-north" style="padding: 0px 0px; overflow: visible">
    <jsp:include page="includes/finder.jsp" flush="true">
        <jsp:param name="openedObject" value="PERC_SITE"/>
        <jsp:param name="finderMode" value="library"/>
    </jsp:include>
</div>
<%} else { %>
<iframe id="cui-frame" title="<i18n:message key='perc.ui.navMenu.home@Home' />" src="/cm/cui/index.html"
        style="height:92%;width:100%;position:absolute;top:60px;left:0px;right:0px;bottom:0px" width="100%"
        height="100%"></iframe>
<%} %>
</body>
</html>
