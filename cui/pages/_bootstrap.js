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

var requireJsConfig = {
    baseUrl: '',
    //urlArgs: "bust=build-1.2.34",
    paths: {
        'components': '/cm/cui/components',
        'text': '/cm/cui/components/requirejs-text/text',
        'jquery': '/cm/cui/components/jquery/dist/jquery.min',
        'jquery-ui': '/cm/cui/components/jquery-ui/ui/minified/jquery-ui.custom.min',
        'knockout': '/cm/cui/components/knockoutjs/dist/knockout',
        'pubsub': '/cm/cui/components/pubsub-js/src/pubsub',
		'dynatree': '/cm/cui/components/dynatree/jquery.dynatree.min',
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
        'fontAwesome': '/cm/cui/components/font-awesome-4.1.0/css/font-awesome.min.css',
        //'bootstrap':'/cm/cui/components/twitter-bootstrap-3.0.0/dist/css/bootstrap.min.css',
        'bootstrap':'../jslib/profiles/3x/libraries/bootstrap/css/bootstrap.min.css',
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
}

if (typeof(exports) !== 'undefined' && exports !== null) {
	exports.requireJsConfig = requireJsConfig;
}
