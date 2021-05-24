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