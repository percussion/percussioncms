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

// assumed dependencies:
//  - require.js

define (
    // prerequisites:
    [
        'pages/cm1adaptor',
        'text!widgets/basedialog/basedialog.html',
        'widgets/basedialog/basedialog.viewmodel',
        'widgets/addwizard/addwizard',
        'widgets/search/search',
        'widgel-base',
        'jquery-ui',
        'jquery-ui/ui/dialog'
    ],
    // module:
    function (cm1Adaptor, defaultView, defaultViewModel, addwizard, search) {
        var widgetName = 'cui.basedialog';
        $.widGEL.baseWidget.injectCssFile('twitter.bootstrap', requirejs.toUrl('bootstrap'));
        $.widGEL.baseWidget.injectCssFile('twitter.bootstrap.theme', requirejs.toUrl('bootstrap-theme'));
        $.widGEL.baseWidget.injectCssFile('widgets.basedialog', requirejs.toUrl('widgets/basedialog/basedialog.css'));
        // $.widGEL.baseWidget.injectCssFile('jquery', requirejs.toUrl('css/jquery-ui-1.10.4.custom.css')); since this file does not exist
        $.widGEL.baseWidget.injectCssFile('font.awesome', requirejs.toUrl('fontawesome-css'));
        $.widGEL.baseWidget.injectCssFile('perc.css', requirejs.toUrl('perc-css'));

        $.widget(widgetName, $.widGEL.baseWidget, {
            options: {
                view: defaultView,
                viewModel: defaultViewModel,
                cm1Adaptor:cm1Adaptor,
                debug: false
            },

            destroy: function () { },

            // non-private properties of the ViewModel are part of the public widget API.
            // see Widgets/Shared/BaseWidget.Model for external usage
            _createDefaultViewModel: function () {
                var thisWidget = this;
                var viewModel = new thisWidget.options.viewModel(thisWidget.options);
                return viewModel;
            },

            _create: function () {
                var thisWidget = this;
                thisWidget._initModelView();

                var $dialogContainer = $("#add-wizard-dialog");
                $dialogContainer.addwizard();

                $dialogContainer = $("#search-dialog");
                $dialogContainer.search();

                setTimeout(function () {
                    var viewModel = thisWidget._viewModel;
                    viewModel.init();
                }, 10);
            }
        });

        return "SUCCESS: " + widgetName + " Widget Registered.";
    }
);
