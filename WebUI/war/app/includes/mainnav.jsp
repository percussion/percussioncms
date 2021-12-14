<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

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
    String mainNavTab = request.getParameter("mainNavTab");
    String debug = request.getParameter("debug");
    String thesite = request.getParameter("site");
    if(thesite == null)
        thesite = "";
    boolean isDebug = "true".equals(debug);
    boolean isAdmin = (Boolean)request.getAttribute("isAdmin");
    boolean isDesigner = (Boolean)request.getAttribute("isDesigner");
    String wdgBuilderParam = (String)request.getAttribute("isWidgetBuilderActive");
    boolean isWdgActive = "true".equalsIgnoreCase(wdgBuilderParam.trim());
    String debugQueryString = isDebug ? "&debug=true" : "";
%>
<script>
    function goToLocation(l)    {
        window.location.href = l;
    }

    jQuery(function ($) { $(function () {

        // Dynamically reverse the z-index stacking order, this is done to get
        // around IE issue with z-index stacking. Bug# CML-2032
        var zIndexNumber = 5000; // The starting stacking z-index
        // Note Dialogs maust have a greater z-index then
        // this number

        $('div').each(function() {
            if(!$(this).hasClass('ui-dialog') && !$(this).hasClass('ui-widget-overlay'))
            {
                $(this).css('z-index', zIndexNumber);
                zIndexNumber -= 10;
            }
        });

        var timerid;
        var navMgr = $.PercNavigationManager;
        var currentView = '<%=mainNavTab%>';
        var views = {
            home: 'VIEW_HOME',
            dashboard: 'VIEW_DASHBOARD',
            pageeditor: 'VIEW_EDITOR',
            architecture: 'VIEW_SITE_ARCH',
            siteadmin: 'VIEW_DESIGN',
            publish: 'VIEW_PUBLISH',
            workflow: 'VIEW_WORKFLOW',
            widgetbuilder: 'VIEW_WIDGET_BUILDER'
        };
        function clear (evt) {
            clearTimeout(timerid);
        }
        function hide (evt) {
            clear();
            timerid = setTimeout(function () {
                $('.perc-topnav .perc-actions-menu').hide();
            }, 800);
        }
        function set_label ($selected) {
            var val = $selected.html();
            $selected.addClass('ui-state-active');
            $('.perc-topnav label').html(val.toUpperCase()).attr("name",val.toUpperCase());
        }
        // initialize the current view label
        var selector = '[data-navmgr=' + views[currentView] + ']';
        set_label($(selector));
        $('body').on('click', function (evt) {
            $(evt.target).find('.perc-topnav .perc-actions-menu').hide();
        }).on('click', '.perc-topnav', function onTopNavClick (event) {
            clear();
            event.stopPropagation();
            $(this).find('.perc-actions-menu').show();
        }).on('click', '.perc-topnav li', function onTopNavOptionClick (event) {
            event.stopPropagation();
            var view = $(this).data('navmgr');
            var navMgrArguments = [navMgr[view], navMgr.getSiteName(), navMgr.getMode(), null, navMgr.getName(), navMgr.getPath(), navMgr.getPathType(), null];
            navMgr.goToLocation.apply(navMgr, navMgrArguments);
            $(this).parents('.perc-actions-menu').hide();
        });
        $('.perc-topnav, .perc-topnav .perc-actions-menu').on("mouseenter",
            function(evt){
                clear(evt);
            }).on("mouseleave",
            function(evt) {
                hide(evt);
            });
    });
    });
    /*]]>*/
</script>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<div class="perc-topnav" role="navigation" aria-label="<i18n:message key="perc.ui.navMenu.topnav@Top Navigation"/>">
    <label></label><span class="icon-chevron-down fas fa-chevron-down" role="presentation"></span>
    <ul id="perc-top-menu-bar" class="perc-actions-menu box_shadow_with_padding" role="menubar" aria-label="<i18n:message key="perc.ui.navMenu.topNav@Top Navigation"/>">
        <li role="menuitem" class="perc-actions-menu-item" data-navmgr="VIEW_HOME"><i18n:message key="perc.ui.navMenu.home@Home"/></li>
        <li role="menuitem" class="perc-actions-menu-item" data-navmgr="VIEW_DASHBOARD"><i18n:message key="perc.ui.navMenu.dashboard@Dashboard"/></li>
        <li role="menuitem" class="perc-actions-menu-item" data-navmgr="VIEW_EDITOR"><i18n:message key="perc.ui.navMenu.webmgt@Editor"/></li>
        <% if (isAdmin || isDesigner) { %>
        <li role="menuitem" class="perc-actions-menu-item" data-navmgr="VIEW_SITE_ARCH"><i18n:message key="perc.ui.navMenu.architecture@Architecture"/></li>
        <li role="menuitem" class="perc-actions-menu-item" data-navmgr="VIEW_DESIGN"><i18n:message key="perc.ui.navMenu.design@Design"/></li>
        <li role="menuitem" class="perc-actions-menu-item" data-navmgr="VIEW_PUBLISH"><i18n:message key="perc.ui.navMenu.publish@Publish"/></li>
        <% } %><% if (isAdmin) { %>
        <li role="menuitem" class="perc-actions-menu-item" data-navmgr="VIEW_WORKFLOW"><i18n:message key="perc.ui.navMenu.admin@Administration"/></li>
        <% } %>
        <% if (isWdgActive && (isAdmin || isDesigner)) { %>
        <li role="menuitem" class="perc-actions-menu-item" data-navmgr="VIEW_WIDGET_BUILDER"><i18n:message key="perc.ui.navMenu.admin@Widget Builder"/></li>
        <% } %>
    </ul>
</div>
