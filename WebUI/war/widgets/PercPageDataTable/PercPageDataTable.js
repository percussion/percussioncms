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

/**
 *  PercPageDataTable.js
 *  @author Jose Annunziato
 */
(function($) {
    $.fn.PercPageDataTable = function(config, excludeActionMenu) {
    
        // this is the data that will be rendered in the table cells
        var rows = config.percData;
        $.each(rows, function(index, row){
            
            if (typeof(row.rowContent[0][0].callback) === "undefined")
            {
                // add a callback to the page link to preview the page
                row.rowContent[0][0].callback = $.PercPreviewPage;
            }
        });
        
        //config.percHeaders[0] = "Title";
        
        if(typeof(config.percRowDblclickCallback) === "undefined")
        {
            config.percRowDblclickCallback = function(event){
                var jQuery = window.parent.jQuery;
                jQuery.PercNavigationManager.openPage(event.data.pagePath);
            };
        }
       
        if(!config.percMenus){ 
            config.percMenus = $.PercPageActions;
            
            }
        
        // configure a callback when the table is redrawn
        var fancyTooltips = function(){
            $(dataTable).find(".perc-index-0 > .perc-index-0 > span").each(function(){
                $(this).PercTooltip();
            });
        };
        if (typeof(config.percTableRedrawCallback) !== "object")
        {
            if (typeof(config.percTableRedrawCallback) === "function")
            {
                config.percTableRedrawCallback = [config.percTableRedrawCallback];
            }
            else
            {
                config.percTableRedrawCallback = [];
            }
        }
        config.percTableRedrawCallback.push(fancyTooltips);
        
        if(excludeActionMenu) {
            var dataTable = $(this).PercDataTable(config);
        }
        else {
            var dataTable = $(this).PercActionDataTable(config);
        }    
        
        fancyTooltips();
        
        return dataTable;
    }
})(jQuery); 
