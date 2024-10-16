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

(function($,P) {

    // The CSS Style Controller. This acts as a mediation between the template model
    // and the three views which make up the Design->Style tabs. The controller holds
    // a single model object of the currently edited template, and will marshall it 
    // as needed to the given views. 
    P.cssController = function( model, root, frameView ) {
        
        // Use singleton controller to keep track of dirty status
        var dirtyController = $.PercDirtyController;

        // make the returns a variable called api and pass that along.
        var api = {
            getCurrentView: getCurrentView,
            getThemeList: getThemeList,
            getThemeCSS: getThemeCSS,
            setTemplateTheme: setTemplateTheme,
            getTemplateTheme: getTemplateTheme,
            setOverrideCSS: setOverrideCSS,
            getOverrideCSS: getOverrideCSS,
            showGalleryView: showGalleryView,
            showThemeView: showThemeView,
            showCSSOverrideView: showCSSOverrideView,
            save: save,
            updateTemplateObject: updateTemplateObject,
            model: model,
            refreshCssViews: refreshCssViews
        };

        return api;

        var self = this;
        var root = $("#frame");
        var currentView = null; // The current view active.
        
        /**
         * This is called to persist information on each tab change into the underlying model.
         * This is only really needed for the CSS Override tab, which we did not want
         * to place event handlers on each keypress inside the editor window.
         */
        function updateTemplateObject() {
            
            setOverrideCSS();
        }

        // Save calls the model object to save its changes back to the server.
        function save(callback) {
            $.PercBlockUI();
            model.save(function (data) {
                model.renderAll(root, function() { afterRender();  });
                callback(true, data);
                $.unblockUI();
                
                // reset dirty flag if style is saved
                dirtyController.setDirty(false);
            });
        }
        
        // The Next three functions deal with instantiating the views.
        // 'this' in this case refers to the jQuery.Percussion P object.
        function showGalleryView() {
            currentView = 0;
            P.cssGalleryView(this);
        }

        function showThemeView() {
            currentView = 1;
            P.cssThemeView(this);
        }

        function showCSSOverrideView() {
            currentView = 2;
            P.cssOverrideView(this);
        }
        
        function getCurrentView() {
            return currentView;
        }

        // Call the model to get the list of themes from the server.
        function getThemeList(callback) {
            model.getThemeList(function(status, data) {
                       callback(status, data);
            })
        }

        // call the model to get the CSS text for the currently selected
        // theme in the controller.
        function getThemeCSS(callback) {
            model.getThemeCSS(function(status, data) {
                callback(data.text());
            });
        }

        // Set the theme name for the template in our css object. Used by the Gallery view.
        // will not be permanent until the save.
        function setTemplateTheme(sThemeName, callback) {
	        var cssObj = model.getCSSObj();
	        cssObj.themeName = sThemeName;
	        model.setCSSObj(cssObj);
	        callback(true);  // This may change later.
        }
        
        // Get the current theme that's in the CSS object, keep in mind, this changes
        // every time a theme is also selected. will not be permanent until the save.
        function getTemplateTheme(callback) {
	        var cssObj = model.getCSSObj();
	        callback(cssObj.themeName); // This may change later.
        }
        
        // Set the override CSS inside the current CSS object. Changes are not committed
        // until save.
        function setOverrideCSS() {
        
            if ($("#perc-css-override-editor-area").val() == undefined)
                return;
                
            var curOverrideCSS = $('#perc-css-override-editor-area').val();
            $.perc_utils.debug("set override CSS: " + curOverrideCSS);
                
            var cssObj = model.getCSSObj();
            cssObj.cssOverride = curOverrideCSS;
            model.setCSSObj(cssObj);
        }
        
        function getOverrideCSS(callback) {
	        // Get the override CSS inside the current CSS object. Changes are not committed
	        // until save.
	        var cssObj = model.getCSSObj();
            callback(cssObj.cssOverride); // This may change later.
        }
        
        function afterRender() {
	        // This function is called after the render is complete from the server.
	        // attach our decorators (if we need any) here.
	        frameView.afterRender();
        }
        
        function refreshCssViews(){
            P.cssGalleryView(this);
            P.cssOverrideView(this);
        }
    }
})(jQuery,jQuery.Percussion);
