/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

var timerid;
var navMgr = $j.PercNavigationManager;
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

$(document).ready(function() {
    bindNavigationEvents();
    password = $.PercChangePasswordMinuet;
});

function bindNavigationEvents() {

    $('.perc-nav-item').on('click keypress', function(event) {
        if(event.type === 'click' || event.which === 13) {
            processNavigationRequest(this);
        }
    });

    $('#percToggleNavigation').on("click", function() {
        toggleNavigation();
    });

    $('.perc-toggle-about').on("click", function() {
        toggleAbout();
    });

    $('.perc-toggle-password').on("click", function() {
        password.togglePassword();
        password.clearPasswordFields();
    });

    $('.perc-change-password-field').on("keyup", function() {
        password.validateNewPassword();
    });

    $('.perc-submit-password-change').on("click", function() {
        password.submitNewPassword();
    });

}

function updateNavLocation(newSiteName, newPath) {
    updateQueryStringParam('site', newSiteName);
    $j.PercNavigationManager.setSiteName(newSiteName);
    updateQueryStringParam('path', encodeURIComponent(newPath));
    $j.PercNavigationManager.setPath(newPath);
}

function processNavigationRequest(eventObj) {
    var view = $(eventObj).data('navmgr');
    var navMgrArguments = [navMgr[view], navMgr.getSiteName(), navMgr.getMode(), null, navMgr.getName(), navMgr.getPath(), navMgr.getPathType(), null];
    navMgr.goToLocation.apply(navMgr, navMgrArguments);
}

function resetNavigation() {
    $('#percNavigationBody').scrollTop('#percNavigationBody');
    $('#percAbout').hide();
    password.clearPasswordFields();
    $('#percPasswordDialogTarget').hide();
    $('#percNavigationBody').hide();
}

function toggleNavigation() {
    if( $('#percNavigationBody').is(":hidden") ) {
        $('#percNavigationBody').show();
        $('#percNavigationBody').modal('_enforceFocus');
        navigationEscapeListener(true);
        $('#percNavigationBody').animateCss('slideInDown faster');
        $('#percNavigationBody').trigger("focus");
    }
    else {
        $('#percNavigationBody').animateCss('slideOutUp faster', function() {
            resetNavigation();
            $('#percToggleNavigation').trigger("focus");
        });
    }
}

/*  This method activates and deactivates the escape
*   key listener for the navigation toggle. This will prevent
*   the navigation from being accidentally closed via keydown
*   while other overalays are open on top of the nav
*/
function navigationEscapeListener(action) {
    if(action === true) {
        $('#percNavigationBody').on('keydown', function(event) {
            if(event.key === 'Escape') {
                $('#percToggleNavigation').trigger('click');
            }
        });
    }
    else {
        $('#percNavigationBody').off('keydown');
    }
}

function toggleAbout() {
    if( $('#percAbout').is(":hidden") ) {
        navigationEscapeListener(false);
        $('#percAbout').show();
        $('#percAbout').modal('_enforceFocus');
        $('#percAbout').animateCss('fadeIn faster');
    }
    else {
        $('#percAbout').animateCss('fadeOut faster', function() {
            $('#percAbout').hide();
            $('#percNavigationBody').modal('_enforceFocus');
            navigationEscapeListener(true);
            $('.perc-toggle-about').trigger("focus");
        });
    }
}
