/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    $.PercNavigationManager.setSiteName(newSiteName);
    updateQueryStringParam('path', encodeURIComponent(newPath));
    $.PercNavigationManager.setPath(newPath);
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
