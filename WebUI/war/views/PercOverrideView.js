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
     
     P.cssOverrideView = function(controller) {
         
         var currentContent;
         
         // Use singleton controller to keep track of dirty status
         var dirtyController = $.PercDirtyController;
         
         var root = $("#perc-css-editor");
    
         controller.getOverrideCSS(function(data) {
         
             render(data);
             
         });
    
        function saveCSS(callback){
            var currentView = $.PercNavigationManager.getView();
            if(currentView === $.PercNavigationManager.VIEW_EDITOR) {
                controller.save(callback);                
            }
            else if (currentView === $.PercNavigationManager.VIEW_EDIT_TEMPLATE) {
                callbackFunc = callback || function (){};
                controller.setOverrideCSS();
                controller.save(function (status, data) {
                    if (status === true) {
                        dirtyController.setDirty(false, "template");
                        callbackFunc();
                    }
                }); 
            }
        }
         
         function render(data) {
             
             root.empty();
    
             var editorHTML = 
                 '<form id="perc-css-override-editor-form">' +
                 '<div class=".ui-state-highlight"></div>' +
                 ' <div class="ui-layout-east" style="text-align: right;">' +
                 //'  <input type="button" id="perc-css-override-editor-previeww" />' +
                 ' </div>' +
                 ' <div class="ui-layout-center">' +
                 ' <div id="perc-css-override-editor-alert-text">&nbsp;</div>' +
                 '  <textarea rows="15" cols="90" id="perc-css-override-editor-area">'+data+'</textarea><br />' +
                 ' </div>' +
                 '</form>';
             
             root.append(editorHTML);
             
             // If user types in the text area of the CSS override, mark the style as dirty
             // It is then reset if style is saved or cancelled
             $("#perc-css-override-editor-area").off("change").on("change", function() {
            	 dirtyController.setDirty(true, "style" , saveCSS);
             });
         }
     };
 })(jQuery,jQuery.Percussion);
