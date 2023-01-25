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

// assumed dependencies:
//  - require.js

define (
    // prerequisites:
    [
		'pages/cm1adaptor',
        'text!widgets/pagewizard/pagewizard.html',
        'widgets/pagewizard/pagewizard.viewmodel',
        'widgel-base'
    ],
    // module:
    function (cm1Adaptor, defaultView, defaultViewModel) {
        var widgetName = 'cui.pagewizard';
		$.widGEL.baseWidget.injectCssFile('twitter.bootstrap', requirejs.toUrl('bootstrap'));
        $.widGEL.baseWidget.injectCssFile('twitter.bootstrap.theme', requirejs.toUrl('bootstrap-theme'));
        $.widGEL.baseWidget.injectCssFile('widgets.pagewizard', requirejs.toUrl('widgets/pagewizard/pagewizard.css'));
       // $.widGEL.baseWidget.injectCssFile('jquery', requirejs.toUrl('css/jquery-ui-1.10.4.custom.css')); since this file does not exist
        $.widGEL.baseWidget.injectCssFile('font.awesome', requirejs.toUrl('fontawesome-css'));
		$.widGEL.baseWidget.injectCssFile('perc.css', requirejs.toUrl('perc-css'));
		$.widGEL.baseWidget.injectCssFile('dynatree.css', requirejs.toUrl('dynatree-css'));
        
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
                
                setTimeout(function () {
                    var viewModel = thisWidget._viewModel;
                    viewModel.init();
                }, 10);
            }
        });
        
        return "SUCCESS: " + widgetName + " Widget Registered.";
    }
);
