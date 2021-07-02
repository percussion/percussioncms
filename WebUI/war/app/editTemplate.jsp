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
    String ua = request.getHeader( "User-Agent" );
    boolean isMSIE = ( ua != null && ua.indexOf( "MSIE" ) != -1 );
    String site = request.getParameter("site");
    if(site == null)
        site = "";

%>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<!DOCTYPE html>
<html lang="<%= lang %>">
<head>
    <title><i18n:message key="perc.ui.admin.title@Administration"/></title>
    <!--Meta Includes -->
    <%@include file="includes/common_meta.jsp"%>

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
    <link rel="stylesheet" type="text/css" href="../themes/smoothness/jquery-ui-1.8.9.custom.css" />
    <link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%=locale%>"></script>
    <%  if(isDebug)
    {
    %>

    <!-- CSS Includes -->
    <%@include file="includes/common_css.jsp"%>
    <%@include file="includes/template/editTemplate_css.jsp"%>

    <!-- JavaScript Includes (order matters) -->
    <%@include file="includes/common_js.jsp"%>
    <%@include file="includes/template/editTemplate_js.jsp"%>

    <%@include file="includes/finder_js.jsp"%>


    <%  }
    else
    {
    %>
    <script src="../jslibMin/perc_editTemplate.packed.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_editTemplate.packed.min.css" />
    <%  }
    %>
    <!--[if lte IE 7]><link rel="stylesheet" type="text/css" href="../css/IE_styles.css" /><![endif]-->
    <!--[if gte IE 8]><link rel="stylesheet" type="text/css" href="../css/IE8_styles.css" /><![endif]-->

    <script>

        var sGalleryThemeName = "";
        var selectedTemplate;
        var global_templates;
        var global_all_templates = new Array();
        var gPageId;
        var gSelectedView;
        var memento;
        memento = $.PercNavigationManager.getMemento();
        if(memento.tabId)
        {
            var tabId = '#'+memento.tabId;
        }
        else
        {
            var tabId = '#' + 'perc-tab-content';
        }

        if(memento.pageId)
        {
            memento['isEditPage'] = true;
        }
        else
        {
            memento['isEditPage'] = false;
        }

        $(document).ready(function () {
            var querystring = $.deparam.querystring();

            $.Percussion.templateView();
            $.Percussion.PercFinderView();

            gSelectTemp = memento.templateId;
            gPageId = memento.pageId;
            gSelectedView = memento.view;
            $(tabId).trigger("click");

            // Add close button to content menu
            var closeButton = "<button style='float: right;' class='btn btn-primary perc-close' title='Click to Close' id='perc-template-close'>Close</button>";
            $("#perc-content-menu").append($(closeButton));
            $("#perc-template-close").on("click",function(){
                clearCacheRegionCSS();
                // check in the page
                if (gPageId != null && gPageId != "")
                {
                    $.PercWorkflowService().checkIn(gPageId, function(status, result){});
                }
                var memento = {'templateId' : gSelectTemp, 'pageId' : gPageId, 'view' : gSelectedView};
                // Use the PercNavigationManager to switch to the template editor
                $.PercNavigationManager.goToLocation(
                    $.PercNavigationManager.VIEW_DESIGN,
                    querystring.site,
                    null,
                    null,
                    null,
                    querystring.path,
                    null,
                    memento);
                function clearCacheRegionCSS()
                {
                    var model = $.PercNavigationManager.getTemplateModel();
                    if (model !== undefined)
                    {
                        $.PercTemplateService().regionCSSClearCache(
                            model.getTemplateObj().Template.theme,
                            model.getTemplateObj().Template.name,
                            function(status, data) {});
                    }
                }
            });
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
    <div class="perc-header">
        <jsp:include page="includes/header.jsp" flush="true">
            <jsp:param name="mainNavTab" value="siteadmin" />
        </jsp:include>
    </div>

    <div class="ui-layout-north" style="padding: 0px 0px; overflow:visible">
        <jsp:include page="includes/finder.jsp" flush="true">
            <jsp:param name="openedObject" value="PERC_SITE" />
        </jsp:include>
        <div id="tabs" class = 'perc-template-tabs'>
            <ul>
                <li>
                    <a id="perc-tab-content" href="#tabs-2" ><i18n:message key="perc.ui.web.mgt@Content"/></a>
                </li>
                <li><a id="perc-tab-layout" href="#tabs-3" ><i18n:message key = "perc.ui.web.mgt@Layout"/></a></li>
                <li><a id="perc-tab-style" href="#tabs-4" ><i18n:message key = "perc.ui.web.mgt@Style"/></a></li>
                <div class="perc-template-details">
                    <div id="perc_selected_template_name"><span class ="perc-template-name-label"><i18n:message key = "perc.ui.edit.template@Editing Template"/></span> <span class = "perc-template-name-text"></span></div>
                </div>
            </ul>
            <div id="tabs-2" style="padding-top: 11px;" >
                <jsp:include page="includes/content_toolbar.jsp" flush="true"/>
            </div>
            <div id="tabs-3" style="padding-top: 11px;" >
                <jsp:include page="template_layout.jsp" flush="true"/>
            </div>
            <div id="tabs-4" style="padding-top: 11px;" >
                <jsp:include page="template_style.jsp" flush="true"/>
            </div>
            <div id="perc-pageEditor-edit-template-toolbar-content" class="ui-helper-clearfix"> </div>
            <div id="bottom"></div>
        </div>
    </div>
    <iframe id="frame" name="frame"  title="<i18n:message key='perc.ui.design.title@Edit Template'/>" style="width: 100%; border: 0;" width="100%" class="perc-ui-component-ready"></iframe>
</div>

<%@include file='includes/siteimprove_integration.html'%>
</body>
</html>
