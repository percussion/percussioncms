<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.percussion.utils.container.PSContainerUtilsFactory" %>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ page import="com.percussion.utils.container.IPSConnector" %>
<%@ page import="com.percussion.server.PSServer" %>
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


    // IPSConnector connector = PSContainerUtilsFactory.getInstance().getConnectorInfo().getHttpConnector().get();


    int nonSslPort = request.getServerPort();//connector.getPort();
    String hostAddress = request.getServerName();//connector.getCallbackHost();
    String hostScheme = request.getScheme();
    String debug = request.getParameter("debug");

    if(PSServer.isRequestBehindProxy(request)) {
        nonSslPort  = Integer.valueOf(PSServer.getProperty("proxyPort",""+nonSslPort));
        hostScheme  = PSServer.getProperty("proxyScheme", "http");
        hostAddress = PSServer.getProperty("publicCmsHostname", "localhost");
        if(nonSslPort == 80 || nonSslPort == 443){
            nonSslPort = -1;
        }
    }
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    String site = request.getParameter("site");
    if (site == null)
        site = "";
    if (debug == null)
        debug = "false";

    String fakeData = request.getParameter("fakeData");
    if (fakeData == null)
        fakeData = "false";
    else
        fakeData = "true";

    String trafficScale = request.getParameter("trafficScale");
    if (trafficScale == null)
        trafficScale = "30";
%>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%=debug%>"/>
<!DOCTYPE html>
<html lang="<%=lang %>">
<head>
    <title><i18n:message key="perc.ui.dashboard.title@Dashboard"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!--Meta Includes -->
    <%@include file="includes/common_meta.jsp" %>
    <meta http-equiv="Pragma" content="no-cache">
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
    <link rel="stylesheet" type="text/css" href="/cm/gadgets/repository/PercBlogsGadget/PercNewBlogDialog.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%= locale%>"></script>

    <!-- Set javascript variable to get it in other js files -->
    <script>
        var hostAddress = "<%=hostAddress%>";
        var nonSslPort = <%=nonSslPort%>;
        var hostScheme = "<%=hostScheme%>";
    </script>

    <% if (isDebug) { %>

    <!-- CSS Includes -->
    <%@include file="includes/common_css.jsp" %>

    <link type="text/css" href="../css/perc_css_editor.css" rel="stylesheet"/>
    <link type="text/css" href="../css/styles.css" rel="stylesheet"/>
    <link type="text/css" href="../css/perc_template_layout.css" rel="stylesheet"/>
    <link type="text/css" href="../css/perc_mcol.css" rel="stylesheet"/>
    <link type="text/css" href="../css/perc_decoration.css" rel="stylesheet"/>
    <link type="text/css" href="../jslib/profiles/3x/jquery/plugins/jquery-perc-retiredjs/jquery.tooltip.css" rel="stylesheet"/>
    <!-- Stuff needed for finder to work like Editor -->
    <link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css"/>
    <link rel="stylesheet" type="text/css" href="/cm/gadgets/repository/PercBlogsGadget/PercNewBlogDialog.css"/>
    <link rel="stylesheet" type="text/css" href="/cm/widgets/PercFinderTree/PercFinderTree.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css"/>

    <!-- JavaScript Includes (order matters) -->
    <%@include file="includes/common_js.jsp" %>
    <%@include file="includes/finder_js.jsp" %>


    <!-- Start Dashboard CSS -->
    <link type="text/css" href="../css/perc_mcol.css" rel="stylesheet"/>
    <link type="text/css" href="../css/styles.css" rel="stylesheet"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css"/>
    <link rel="../widgets/PercWizard/PercWizard.css"/>
    <link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css"/>
    <!-- Shindig { -->
    <link rel="stylesheet" type="text/css" media="screen" href="../gadgets/container/gadgets.css"/>
    <link rel="stylesheet" type="text/css" media="screen" href="../css/perc_dashboard.css"/>

    <!--[if IE]>
    <link rel="stylesheet" type="text/css" media="screen" href="../css/perc_dashboard_IE.css"/>
    <![endif]-->

    <!-- End Dashboard CSS -->
    <!-- Start Dashboard JS -->
    <script src="../gadgets/js/rpc.js"></script>
    <script src="../gadgets/container/cookies.js"></script>
    <script src="../gadgets/container/util.js"></script>
    <script src="../gadgets/container/gadgets.js"></script>
    <script src="../gadgets/container/serverbaseduserprefstore.js"></script>


    <script src="../widgets/PercAutoScroll.js"></script>
    <script src="../views/PercDashboard.js"></script>
    <script src="../services/perc_sectionServiceClient.js"></script>
    <script src="../services/PercServiceUtils.js"></script>
    <script src="../services/PercSiteService.js"></script>
    <script src="../services/PercFormService.js"></script>
    <script src="../services/PercCookieConsentService.js"></script>
    <script src="../services/PercPathService.js"></script>
    <script src="../services/PercMetadataService.js"></script>
    <script src="../services/PercDashboardService.js"></script>
    <script src="../services/PercActivityService.js"></script>
    <script src="../services/PercReusableSearchService.js"></script>
    <script src="../widgets/PercWizard/PercWizard.js"></script>
    <script src="../plugins/perc_ChangePwDialog.js"></script>
    <script src="../plugins/perc_newsitedialog.js"></script>
    <!-- End Dashboard JS -->
    <% } else { %>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_dashboard.packed.min.css"/>

    <!-- Shindig { -->
    <link rel="stylesheet" type="text/css" media="screen" href="../gadgets/container/gadgets.css"/>
    <link rel="stylesheet" type="text/css" media="screen" href="../css/perc_dashboard.css"/>

    <!--[if IE]>
    <link rel="stylesheet" type="text/css" media="screen" href="../css/perc_dashboard_IE.css"/>
    <![endif]-->

    <script src="../gadgets/js/rpc.js"></script>
    <script src="../gadgets/container/cookies.js"></script>
    <script src="../gadgets/container/util.js"></script>
    <script src="../gadgets/container/gadgets.js"></script>
    <script src="../gadgets/container/serverbaseduserprefstore.js"></script>
    <!-- } Shindig -->

    <script src="../jslibMin/perc_dashboard.packed.min.js"></script>
    <% } %>
    <script>
        window.addEventListener('DOMContentLoaded', (event) => {

            var fakeData = <%=fakeData%>;
            var trafficScale = <%=trafficScale%>;

            //Finder initialization code
            $(document).ready(function () {
                $.Percussion.PercFinderView();

                // update bottom DIV on window resize
                window.onresize = function () {

                    // compute and dynamically set the size of the bottom DIV
                    // so that scrobars appear as needed
                    fixBottomHeight();

                    // mcol-path-summary is the input field right above the miller column. It shows the full path selected in the finder
                    // the size should always be smaller than the enclosing DIV .perc-finder. Here we subtract 50px to make sure it is smaller
                    //$perc-jquery("#mcol-path-summary").width($perc-jquery("#mcol-path-summary").parent().width() - ($perc-jquery("#mcol-path-summary").outerWidth(true) - $perc-jquery("#mcol-path-summary").width()) - $perc-jquery("#perc-finder-go-action").outerWidth(true) - 50);
                };

                // set bottom DIVs height at load time
                fixBottomHeight();

                dashOptions = {columns: ["columnWest", "columnCenter", "columnEast"]};
                $dashboard = $.PercDashboard(dashOptions);
                $dashboard.load("/Rhythmyx/services/dashboardmanagement/dashboard/", function () {
                    $dashboard.setupTray();
                    $dashboard.showSplashDialog();
                });
            });
        });
    </script>


    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="../css/IE_styles.css"/><![endif]-->
    <!--[if gte IE 8]>
    <link rel="stylesheet" type="text/css" href="../css/IE8_styles.css"/><![endif]-->
</head>


<!-- overflow hidden because we only want the bottom publish UI to scroll, not the finder -->
<body style="overflow : hidden">

<!-- position relative so that publish UI flows right after the finder as the next block -->
<div class="perc-main perc-finder-fix">
    <jsp:include page="includes/header.jsp" flush="true">
        <jsp:param name="mainNavTab" value="dashboard"/>
    </jsp:include>
    <div class="ui-layout-north" style="padding: 0px 0px; overflow: visible">
        <jsp:include page="includes/finder.jsp" flush="true">
            <jsp:param name="openedObject" value="PERC_SITE"/>
        </jsp:include>
    </div>
</div>

<div class="perc-dashboard-toolbar-container">

    <!-- Dashboard Toolbar -->

    <!-- Dashboard Tray -->

    <div id="perc-dashboard-gadget-tray" class="perc-tray perc-dashboard-gadget-tray">
        <div class="perc-dashboard-gadget-toolbar">
            <div id="perc-dashboard-gadget-tray-expander" class="perc-tray-expander perc-tray-expander-collapsed">
                <a href="#" class="perc-tray-expander-label" onclick="return false;"> <i class="icon-cogs fas fa-cogs"></i><i18n:message key="perc.ui.dashboard@Add Dashboard Gadgets"/></a>
            </div>
            <div class="perc-gadget-filter">
                <label><i18n:message key = "perc.ui.dashboard@Type"/></label>
                <select class="perc-gadget-type">
                    <option value="all"><i18n:message key = "perc.ui.dashboard@View All"/></option>
                    <option selected="true" value="percussion">Percussion</option>
                    <option value="custom"><i18n:message key = "perc.ui.dashboard@Custom"/></option>
                </select>
                <label><i18n:message key = "perc.ui.dashboard@Category"/></label>
                <select class="perc-gadget-category">
                    <option class="perc-gadget-category-default" selected="true" value="all"><i18n:message key = "perc.ui.dashboard@View All"/></option>
                    <option class="perc-gadget-category-predefined" value="analytics"><i18n:message key = "perc.ui.dashboard@Analytics"/></option>
                    <option class="perc-gadget-category-predefined" value="blog"><i18n:message key = "perc.ui.dashboard@Blog"/></option>
                    <option class="perc-gadget-category-predefined" value="content"><i18n:message key = "perc.ui.dashboard@Content"/></option>
                    <option class="perc-gadget-category-predefined" value="integration"><i18n:message key = "perc.ui.dashboard@Integration"/></option>
                    <option class="perc-gadget-category-predefined" value="search"><i18n:message key = "perc.ui.dashboard@Search"/></option>
                    <option class="perc-gadget-category-predefined" value="social"><i18n:message key = "perc.ui.dashboard@Social"/></option>
                </select>
            </div>
            <div id="perc-dashboard-restore-menu" class="perc-dashboard-restore-menu" href="#"
                 title="<i18n:message key="perc.ui.dashboard@Restore Default Dashboard"/>" onclick="return false;">
                <a href="#" class="perc-dashboard-restore-label"><i class="icon-dashboard fas fa-chart-line"></i><i18n:message key="perc.ui.dashboard@Reset"/></a>
            </div>
        </div>
        <div class="perc-dashboard-gadget-list-container">
            <div id="perc-dashboard-gadget-list" class="perc-tray-list">
                <div class="perc-tray-item" id="perc-tray-gadget-template" url="" name="" style="display : none">
                    <div class="perc-tray-item-icon"></div>
                    <div class="perc-tray-item-label">
                        <div class="perc-text-overflow"><i18n:message key = "perc.ui.dashboard@Generic Widget"/></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="perc-dashboard-gadget-drop" class="perc-dashboard-gadget-drop">
        <div id="perc-dashboard-gadget-highlight" class="perc-dashboard-gadget-highlight">&nbsp;</div>
    </div>

</div>

<div class="perc-body-background perc-dashboard-container" style="margin-top : 0px">

    <!-- bottom div showing the publish UI will scroll independently -->
    <div class='perc-whitebg' style="overflow : hidden">
        <div class="perc-dashboard" style="min-width : 990px">
            <table style="width: 100%;">
                <tr style="overflow : hidden">
                    <td style="vertical-align: top; width: 34%;">
                        <div id="col-0" class="perc-gadget-column" style="min-width:100%"></div>
                    </td>
                    <td style="vertical-align: top; width: 66%;">
                        <div id="col-1" class="perc-gadget-column" style="min-width:100%"></div>
                    </td>
                </tr>
            </table>
        </div>

        <div id="perc-gadget-menu" class="box_shadow_with_padding">
            <div class="perc-gadget-menu-items">
                <div class="perc-gadget-menu-item" id="perc-gadget-menu-config"><i18n:message key = "perc.ui.dashboard@Edit Settings"/></div>
                <div class="perc-gadget-menu-item" id="perc-gadget-menu-remove"><i18n:message key = "perc.ui.dashboard@Delete"/></div>
                <div class="perc-gadget-menu-item" id="perc-gadget-menu-minimize"><i18n:message key = "perc.ui.dashboard@Minimize"/></div>
                <div class="perc-gadget-menu-item" id="perc-gadget-menu-expand"><i18n:message key = "perc.ui.dashboard@Expand"/></div>
            </div>
        </div>
    </div>

    <%@include file='includes/siteimprove_integration.html'%>

</div>

</body>
</html>
