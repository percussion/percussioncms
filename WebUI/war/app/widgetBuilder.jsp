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
    String ua = request.getHeader("User-Agent");
    String site = request.getParameter("site");
    if (site == null)
        site = "";

%>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<!DOCTYPE html>
<html lang="<%=lang%>">
<head>
    <title><i18n:message key = "perc.ui.widget.builder@Widget Builder"/></title>
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
    <link rel="stylesheet" type="text/css" href="../themes/smoothness/jquery-ui-1.8.9.custom.css"/>
    <link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%= locale %>"></script>
    <script src="/JavaScriptServlet"></script>
    <%@include file="../widgetbuilder/templates/templates.jsp" %>

    <% if (isDebug) { %>

    <!-- CSS Includes -->
    <%@include file="includes/common_css.jsp" %>
    <link rel="stylesheet" type="text/css" href="../css/styles.css"/>
    <link rel="stylesheet" type="text/css" href="../jslib/profiles/3x/jquery/libraries/backgridjs/backgrid.css"/>
    <link rel="stylesheet" type="text/css" href="../widgetbuilder/css/perc_widgetbuilder.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css"/>

    <!-- JavaScript Includes (order matters) -->
    <%@include file="includes/common_js.jsp" %>
    <script src="../jslib/profiles/3x/libraries/underscore/underscore.js"></script>
    <script src="../jslib/profiles/3x/libraries/backbone/backbone.js"></script>
    <script src="../jslib/profiles/3x/jquery/libraries/backgridjs/backgrid.js"></script>
    <script src="../widgetbuilder/js/PercWidgetBuilder.js"></script>
    <script src="../widgetbuilder/js/PercWidgetBuilderExpander.js"></script>
    <script src="../widgetbuilder/js/PercWidgetBuilderService.js"></script>
    <script src="../widgetbuilder/js/PercWidgetDefsTable.js"></script>
    <script src="../widgetbuilder/js/models/PercWidgetBuilderDefinitionModel.js"></script>
    <script src="../widgetbuilder/js/models/PercWidgetFieldsModels.js"></script>
    <script src="../widgetbuilder/js/models/PercWidgetDisplayModels.js"></script>
    <script src="../widgetbuilder/js/models/PercWidgetResourceModels.js"></script>
    <script src="../widgetbuilder/js/views/PercWidgetBuilderDefinitionView.js"></script>
    <script src="../widgetbuilder/js/views/PercWidgetFieldsViews.js"></script>
    <script src="../widgetbuilder/js/views/PercWidgetDisplayViews.js"></script>
    <script src="../widgetbuilder/js/views/PercWidgetResourceViews.js"></script>
    <script src="../services/PercUserService.js"></script>
    <script src="../plugins/perc_ChangePwDialog.js"></script>
    <script src="../plugins/perc_ChangeUserEmailDialog.js"></script>
    <% } else { %>
    <script src="../jslibMin/perc_widgetBuilder.packed.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_widgetBuilder.packed.min.css"/>
    <% } %>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/lib/codemirror.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/addon/edit/matchbrackets.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/mode/xml/xml.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/mode/javascript/javascript.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/mode/css/css.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/mode/htmlmixed/htmlmixed.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/mode/velocity/velocity.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/addon/dialog/dialog.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/addon/search/searchcursor.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/addon/search/search.js"></script>
    <script src="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/addon/selection/active-line.js"></script>
    <link rel="stylesheet" href="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/lib/codemirror.css" />
    <link rel="stylesheet" href="/sys_resources/tinymce/plugins/codemirror/codemirror-4.8/addon/dialog/dialog.css"/>

    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="../css/IE_styles.css"/><![endif]-->
    <!--[if gte IE 8]>
    <link rel="stylesheet" type="text/css" href="../css/IE8_styles.css"/><![endif]-->
    <script>
        var dirtyController = $.PercDirtyController;
        function navigationEvent() {
            // if template is not dirty, return nothing and allow navigation
            // otherwise return alert message and display confirmantion box
            return dirtyController.navigationEvent();
        }
    </script>
</head>
<body onbeforeunload="return navigationEvent()" style="overflow:auto;">
<div class="perc-main">
    <div class="perc-header">
        <jsp:include page="includes/header.jsp" flush="true">
            <jsp:param name="mainNavTab" value="widgetbuilder"/>
        </jsp:include>
    </div>
    <div class='perc-wb-defs-outer' collapsed="true">
        <table class="perc-wb-list-toolbar">
            <tr>
                <td class="perc-wb-list-view-options">
                    <a id='perc-wb-defs-expander' role="button" tabindex="8" title='<i18n:message key = "perc.ui.workflow.view@Maximize"/>-<i18n:message key = "perc.ui.workflow.view@Minimize"/>' class="perc-font-icon icon-plus-sign fas fa-plus ui-state-enabled"></a>
                </td>
                <td class="perc-wb-list-menu">
                    <div class="perc-wb-list-buttonbar" role="toolbar" aria-label="Widget Builder Toolbar">
                        <a tabindex="13" id="perc-wb-button-delete" class="perc-font-icon icon-remove fas fa-minus ui-disabled"
                           title="Click to delete selected widget (DEL)" href="#" role="button"></a>
                        <a tabindex="12" id="perc-wb-button-new" class="perc-font-icon" href="#" title="Click to create new widget"
                           style="" role="button"><span class="icon-plus fas fa-plus"></span><span class="icon-file fas fa-file"></span></a>
                        <a tabindex="11" id="perc-wb-button-edit" class="perc-font-icon icon-edit fas fa-edit ui-disabled"
                           title="Click to edit selected widget (ENTER)" href="#" role="button"></a>
                        <a tabindex="10" id="perc-wb-button-deploy" class="perc-font-icon icon-download-alt fas fa-upload ui-disabled"
                           title="Click to deploy selected widget (SPC)" href="#" role="button"></a>
                        <a tabindex="9" id="perc-wb-button-package-manager" class="perc-font-icon icon-folder-open fas fa-folder-open"
                           title="Click to access Package Manager" href="/cm/packages" role="button" target="_blank" rel = "noopener noreferrer"></a>
                    </div>
                </td>
            </tr>
        </table>
        <div id="perc-wb-defs-container" style="display:none">
        </div>
    </div>
    <div id="perc-widget-def-tabs">
        <div class="perc-widget-name">
        </div>
        <ul role="tablist">
            <li role="presentation" class="WBTabs">
                <a id="perc-tab-widget-general" role="button" tabindex="122" title='<i18n:message key = "perc.ui.folder.properties.dialog@General"/>' class="perc-widget-def-tab-link"
                   href="#perc-widget-tab-general" role="tab"><i18n:message key = "perc.ui.folder.properties.dialog@General"/></a>
            </li>
            <li role="presentation" class="WBTabs" onclick="javascript:setRowIndexOnContentData_1();">
                <a id="perc-tab-widget-content"  role="button" tabindex="123" title='<i18n:message key = "perc.ui.widget.builder@Content"/>' class="perc-widget-def-tab-link"
                   href="#perc-widget-tab-content" role="tab"><i18n:message key = "perc.ui.widget.builder@Content"/></a>
            </li>
            <li role="presentation" class="WBTabs" >
                <a  id="perc-tab-widget-resources" role="button" tabindex="124" title='<i18n:message key = "perc.ui.widget.builder@Resources"/>' class="perc-widget-def-tab-link" href="#perc-widget-tab-resources" role="tab"><i18n:message key = "perc.ui.widget.builder@Resources"/></a>
            </li>
            <li role="presentation" class="WBTabs" onclick="javascript:setOnClickForDisplayAutoGenerate();">
                <a id="perc-tab-widget-display"role="button" tabindex="125" title='<i18n:message key = "perc.ui.widget.builder@Display"/>' class="perc-widget-def-tab-link"
                   href="#perc-widget-tab-display" role="tab"><i18n:message key = "perc.ui.widget.builder@Display"/></a>
            </li>
        </ul>
        <div id="perc-widget-def-action-bar">
            <div id="perc-widget-menu" style='position:relative;'>
                <div id="perc-widget-menu-buttons" style="display:none">
                    <button id="perc-widget-save" tabindex="127" title='<i18n:message key ="perc.ui.button@Save"/>' class="btn btn-primary" name="perc-widget-save" style="float: right;margin-right:10px;"><i18n:message key ="perc.ui.button@Save"/> </button>
                    <button id="perc-widget-close" tabindex="126" title='<i18n:message key ="perc.ui.common.label@Cancel"/>' class="btn btn-primary" name="perc-widget-close" style="float: right;"><i18n:message key ="perc.ui.common.label@Cancel"/> </button>
                </div>
            </div>
        </div>
        <div id="perc-pageEditor-toolbar-content" class="ui-helper-clearfix">
        </div>
        <div id="perc-widget-tab-general" class="perc-widget-editing-container">
        </div>
        <div id="perc-widget-tab-content" class="perc-widget-editing-container">
            <div id="perc-widget-fields-top-container">
                <div class="perc-add-field-button-row"><span class="perc-group-header"><span
                        class="ui-widget-content-header"><i18n:message key = "perc.ui.widget.builder@Content Editing Fields"/></span></span><span tabindex="139"
                        class="perc-add-field-button"
                        style="float:left; margin-left:754px;margin-bottom:8px;display:inline-block;cursor:pointer;"
                        onclick="WidgetBuilderApp.showFieldEditor(null);"><i18n:message key = "perc.ui.widget.builder@Add Field"/></span></div>
                <div style="clear:both"></div>
                <div class="perc-widget-field-header">
                    <div class="perc-widget-field-entry"><i18n:message key = "perc.ui.widget.builder@Name"/></div>
                    <div class="perc-widget-field-entry"><i18n:message key = "perc.ui.widget.builder@Label"/></div>
                    <div class="perc-widget-field-entry"><i18n:message key = "perc.ui.widget.builder@Type"/></div>
                    <div class="perc-widget-field-entry"><i18n:message key = "perc.ui.widget.builder@Actions"/></div>
                </div>
                <div id="perc-widget-fields-container">
                </div>
            </div>
        </div>
        <div id="perc-widget-tab-resources" class="perc-widget-editing-container">
            <div id="perc-widget-js-resources-top-container" style="width:384px;margin-bottom:20px;"
                 class="resource-tab-border">
                <div class="perc-add-resource-button-row">
                    <div class="perc-group-header">
                        <div class="ui-widget-content-header"><i18n:message key = "perc.ui.widget.builder@Javascript Resources"/></div>
                    </div>
                    <div id="perc-add-js-resource-button" title="Add Javascript Resource" role="button" tabindex="171"
                         class="perc-add-resource-button" onclick="WidgetBuilderApp.addNewResource('JS');">
                        <span class="perc-font-icon resource-tab-button-background"><span
                                class="font-icon-size icon-plus fas fa-plus"></span><span class="icon-file-alt fas fa-file-alt"></span></span></div>
                </div>
                <div style="clear:both"></div>
                <div id="perc-widget-js-resources-container"></div>
            </div>
            <div id="perc-widget-css-resources-top-container" style="width:384px;margin-bottom:20px;"
                 class="resource-tab-border">
                <div class="perc-add-resource-button-row">
                    <div class="perc-group-header">
                        <div class="ui-widget-content-header"><i18n:message key = "perc.ui.widget.builder@CSS Resources"/></div>
                    </div>
                    <div id="perc-add-css-resource-button" title="Add CSS Resource" class="perc-add-resource-button" role="button" tabindex="191"
                         onclick="WidgetBuilderApp.addNewResource('CSS');"><span
                            class="perc-font-icon resource-tab-button-background"><span
                            class="font-icon-size icon-plus fas fa-plus"></span><span class="icon-file-alt fas fa-file-alt"></span></span></div>
                </div>
                <div style="clear:both"></div>
                <div id="perc-widget-css-resources-container"></div>
            </div>
        </div>
        <div id="perc-widget-tab-display" class="perc-widget-editing-container">
            <div id="perc-widget-display-top-container" style="width:530px;margin-bottom:20px;">
                <div class="perc-generate-display-button-row">
                    <div class="perc-group-header">
                        <div class="ui-widget-content-header"><i18n:message key = "perc.ui.widget.builder@Display HTML"/></div>
                    </div>
                    <div id="perc-display-html-auto-generate-button" class="perc-display-html-auto-generate-button" role="button" tabindex="142"
                         onclick="WidgetBuilderApp.autoGenerateHtml();"><i18n:message key = "perc.ui.widget.builder@Auto Generate"/>
                    </div>
                </div>
                <div style="clear:both"></div>
                <div id="perc-widget-display-html-container"></div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
