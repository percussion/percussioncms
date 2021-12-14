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
