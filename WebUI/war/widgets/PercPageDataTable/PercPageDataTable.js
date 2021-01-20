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
