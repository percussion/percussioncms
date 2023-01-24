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
 *  PercActionDataTable.js
 *  @author Jose Annunziato
 */
(function($) {
    
    var ACTION_COLUMN_WIDTH = 41;
    
    $.fn.PercActionDataTable = function(config) {
    	        
        // this is the data that will be rendered in the table cells
        var rows = config.percData;
        
        // add an Actions column at the far right with empty content
        // and make sure that the first column is labeled Title
        
        config.percHeaders.push("Actions");
        var menu = "";
        $.each(rows, function(index, row){
            // add the menu at the end
			var rowMenu = Array.isArray(config.percMenus)?config.percMenus[index]:config.percMenus;
            var menu = getActionMenu(rowMenu, config.percData[index].rowData);
            row.rowContent.push([{ "content": menu }]);
			
        });
        
        // add menu column as visible column
        if(config.percVisibleColumns)
            config.percVisibleColumns.push(config.percHeaders.length-1);
		
        // configure a callback when the table is redrawn
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
        config.percTableRedrawCallback.push(tableRedrawCallback);

        // make action column not sortable
        if(!config.aoColumns) {
            config.aoColumns = [];
        }
        config.aoColumns[config.percHeaders.length-1] = {bSortable: false};
        
        // render the data as a data table and put it in $(this)
        var dataTable = $(this).PercDataTable(config);
        
        dataTable.find("th.perc-last").width(ACTION_COLUMN_WIDTH);
		tableRedrawCallback(dataTable);
		
		return dataTable;
    };

    function getActionMenu(menu, rowData) {
        if(menu == undefined)
            menu = $.PercPageActions;
        var menuDiv = $('<div />').PercSimpleMenu(menu);
        
        menuDiv.find('a').each(function(){
            $(this).attr("href", percJQuery.perc_paths.ASSET_FORMS_EXPORT +"/"+rowData.formSummary.site  + "/" + rowData.formSummary.name + ".csv");
            $(this).attr("target", "_blank");
            $(this).addClass("perc-datatable-form-tracker-export-action");
            $(this).attr("rel", "noopener noreferrer");
        });
        
        if(rowData)
            menuDiv.data("data",rowData);
		return menuDiv;
    }
    
    $.PercOpenPage = function(event) {
        var data = event.data;
        if(data) {
            var jQuery = window.parent.jQuery;
            jQuery.PercNavigationManager.openPage(data.pagePath);
        } else {
            
        }
    };
    
    $.PercOpenAsset = function(event) {
        var data = event.data;
        if(data) {
            var assetObj ={};
            assetObj.id = data.assetId;
            assetObj.path = data.assetPath;
            var jQuery = window.parent.jQuery;
            jQuery.PercNavigationManager.openAsset(assetObj);
        }
    };

    $.PercPreviewAsset = function (event) {
        var data = event.data;
        if(!data)
            data = $(event.currentTarget).parents(".perc-datatable-row").data("percRowData");
        if(data) {
            var jQuery = window.parent.jQuery;
            jQuery.perc_finder().launchAssetPreview(data.assetId);
        }
    };

    $.PercPreviewPage = function (event) {
        var data = event.data;
        if(!data)
        	data = $(event.currentTarget).parents(".perc-datatable-row").data("percRowData");
        if(data) {
            var jQuery = window.parent.jQuery;
            jQuery.perc_finder().launchPagePreviewByPath(data.pagePath,data.pageId);
        } else {
            
        }
    };
    
    $.PercPageActions = { title : "", menuItemsAlign : "left", stayInsideOf : ".dataTables_wrapper",
            items : [
                {label : "Open Page",    callback : $.PercOpenPage},
                {label : "Preview Page", callback : $.PercPreviewPage}
    ]};

    function tableRedrawCallback(dataTable) {
		dataTable.find("td:last-child").attr("align", "right").attr("text-align", null)
            .on("dblclick", function(e){
                e.stopPropagation();
            });
		dataTable.find("td:last-child").attr("align", "right").attr("text-align", null)
            .on("dblclick", function(e){
                e.stopPropagation();
            });
		dataTable.find("td.perc-actions div").css("overflow","visible");
    }

})(jQuery); 
