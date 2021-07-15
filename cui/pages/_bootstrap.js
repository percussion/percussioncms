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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

var requireJsConfig = {
    baseUrl: '',
    //urlArgs: "bust=build-1.2.34",
    paths: {
        'components': '/cm/cui/components',
        'text': '/cm/cui/components/requirejs-text/text',
        'jquery': '/cm/jslib/profiles/3x/jquery/jquery-3.6.0',
        'jquery-migrate': '/cm/jslib/profiles/3x/jquery/jquery-migrate-3.3.2',
        'jquery-ui': '/cm/cui/components/jquery-ui/jquery-ui',
        'jquery-ui/widget': '/cm/cui/components/jquery-ui/ui/widget',
        "jquery-ui/data":'/cm/cui/components/jquery-ui/ui/data',
        "jquery-ui/disable-selection":"/cm/cui/components/jquery-ui/ui/disable-selection",
        "jquery-ui/form-reset-mixin":"/cm/cui/components/jquery-ui/ui/form-reset-mixin",
        "jquery-ui/focusable":"/cm/cui/components/jquery-ui/ui/focusable",
        "jquery-ui/form":"/cm/cui/components/jquery-ui/ui/form",
        "jquery-ui/ie":"/cm/cui/components/jquery-ui/ui/ie",
        "jquery-ui/keycode":"/cm/cui/components/jquery-ui/ui/keycode",
        "jquery-ui/labels":"/cm/cui/components/jquery-ui/ui/labels",
        "jquery-ui/jquery-patch.js":"/cm/cui/components/jquery-ui/ui/jquery-patch.js",
        "jquery-ui/plugin":"/cm/cui/components/jquery-ui/ui/plugin",
        "jquery-ui/position":"/cm/cui/components/jquery-ui/ui/position",
        "jquery-ui/safe-active-element":"/cm/cui/components/jquery-ui/ui/safe-active-element",
        "jquery-ui/safe-blur":"/cm/cui/components/jquery-ui/ui/safe-blur",
        "jquery-ui/scroll-parent":"/cm/cui/components/jquery-ui/ui/scroll-parent",
        "jquery-ui/tabbable":"/cm/cui/components/jquery-ui/ui/tabbable",
        "jquery-ui/unique-id":"/cm/cui/components/jquery-ui/ui/unique-id",
        "jquery-ui/version":"/cm/cui/components/jquery-ui/ui/version",
        "jquery-ui/ui/tooltip":"/cm/cui/components/jquery-ui/ui/widgets/tooltip",
        "jquery-ui/ui/accordian":"/cm/cui/components/jquery-ui/ui/widgets/accordian",
        "jquery-ui/ui/autocomplete":"/cm/cui/components/jquery-ui/ui/widgets/autocomplete",
        "jquery-ui/ui/checkboxradio":"/cm/cui/components/jquery-ui/ui/widgets/checkboxradio",
        "jquery-ui/ui/controlgroup":"/cm/cui/components/jquery-ui/ui/widgets/controlgroup",
        "jquery-ui/ui/datepicker":"/cm/cui/components/jquery-ui/ui/widgets/datepicker",
        "jquery-ui/ui/draggable":"/cm/cui/components/jquery-ui/ui/widgets/draggable",
        "jquery-ui/ui/dialog":"/cm/cui/components/jquery-ui/ui/widgets/dialog",
        "jquery-ui/ui/button":"/cm/cui/components/jquery-ui/ui/widgets/button",
        "jquery-ui/ui/droppable":"/cm/cui/components/jquery-ui/ui/widgets/droppable",
        "jquery-ui/ui/menu":"/cm/cui/components/jquery-ui/ui/widgets/menu",
        "jquery-ui/ui/mouse":"/cm/cui/components/jquery-ui/ui/widgets/mouse",
        "jquery-ui/ui/progressbar":"/cm/cui/components/jquery-ui/ui/widgets/progressbar",
        "jquery-ui/ui/resizable":"/cm/cui/components/jquery-ui/ui/widgets/resizable",
        "jquery-ui/ui/selectable":"/cm/cui/components/jquery-ui/ui/widgets/selectable",
        "jquery-ui/ui/selectmenu":"/cm/cui/components/jquery-ui/ui/widgets/selectmenu",
        "jquery-ui/ui/slider":"/cm/cui/components/jquery-ui/ui/widgets/slider",
        "jquery-ui/ui/sortable":"/cm/cui/components/jquery-ui/ui/widgets/sortable",
        "jquery-ui/ui/spinner":"/cm/cui/components/jquery-ui/ui/widgets/spinner",
        "jquery-ui/ui/tabs":"/cm/cui/components/jquery-ui/ui/widgets/tabs",
        'knockout': '/cm/cui/components/knockoutjs/dist/knockout',
        'pubsub': '/cm/cui/components/pubsub-js/src/pubsub',
        'dynatree': '/cm/jslib/profiles/3x/jquery/plugins/jquery-dynatree/jquery.dynatree',
        'widgel-base': '/cm/cui/components/widgel/dist/widgel-base',
        'perc-utils': '/cm/cui/components/perc-utils/dist',
        'perc-css': '/cm/cui/components/perc-css/perc.css',
        'opensans-css': '/cm/cui/components/google-fonts/opensans.css',
        'montserrat-css': '/cm/cui/components/google-fonts/montserrat.css',
        'dynatree-css':'/cm/cui/components/dynatree/skin/ui.dynatree.css',
        'widgets': '/cm/cui/widgets',
        'modules': '/cm/cui/modules',
        'test'   : '/cm/cui/test',
        'utils'  : '/cm/cui/pages/utils',
        'css': 'css',
        'fontawesome-css': '/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css',
        'bootstrap':'/cm/jslib/profiles/3x/libraries/bootstrap/css/bootstrap.min.css',
        'bootstrap-theme':'/cm/cui/components/twitter-bootstrap-3.0.0/dist/css/bootstrap-theme.min.css'
    },
    shim: {
        'knockout': {
            exports: 'ko'
        },
        'jquery-ui': {
            exports: '$',
            deps: ['jquery']
        },
        'dynatree': {
            deps: ['jquery-ui']
        }
    }
};

if (typeof(exports) !== 'undefined' && exports !== null) {
    exports.requireJsConfig = requireJsConfig;
}
