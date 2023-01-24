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

    P.cssThemeView = function(controller) {
    
        var root = $("#perc-css-theme-editor");    
        
    
        controller.getThemeCSS(function(data) {
            render(data);
        });
     
        
    
        // The render function, empties the tab container, and draws an instance of the CSS Theme Viewer. 
    
        function render(data) {
        
            root.empty();
            
            var editorHTML = 
            '<form id="perc-css-theme-editor-form">' +
            '<div id="perc-css-theme-editor-alert-text" class=".ui-state-highlight"></div>' +
            ' <div class="ui-layout-east" style="">' +           
            ' </div>' +
            ' <div class="ui-layout-center">' +
            '  <pre><div style="" id="perc-css-theme-editor-area">'+data+'</div></pre><br />' +
            ' </div>' +
            '</form>';
    
            root.append(editorHTML);          
            
            
       }     
        
    };
})(jQuery,jQuery.Percussion);
